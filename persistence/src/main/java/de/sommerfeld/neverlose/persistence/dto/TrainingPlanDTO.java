package de.sommerfeld.neverlose.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DTO for persisting TrainingPlan data, including its list of unit DTOs.
 * This typically represents the root object saved in a JSON file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainingPlanDTO(
        UUID id,
        String name,
        String description,
        List<TrainingUnitDTO> trainingUnits
) {
    public TrainingPlanDTO {
        trainingUnits = (trainingUnits != null) ? List.copyOf(trainingUnits) : Collections.emptyList();
    }
}
