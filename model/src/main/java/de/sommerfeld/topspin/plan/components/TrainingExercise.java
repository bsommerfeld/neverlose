package de.sommerfeld.topspin.plan.components;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a training exercise with specific attributes such as name, description,
 * duration, number of sets, and whether it involves the use of a ball bucket.
 * The unique identifier for each training exercise is generated automatically.
 * <p>
 * A training exercise is defined by the following properties:
 * - Name: The name or title of the exercise.
 * - Description: A detailed explanation of the exercise.
 * - Duration: The time duration for which the exercise should be performed.
 * - Sets: The number of sets or repetitions involved in the exercise.
 * - BallBucket: A flag indicating whether a ball bucket is required for the exercise.
 * <p>
 * This class provides methods to access and modify these properties,
 * as well as methods to compare and represent the exercise as a string.
 */
public class TrainingExercise {

    private final UUID id;

    private String name;
    private String description;
    private String duration;
    private int sets;
    private boolean ballBucket;

    public TrainingExercise(String name, String description, String duration, int sets, boolean ballBucket) {
        this(UUID.randomUUID(), name, description, duration, sets, ballBucket);
    }

    public TrainingExercise(UUID uuid, String name, String description, String duration, int sets, boolean ballBucket) {
        this.id = uuid;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.sets = sets;
        this.ballBucket = ballBucket;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public boolean isBallBucket() {
        return ballBucket;
    }

    public void setBallBucket(boolean ballBucket) {
        this.ballBucket = ballBucket;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrainingExercise that = (TrainingExercise) o;
        return sets == that.sets &&
                ballBucket == that.ballBucket &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, duration, sets, ballBucket);
    }

    @Override
    public String toString() {
        return "TrainingExercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", duration='" + duration + '\'' +
                ", sets=" + sets +
                ", ballBucket=" + ballBucket +
                '}';
    }
}