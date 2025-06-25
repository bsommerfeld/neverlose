package de.sommerfeld.neverlose.persistence.mapper;

import de.sommerfeld.neverlose.persistence.dto.TrainingExerciseDTO;
import de.sommerfeld.neverlose.persistence.dto.TrainingPlanDTO;
import de.sommerfeld.neverlose.persistence.dto.TrainingUnitDTO;
import de.sommerfeld.neverlose.plan.TrainingPlan;
import de.sommerfeld.neverlose.plan.components.TrainingExercise;
import de.sommerfeld.neverlose.plan.components.TrainingUnit;

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

    /**
     * Converts a TrainingUnit domain object to its DTO representation.
     *
     * @param unit The TrainingUnit domain object.
     * @return The corresponding TrainingUnitDTO
     */
    TrainingUnitDTO toDTO(TrainingUnit unit);

    /**
     * Converts a TrainingUnitDTO to its TrainingUnit domain object representation.
     *
     * @param dto The TrainingUnitDTO.
     * @return The corresponding TrainingUnit domain object.
     */
    TrainingUnit toDomain(TrainingUnitDTO dto);

    /**
     * Converts a TrainingExercise domain object to its DTO representation.
     *
     * @param exercise The TrainingExercise domain object.
     * @return The corresponding TrainingExerciseDTO.
     */
    TrainingExerciseDTO toDTO(TrainingExercise exercise);

    /**
     * Converts a TrainingExerciseDTO to its TrainingExercise domain object representation.
     *
     * @param dto The TrainingExerciseDTO.
     * @return The corresponding TrainingExercise domain object.
     */
    TrainingExercise toDomain(TrainingExerciseDTO dto);
}
