package de.sommerfeld.topspin.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) for persisting TrainingExercise data.
 * Designed for easy JSON serialization/deserialization with Jackson.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainingExerciseDTO(
        UUID id,
        String name,
        String description,
        String duration,   // Keep as String for flexibility as in domain model
        int sets,
        boolean ballBucket
) {
}
