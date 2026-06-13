package com.calorietracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Calorie Tracker backend service.
 */
@SpringBootApplication
public class CalorieTrackerApplication {

    /**
     * Boots the Spring application context.
     *
     * @param args command-line arguments passed to the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(CalorieTrackerApplication.class, args);
    }
}
