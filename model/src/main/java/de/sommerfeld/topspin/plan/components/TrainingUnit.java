package de.sommerfeld.topspin.plan.components;

import de.sommerfeld.topspin.plan.components.collection.TrainingExercises;

public record TrainingUnit(String name, String description, Weekday weekday, TrainingExercises trainingExercises) {
}
