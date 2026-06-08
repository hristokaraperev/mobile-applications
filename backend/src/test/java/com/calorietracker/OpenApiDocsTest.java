package com.calorietracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the OpenAPI documentation is generated and publicly reachable, so Swagger
 * UI works during the defense without authentication.
 */
@AutoConfigureMockMvc
class OpenApiDocsTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiDocsAreReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUiIsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
