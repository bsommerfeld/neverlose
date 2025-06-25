package de.bsommerfeld.neverlose.fx.viewmodel;

import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class TrainingUnitViewModel {

  private final TrainingUnit trainingUnit;

  private final StringProperty name;
  private final StringProperty description;
  private final ObjectProperty<Weekday> weekday;
  private final ObservableList<ExerciseViewModel> exercises;
  private final ObjectProperty<ExerciseViewModel> selectedExercise;

  public TrainingUnitViewModel(TrainingUnit trainingUnit) {
    if (trainingUnit == null) {
      throw new IllegalArgumentException("TrainingUnit model cannot be null.");
    }
    this.trainingUnit = trainingUnit;
    this.name = new SimpleStringProperty(trainingUnit.getName());
    this.description = new SimpleStringProperty(trainingUnit.getDescription());
    this.weekday = new SimpleObjectProperty<>(trainingUnit.getWeekday());

    List<TrainingExercise> modelExercises =
        trainingUnit.getTrainingExercises() != null
            ? trainingUnit.getTrainingExercises().getAll()
            : new ArrayList<>();

    this.exercises =
        FXCollections.observableArrayList(
            modelExercises.stream().map(ExerciseViewModel::new).collect(Collectors.toList()));

    this.selectedExercise = new SimpleObjectProperty<>();

    addModelUpdateListeners();
    addExerciseListSyncListeners();
  }

  private void addModelUpdateListeners() {
    name.addListener((obs, oldVal, newVal) -> trainingUnit.setName(newVal));
    description.addListener((obs, oldVal, newVal) -> trainingUnit.setDescription(newVal));
    weekday.addListener((obs, oldVal, newVal) -> trainingUnit.setWeekday(newVal));
  }

  private void addExerciseListSyncListeners() {
    if (trainingUnit.getTrainingExercises() == null) {
      System.err.println(
          "Warning: TrainingExercises collection in TrainingUnit model is null. Sync disabled.");
      return;
    }
    this.exercises.addListener(
        (ListChangeListener<ExerciseViewModel>)
            c -> {
              while (c.next()) {
                if (c.wasAdded()) {
                  for (ExerciseViewModel addedVm : c.getAddedSubList()) {
                    // Check if the model object already exists in the underlying list
                    // This requires the model collection exposes a contains() or similar method,
                    // or we get the modifiable list
                    if (!trainingUnit
                        .getTrainingExercises()
                        .getAll()
                        .contains(addedVm.getModel())) {
                      trainingUnit.getTrainingExercises().add(addedVm.getModel());
                    }
                  }
                }
                if (c.wasRemoved()) {
                  for (ExerciseViewModel removedVm : c.getRemoved()) {
                    trainingUnit.getTrainingExercises().remove(removedVm.getModel());
                  }
                }
                // TODO: Handle permutations (c.wasPermutated()) if list reordering is allowed and
                // needs model sync
                // TODO: Handle updates (c.wasUpdated()) - usually handled by property bindings
                // within ExerciseViewModel
              }
              // "plan changed" event if needed for preview updates upon list change
            });
  }

  public void addExercise() {
    TrainingExercise newExercise = new TrainingExercise("New Exercise", "", "5 min", 3, false);
    ExerciseViewModel newExerciseViewModel = new ExerciseViewModel(newExercise);
    this.exercises.add(newExerciseViewModel);
  }

  public void removeExercise(ExerciseViewModel exercise) {
    if (exercise != null) {
      this.exercises.remove(exercise);
    }
  }

  public void removeSelectedExercise() {
    ExerciseViewModel selected = selectedExercise.get();
    if (selected != null) {
      this.exercises.remove(selected);
    }
  }

  public StringProperty nameProperty() {
    return name;
  }

  public StringProperty descriptionProperty() {
    return description;
  }

  public ObjectProperty<Weekday> weekdayProperty() {
    return weekday;
  }

  public ObservableList<ExerciseViewModel> exercisesProperty() {
    return exercises;
  }

  public ObjectProperty<ExerciseViewModel> selectedExerciseProperty() {
    return selectedExercise;
  }

  public TrainingUnit getModel() {
    return trainingUnit;
  }

  @Override
  public String toString() {
    return "TrainingUnitViewModel{"
        + "trainingUnit="
        + trainingUnit
        + ", name="
        + name
        + ", description="
        + description
        + ", weekday="
        + weekday
        + ", exercises="
        + exercises
        + ", selectedExercise="
        + selectedExercise
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    TrainingUnitViewModel that = (TrainingUnitViewModel) o;
    return Objects.equals(trainingUnit, that.trainingUnit)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(weekday, that.weekday)
        && Objects.equals(exercises, that.exercises)
        && Objects.equals(selectedExercise, that.selectedExercise);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trainingUnit, name, description, weekday, exercises, selectedExercise);
  }
}
