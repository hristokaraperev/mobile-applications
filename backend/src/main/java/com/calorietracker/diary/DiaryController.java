package com.calorietracker.diary;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
}
