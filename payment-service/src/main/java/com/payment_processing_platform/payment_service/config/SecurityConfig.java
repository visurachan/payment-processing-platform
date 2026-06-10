package com.payment_processing_platform.payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/**")
                        .access(new WebExpressionAuthorizationManager(
                                // Allows only localhost in development
                                // In production port 8081 is not publicly exposed —
                                // network-level protection handles external access
                                "hasIpAddress('127.0.0.1') or hasIpAddress('::1')"))
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
