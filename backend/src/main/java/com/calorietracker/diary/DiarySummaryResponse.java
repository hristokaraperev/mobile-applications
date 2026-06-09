package com.calorietracker.diary;

import java.time.LocalDate;
import java.util.Map;

/**
 * Daily nutrition summary grouped by meal, compared against the user's kcal goal.
 */
public record DiarySummaryResponse(
        LocalDate date,
        Integer dailyKcalGoal,
        Double totalKcal,
        Double totalProteinG,
        Double totalCarbsG,
        Double totalFatG,
        Map<String, MealTotals> meals
) {

    /** Nutrition totals for a single meal type. */
    public record MealTotals(
            Double kcal,
            Double proteinG,
            Double carbsG,
            Double fatG
    ) {}
}
