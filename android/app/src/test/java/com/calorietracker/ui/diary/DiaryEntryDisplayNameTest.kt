package com.calorietracker.ui.diary

import com.calorietracker.data.diary.DiaryEntryDto
import com.calorietracker.data.diary.displayName
import org.junit.Assert.assertEquals
import org.junit.Test

/** Behaviour of the diary entry display name: the snapshotted name, with a fallback when absent. */
class DiaryEntryDisplayNameTest {

    private fun entry(itemName: String?): DiaryEntryDto =
        DiaryEntryDto(
            id = "00000000-0000-0000-0000-000000000000",
            entryDate = "2026-06-13",
            mealType = "BREAKFAST",
            sourceType = "FOOD",
            quantity = 100.0,
            itemName = itemName,
            kcal = 0.0,
        )

    @Test
    fun `uses the snapshotted item name when present`() {
        assertEquals("Banana", entry("Banana").displayName())
    }

    @Test
    fun `falls back to a generic label when the name is missing`() {
        assertEquals("Item", entry(null).displayName())
    }
}
