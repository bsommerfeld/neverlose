package de.sommerfeld.topspin.plan;

import de.sommerfeld.topspin.plan.components.collection.TrainingUnits;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a training plan that consists of a collection of training units.
 * A training plan is identified by a unique ID and contains a name and description.
 */
public class TrainingPlan {

    private final UUID id;

    private String name;
    private String description;
    private final TrainingUnits trainingUnits;

    public TrainingPlan(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.trainingUnits = new TrainingUnits();
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

    public TrainingUnits getTrainingUnits() {
        return trainingUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrainingPlan that = (TrainingPlan) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(trainingUnits, that.trainingUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, trainingUnits);
    }

    @Override
    public String toString() {
        return "TrainingPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", trainingUnits=" + trainingUnits +
                '}';
    }
}