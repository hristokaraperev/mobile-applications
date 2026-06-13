package com.calorietracker.data.diary

import java.time.LocalDate
import javax.inject.Inject

/** A day's diary entries paired with its nutrition summary. */
data class DiaryDay(
    val date: LocalDate,
    val entries: List<DiaryEntryDto>,
    val summary: DiarySummaryDto,
)

/** Fetches a day's diary entries and totals from the API. */
class DiaryRepository @Inject constructor(
    private val api: DiaryApi,
) {

    /** Loads the entries and summary for [date] in a single call. */
    suspend fun loadDay(date: LocalDate): DiaryDay {
        val iso = date.toString()

        return DiaryDay(
            date = date,
            entries = api.entries(iso),
            summary = api.summary(iso),
        )
    }
}
