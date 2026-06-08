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
                .description("""
                        Personal calorie tracker — foods, recipes, and diary.

                        **Data attribution:** food data retrieved via `GET /foods/barcode/{ean}` \
                        may originate from [Open Food Facts](https://world.openfoodfacts.org) \
                        and is licensed under the \
                        [Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/).""")
                .version("v1"));
    }
}
