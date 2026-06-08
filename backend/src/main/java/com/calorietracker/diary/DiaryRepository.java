package com.calorietracker.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface DiaryRepository extends JpaRepository<DiaryEntry, UUID> {

    List<DiaryEntry> findByUserIdAndEntryDateAndDeletedFalse(Long userId, LocalDate entryDate);

    List<DiaryEntry> findByUserIdAndUpdatedAtAfter(Long userId, OffsetDateTime since);
}
