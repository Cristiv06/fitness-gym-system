package com.fitness.gym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI gymOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description(
                                                "Conturi demo: admin / Admin123! (ADMIN+USER), user / User123! (USER). Apasa Authorize in Swagger.")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .info(new Info()
                        .title("Gym system API")
                        .description(
                                "REST API pentru gestionarea salii de fitness. "
                                        + "GET /api/** — USER sau ADMIN; POST/PUT/DELETE /api/** — doar ADMIN. "
                                        + "In Swagger foloseste **Authorize** (HTTP Basic) sau autentifica-te la /login in acelasi browser (localhost).")
                        .version("1.0"));
    }
}
