package de.bsommerfeld.neverlose.fx.viewmodel;

import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import java.util.Objects;
import javafx.beans.property.*;

public class ExerciseViewModel {
  private final TrainingExercise exercise;

  private final StringProperty name;
  private final StringProperty description;
  private final StringProperty duration;
  private final IntegerProperty sets;
  private final BooleanProperty ballBucket;

  public ExerciseViewModel(TrainingExercise exercise) {
    if (exercise == null) {
      throw new IllegalArgumentException("TrainingExercise model cannot be null.");
    }
    this.exercise = exercise;
    this.name = new SimpleStringProperty(exercise.getName());
    this.description = new SimpleStringProperty(exercise.getDescription());
    this.duration = new SimpleStringProperty(exercise.getDuration());
    this.sets = new SimpleIntegerProperty(exercise.getSets());
    this.ballBucket = new SimpleBooleanProperty(exercise.isBallBucket());

    addModelUpdateListeners();
  }

  private void addModelUpdateListeners() {
    name.addListener((obs, oldVal, newVal) -> exercise.setName(newVal));
    description.addListener((obs, oldVal, newVal) -> exercise.setDescription(newVal));
    duration.addListener((obs, oldVal, newVal) -> exercise.setDuration(newVal));
    sets.addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) {
            exercise.setSets(newVal.intValue());
          }
        });
    ballBucket.addListener((obs, oldVal, newVal) -> exercise.setBallBucket(newVal));
  }

  public StringProperty nameProperty() {
    return name;
  }

  public StringProperty descriptionProperty() {
    return description;
  }

  public StringProperty durationProperty() {
    return duration;
  }

  public IntegerProperty setsProperty() {
    return sets;
  }

  public BooleanProperty ballBucketProperty() {
    return ballBucket;
  }

  public TrainingExercise getModel() {
    return exercise;
  }

  @Override
  public String toString() {
    return "ExerciseViewModel{"
        + "exercise="
        + exercise
        + ", name="
        + name
        + ", description="
        + description
        + ", duration="
        + duration
        + ", sets="
        + sets
        + ", ballBucket="
        + ballBucket
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ExerciseViewModel that = (ExerciseViewModel) o;
    return Objects.equals(exercise, that.exercise)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(duration, that.duration)
        && Objects.equals(sets, that.sets)
        && Objects.equals(ballBucket, that.ballBucket);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exercise, name, description, duration, sets, ballBucket);
  }
}
