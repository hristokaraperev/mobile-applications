package com.calorietracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline security: the OpenAPI documentation and Swagger UI are publicly
 * reachable so the API can be demonstrated without a token. Everything else
 * requires authentication; the JWT authentication itself is added in a later slice.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
