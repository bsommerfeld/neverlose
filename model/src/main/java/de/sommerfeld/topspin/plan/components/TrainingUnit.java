package de.sommerfeld.topspin.plan.components;

public class TrainingUnit {

    private final String name;
    private final String description;
    private final Weekday weekday;
    private final TrainingExercises trainingExercises;

    public TrainingUnit(String name, String description, Weekday weekday, TrainingExercises trainingExercises) {
        this.name = name;
        this.description = description;
        this.weekday = weekday;
        this.trainingExercises = trainingExercises;
    }
}
