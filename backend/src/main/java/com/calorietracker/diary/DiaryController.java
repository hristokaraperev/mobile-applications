package com.calorietracker.diary;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for diary entry management and offline sync.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * Creates a diary entry, snapshotting nutrition from the referenced food or recipe at write time.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse create(
            @Valid @RequestBody CreateDiaryEntryRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.create(req, userId);
    }

    /**
     * Returns all non-deleted diary entries for the authenticated user on the given date.
     */
    @GetMapping
    public List<DiaryEntryResponse> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.findByDate(userId, date);
    }

    /**
     * Batch upserts diary entries from an offline sync payload.
     */
    @PostMapping("/sync")
    public List<DiaryEntryResponse> sync(
            @Valid @RequestBody List<SyncDiaryEntryRequest> items,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.sync(items, userId);
    }

    /**
     * Returns all entries (including soft-deleted) modified after {@code since} for offline sync.
     */
    @GetMapping("/changes")
    public List<DiaryEntryResponse> changes(
            @RequestParam OffsetDateTime since,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.findChanges(userId, since);
    }

    /**
     * Returns per-meal and daily nutrition totals for the authenticated user on the given date.
     */
    @GetMapping("/summary")
    public DiarySummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.summary(userId, date);
    }

    /**
     * Updates quantity and meal type of a diary entry, recalculating the nutrition snapshot.
     */
    @PutMapping("/{id}")
    public DiaryEntryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDiaryEntryRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        return diaryService.update(id, req, userId);
    }

    /** Soft-deletes a diary entry owned by the authenticated user. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal Long userId) {
        diaryService.softDelete(id, userId);
    }
}
