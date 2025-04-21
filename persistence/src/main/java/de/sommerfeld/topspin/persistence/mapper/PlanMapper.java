package de.sommerfeld.topspin.persistence.mapper;

import de.sommerfeld.topspin.persistence.dto.TrainingPlanDTO;
import de.sommerfeld.topspin.plan.TrainingPlan;

/**
 * Maps between TrainingPlan domain objects and TrainingPlanDTOs for persistence.
 */
public interface PlanMapper {

    /**
     * Converts a TrainingPlan domain object to its DTO representation.
     *
     * @param plan The TrainingPlan domain object.
     * @return The corresponding TrainingPlanDTO.
     */
    TrainingPlanDTO toDTO(TrainingPlan plan);

    /**
     * Converts a TrainingPlanDTO to its TrainingPlan domain object representation.
     *
     * @param dto The TrainingPlanDTO.
     * @return The corresponding TrainingPlan domain object.
     */
    TrainingPlan toDomain(TrainingPlanDTO dto);
}
