package com.calorietracker.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Persistence operations for {@link DiaryEntry}.
 */
public interface DiaryRepository extends JpaRepository<DiaryEntry, UUID> {

    /** Returns the non-deleted entries logged by a user on a given date, e.g. for a daily summary. */
    List<DiaryEntry> findByUserIdAndEntryDateAndDeletedFalse(Long userId, LocalDate entryDate);

    /** Returns all entries (including deleted ones) updated since the given timestamp, for client sync. */
    List<DiaryEntry> findByUserIdAndUpdatedAtAfter(Long userId, OffsetDateTime since);
}
