package com.fitness.userservice.config;

import com.fitness.userservice.security.InternalJwtService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ataseaza un JWT intern semnat pe fiecare apel Feign catre alte microservicii.
 */
@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor internalJwtRequestInterceptor(InternalJwtService jwtService) {
        return template -> template.header("Authorization", "Bearer " + jwtService.generateToken());
    }
}
