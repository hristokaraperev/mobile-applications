package com.calorietracker.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read and update operations for the authenticated user's own profile.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the profile of the user with the given id.
     *
     * @param userId the authenticated user's id.
     * @return the user's public profile.
     * @throws ResponseStatusException 404 if no such user exists.
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = requireUser(userId);

        return UserProfileResponse.from(user);
    }

    /**
     * Updates the profile of the user with the given id and returns the updated view.
     *
     * @param userId  the authenticated user's id.
     * @param request the fields to update.
     * @return the updated profile.
     * @throws ResponseStatusException 404 if no such user exists.
     */
    public UserProfileResponse updateProfile(Long userId, UpdateUserRequest request) {
        User user = requireUser(userId);
        user.setDailyKcalGoal(request.dailyKcalGoal());
        User saved = userRepository.save(user);

        return UserProfileResponse.from(saved);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
