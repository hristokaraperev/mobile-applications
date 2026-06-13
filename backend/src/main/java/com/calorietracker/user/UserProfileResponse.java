package com.calorietracker.user;

/**
 * Public view of a user's profile, returned by the {@code /users/me} endpoints.
 *
 * @param id            the user's unique identifier.
 * @param email         the user's login email.
 * @param displayName   the user's chosen display name, or {@code null} if unset.
 * @param dailyKcalGoal the user's target daily energy intake in kcal, or {@code null} if unset.
 */
public record UserProfileResponse(Long id, String email, String displayName, Integer dailyKcalGoal) {

    /** Maps a {@link User} entity to its public profile view. */
    static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getDailyKcalGoal()
        );
    }
}
