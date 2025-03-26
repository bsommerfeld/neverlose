package de.sommerfeld.topspin.plan;

import de.sommerfeld.topspin.plan.components.collection.TrainingUnits;

public record TrainingPlan(String name, String description, long creationDate, TrainingUnits trainingUnits) {
}
