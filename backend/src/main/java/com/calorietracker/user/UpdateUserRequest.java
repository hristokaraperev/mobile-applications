package com.calorietracker.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for {@code PUT /users/me}.
 *
 * @param dailyKcalGoal the new target daily energy intake in kcal; must be positive.
 */
public record UpdateUserRequest(@NotNull @Positive Integer dailyKcalGoal) {}
