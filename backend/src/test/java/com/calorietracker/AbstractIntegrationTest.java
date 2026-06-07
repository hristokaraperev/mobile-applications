package com.calorietracker;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests. Boots the full Spring context against an
 * in-memory H2 database running in PostgreSQL-compatibility mode, with the same
 * Flyway migrations applied as production.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}
