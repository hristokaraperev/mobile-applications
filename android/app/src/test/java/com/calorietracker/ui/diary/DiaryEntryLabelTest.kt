package com.calorietracker.ui.diary

import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.quantityLabel
import org.junit.Assert.assertEquals
import org.junit.Test

/** Behaviour of the diary entry quantity label: grams for foods, portions for recipe portions. */
class DiaryEntryLabelTest {

    private fun entry(sourceType: String, quantity: Double): DiaryEntryDto =
        DiaryEntryDto(
            id = "00000000-0000-0000-0000-000000000000",
            entryDate = "2026-06-13",
            mealType = "BREAKFAST",
            sourceType = sourceType,
            quantity = quantity,
            kcal = 0.0,
        )

    @Test
    fun `food entry reads in grams`() {
        assertEquals("150 g", entry("FOOD", 150.0).quantityLabel())
    }

    @Test
    fun `recipe portion entry reads in portions, pluralised`() {
        assertEquals("2 portions", entry("RECIPE_PORTION", 2.0).quantityLabel())
    }

    @Test
    fun `single recipe portion is singular`() {
        assertEquals("1 portion", entry("RECIPE_PORTION", 1.0).quantityLabel())
    }
}
