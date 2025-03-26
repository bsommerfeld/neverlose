package de.sommerfeld.topspin.plan.components;

public class TrainingExercise {

    private final String name;
    private final String description;
    private final long duration;
    private final int sets;
    private final boolean ballBucket;

    public TrainingExercise(String name, String description, long duration, int sets, boolean ballBucket) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.sets = sets;
        this.ballBucket = ballBucket;
    }
}
