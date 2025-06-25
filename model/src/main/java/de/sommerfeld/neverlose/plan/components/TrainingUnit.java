package de.sommerfeld.neverlose.plan.components;

import de.sommerfeld.neverlose.plan.components.collection.TrainingExercises;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a training unit, which consists of a name, description, assigned weekday, and associated training
 * exercises.
 * <p>
 * A training unit is designed to organize and group specific exercises for a particular day.
 */
public class TrainingUnit {

    private final UUID id;
    private final TrainingExercises trainingExercises;
    private String name;
    private String description;
    private Weekday weekday;

    public TrainingUnit(String name, String description, Weekday weekday) {
        this(UUID.randomUUID(), name, description, weekday, new TrainingExercises());
    }

    public TrainingUnit(UUID uuid, String name, String description, Weekday weekday, TrainingExercises trainingExercises) {
        this.id = uuid;
        this.name = name;
        this.description = description;
        this.weekday = weekday;
        this.trainingExercises = trainingExercises;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Weekday getWeekday() {
        return weekday;
    }

    public void setWeekday(Weekday weekday) {
        this.weekday = weekday;
    }

    public TrainingExercises getTrainingExercises() {
        return trainingExercises;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrainingUnit that = (TrainingUnit) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                weekday == that.weekday &&
                Objects.equals(trainingExercises, that.trainingExercises);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, weekday, trainingExercises);
    }

    @Override
    public String toString() {
        return "TrainingUnit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weekday=" + weekday +
                ", trainingExercises=" + trainingExercises +
                '}';
    }
}
