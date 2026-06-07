package com.calorietracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the API surfaced through Swagger UI. Endpoints added in later slices
 * are documented automatically by springdoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI calorieTrackerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Calorie Tracker API")
                .description("Personal calorie tracker — foods, recipes, and diary.")
                .version("v1"));
    }
}
