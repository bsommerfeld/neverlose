package de.sommerfeld.neverlose.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.sommerfeld.neverlose.plan.components.Weekday;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DTO for persisting TrainingUnit data, including its list of exercise DTOs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainingUnitDTO(
        UUID id,
        String name,
        String description,
        Weekday weekday,
        List<TrainingExerciseDTO> trainingExercises
) {
    public TrainingUnitDTO {
        trainingExercises = (trainingExercises != null) ? List.copyOf(trainingExercises) : Collections.emptyList();
    }
}
