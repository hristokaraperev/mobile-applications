package com.calorietracker.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Looks up a user by email, e.g. during login. */
    Optional<User> findByEmail(String email);

    /** Checks whether an account with the given email already exists, e.g. during registration. */
    boolean existsByEmail(String email);
}
