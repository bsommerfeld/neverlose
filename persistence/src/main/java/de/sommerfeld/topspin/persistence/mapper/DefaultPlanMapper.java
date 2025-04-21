package de.sommerfeld.topspin.persistence.mapper;

import de.sommerfeld.topspin.persistence.dto.TrainingExerciseDTO;
import de.sommerfeld.topspin.persistence.dto.TrainingPlanDTO;
import de.sommerfeld.topspin.persistence.dto.TrainingUnitDTO;
import de.sommerfeld.topspin.plan.TrainingPlan;
import de.sommerfeld.topspin.plan.components.TrainingExercise;
import de.sommerfeld.topspin.plan.components.TrainingUnit;
import de.sommerfeld.topspin.plan.components.collection.TrainingExercises;
import de.sommerfeld.topspin.plan.components.collection.TrainingUnits;

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
                        .map(this::toUnitDTO)
                        .toList();

        return new TrainingPlanDTO(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                unitDTOs
        );
    }

    private TrainingUnitDTO toUnitDTO(TrainingUnit unit) {
        Objects.requireNonNull(unit, "Cannot map null TrainingUnit");
        List<TrainingExerciseDTO> exerciseDTOs = (unit.getTrainingExercises() == null) ? Collections.emptyList() :
                unit.getTrainingExercises().getAll().stream()
                        .map(this::toExerciseDTO)
                        .toList();

        return new TrainingUnitDTO(
                unit.getId(),
                unit.getName(),
                unit.getDescription(),
                unit.getWeekday(),
                exerciseDTOs
        );
    }

    private TrainingExerciseDTO toExerciseDTO(TrainingExercise exercise) {
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
                        .map(this::toUnitDomain)
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

    private TrainingUnit toUnitDomain(TrainingUnitDTO dto) {
        Objects.requireNonNull(dto, "Cannot map null TrainingUnitDTO");

        List<TrainingExercise> exercises = (dto.trainingExercises() == null) ? Collections.emptyList() :
                dto.trainingExercises().stream()
                        .map(this::toExerciseDomain)
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

    private TrainingExercise toExerciseDomain(TrainingExerciseDTO dto) {
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
