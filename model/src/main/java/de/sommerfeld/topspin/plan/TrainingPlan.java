package de.sommerfeld.topspin.plan;

import de.sommerfeld.topspin.plan.components.TrainingUnits;

public class TrainingPlan {

    private final String name;
    private final String description;
    private final TrainingUnits trainingUnits;

    public TrainingPlan(String name, String description, TrainingUnits trainingUnits) {
        this.name = name;
        this.description = description;
        this.trainingUnits = trainingUnits;
    }
}
