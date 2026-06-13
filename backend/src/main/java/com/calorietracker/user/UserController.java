package com.calorietracker.user;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the authenticated user's own profile.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Returns the authenticated user's profile, including their daily kcal goal. */
    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    /** Updates the authenticated user's profile (currently their daily kcal goal). */
    @PutMapping("/me")
    public UserProfileResponse updateMe(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return userService.updateProfile(userId, request);
    }
}
