package com.calorietracker;

import org.junit.jupiter.api.Test;

/**
 * Tracer-bullet test: proves the application context boots end-to-end against a
 * real Postgres, with Flyway migrations applied and JPA schema validation passing.
 */
class ApplicationContextLoadsTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Context startup is the assertion; failure to boot fails the test.
    }
}
