package de.sommerfeld.topspin.persistence.model;

import java.util.UUID;

/**
 * Data Transfer Object holding summary information for a saved TrainingExercise template.
 *
 * @param identifier Unique identifier (UUID) of the exercise.
 * @param name       The user-defined name of the exercise.
 */
public record ExerciseSummary(UUID identifier, String name) {
}