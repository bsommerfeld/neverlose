package de.sommerfeld.neverlose.persistence.mapper;

import de.sommerfeld.neverlose.persistence.dto.TrainingExerciseDTO;
import de.sommerfeld.neverlose.persistence.dto.TrainingPlanDTO;
import de.sommerfeld.neverlose.persistence.dto.TrainingUnitDTO;
import de.sommerfeld.neverlose.plan.TrainingPlan;
import de.sommerfeld.neverlose.plan.components.TrainingExercise;
import de.sommerfeld.neverlose.plan.components.TrainingUnit;
import de.sommerfeld.neverlose.plan.components.collection.TrainingExercises;
import de.sommerfeld.neverlose.plan.components.collection.TrainingUnits;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation for mapping between domain objects and persistence DTOs.
 */
public class DefaultPlanMapper implements PlanMapper {

    @Override
    public TrainingPlanDTO toDTO(TrainingPlan plan) {
        Objects.requireNonNull(plan, "Cannot map null TrainingPlan");
        List<TrainingUnitDTO> unitDTOs = (plan.getTrainingUnits() == null) ? Collections.emptyList() :
                plan.getTrainingUnits().getAll().stream()
                        .map(this::toDTO)
                        .toList();

        return new TrainingPlanDTO(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                unitDTOs
        );
    }

    @Override
    public TrainingUnitDTO toDTO(TrainingUnit unit) {
        Objects.requireNonNull(unit, "Cannot map null TrainingUnit");
        List<TrainingExerciseDTO> exerciseDTOs = (unit.getTrainingExercises() == null) ? Collections.emptyList() :
                unit.getTrainingExercises().getAll().stream()
                        .map(this::toDTO)
                        .toList();

        return new TrainingUnitDTO(
                unit.getId(),
                unit.getName(),
                unit.getDescription(),
                unit.getWeekday(),
                exerciseDTOs
        );
    }

    @Override
    public TrainingExerciseDTO toDTO(TrainingExercise exercise) {
        Objects.requireNonNull(exercise, "Cannot map null TrainingExercise");
        return new TrainingExerciseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getDuration(),
                exercise.getSets(),
                exercise.isBallBucket()
        );
    }

    @Override
    public TrainingPlan toDomain(TrainingPlanDTO dto) {
        Objects.requireNonNull(dto, "Cannot map null TrainingPlanDTO");

        List<TrainingUnit> units = (dto.trainingUnits() == null) ? Collections.emptyList() :
                dto.trainingUnits().stream()
                        .map(this::toDomain)
                        .toList();

        TrainingUnits trainingUnits = new TrainingUnits();
        units.forEach(trainingUnits::add);

        return new TrainingPlan(
                dto.id(),
                dto.name(),
                dto.description(),
                trainingUnits
        );
    }

    @Override
    public TrainingUnit toDomain(TrainingUnitDTO dto) {
        Objects.requireNonNull(dto, "Cannot map null TrainingUnitDTO");

        List<TrainingExercise> exercises = (dto.trainingExercises() == null) ? Collections.emptyList() :
                dto.trainingExercises().stream()
                        .map(this::toDomain)
                        .toList();

        TrainingExercises trainingExercises = new TrainingExercises();
        exercises.forEach(trainingExercises::add);

        return new TrainingUnit(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.weekday(),
                trainingExercises
        );
    }

    @Override
    public TrainingExercise toDomain(TrainingExerciseDTO dto) {
        Objects.requireNonNull(dto, "Cannot map null TrainingExerciseDTO");

        return new TrainingExercise(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.duration(),
                dto.sets(),
                dto.ballBucket()
        );
    }

}
