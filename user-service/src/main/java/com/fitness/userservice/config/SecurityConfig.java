package com.fitness.userservice.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import com.fitness.userservice.security.InternalJwtAuthenticationFilter;
import com.fitness.userservice.security.InternalJwtService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final AuthorizationManager<RequestAuthorizationContext> API_ACCESS =
            (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
                Authentication auth = authentication.get();
                if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                    return new AuthorizationDecision(false);
                }
                var roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                boolean admin = roles.contains("ROLE_ADMIN");
                boolean user = roles.contains("ROLE_USER");
                String method = context.getRequest().getMethod();
                if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method)) {
                    return new AuthorizationDecision(admin || user);
                }
                return new AuthorizationDecision(admin);
            };

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService uds, PasswordEncoder pe) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(pe);
        return provider;
    }

    @Bean
    PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            DaoAuthenticationProvider authProvider,
            PersistentTokenRepository tokenRepo,
            UserDetailsService uds,
            InternalJwtService internalJwtService,
            @Value("${app.security.remember-me-key}") String rememberMeKey) throws Exception {
        http.authenticationProvider(authProvider)
                .addFilterBefore(new InternalJwtAuthenticationFilter(internalJwtService),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/api/internal/**").hasRole("SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/auth/me/classes", "/api/auth/me/enrollments")
                            .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/**")).access(API_ACCESS)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**")
                            .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login").permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true).deleteCookies("remember-me", "JSESSIONID").permitAll())
                .rememberMe(remember -> remember.tokenRepository(tokenRepo).key(rememberMeKey).userDetailsService(uds))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(SecurityConfig::sendUnauthorized)
                        .accessDeniedHandler(SecurityConfig::sendForbidden));
        return http.build();
    }

    private static void sendUnauthorized(jakarta.servlet.http.HttpServletRequest req,
            HttpServletResponse res, AuthenticationException ex) throws IOException {
        if (req.getRequestURI().startsWith("/api/")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"Neautentificat.\"}");
        } else {
            res.sendRedirect("/login");
        }
    }

    private static void sendForbidden(jakarta.servlet.http.HttpServletRequest req,
            HttpServletResponse res, AccessDeniedException ex) throws IOException {
        if (req.getRequestURI().startsWith("/api/")) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"Acces interzis pentru acest rol.\"}");
        } else {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
