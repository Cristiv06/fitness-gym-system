package com.fitness.gym.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** One rule for /api/**: GET/HEAD = USER or ADMIN; other methods = ADMIN only. */
    private static final AuthorizationManager<RequestAuthorizationContext> API_ACCESS =
            (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
                Authentication auth = authentication.get();
                if (auth == null
                        || !auth.isAuthenticated()
                        || auth instanceof AnonymousAuthenticationToken) {
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
    DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            PersistentTokenRepository persistentTokenRepository,
            UserDetailsService userDetailsService,
            @Value("${app.security.remember-me-key}") String rememberMeKey)
            throws Exception {
        http.authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/", "/home", "/login", "/error", "/css/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/register")
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/auth/me/classes",
                                        "/api/auth/me/enrollments")
                                .hasAnyRole("USER", "ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/api/**"))
                                .access(API_ACCESS)
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**")
                                .hasAnyRole("USER", "ADMIN")
                                .anyRequest()
                                .authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login").permitAll())
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("remember-me", "JSESSIONID")
                        .permitAll())
                .rememberMe(remember -> remember.tokenRepository(persistentTokenRepository)
                        .key(rememberMeKey)
                        .userDetailsService(userDetailsService))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(SecurityConfig::sendUnauthorizedOrRedirect)
                        .accessDeniedHandler(SecurityConfig::sendForbiddenJsonForApi));

        return http.build();
    }

    private static void sendUnauthorizedOrRedirect(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        if (request.getRequestURI().startsWith("/api/")
                || request.getRequestURI().startsWith("/v3/api-docs")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Neautentificat.\"}");
        } else {
            response.sendRedirect("/login");
        }
    }

    private static void sendForbiddenJsonForApi(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        if (request.getRequestURI().startsWith("/api/")
                || request.getRequestURI().startsWith("/v3/api-docs")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Acces interzis pentru acest rol.\"}");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
