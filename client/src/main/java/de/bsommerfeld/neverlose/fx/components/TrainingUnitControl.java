package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A custom JavaFX control that represents a TrainingUnit in the WYSIWYG editor.
 * This control allows for direct editing of the TrainingUnit's properties and
 * manages a list of ExerciseControl components for the exercises in the unit.
 */
public class TrainingUnitControl extends VBox {

    private final TrainingUnit trainingUnit;
    private final TextField nameField;
    private final TextField descriptionField;
    private final ComboBox<Weekday> weekdayComboBox;
    private final VBox exercisesContainer;

    /**
     * Creates a new TrainingUnitControl for the specified TrainingUnit.
     *
     * @param trainingUnit the TrainingUnit to represent
     */
    public TrainingUnitControl(TrainingUnit trainingUnit) {
        this.trainingUnit = trainingUnit;
        
        // Configure the VBox
        setSpacing(10);
        setPadding(new Insets(15));
        getStyleClass().add("training-unit-control");
        
        // Create the header with name field and weekday selector
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Name field
        nameField = new TextField(trainingUnit.getName());
        nameField.getStyleClass().add("unit-name-field");
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            trainingUnit.setName(newVal));
        HBox.setHgrow(nameField, Priority.ALWAYS);
        
        // Weekday selector
        weekdayComboBox = new ComboBox<>();
        weekdayComboBox.getItems().addAll(Weekday.values());
        weekdayComboBox.setValue(trainingUnit.getWeekday());
        weekdayComboBox.setOnAction(e -> 
            trainingUnit.setWeekday(weekdayComboBox.getValue()));
        weekdayComboBox.getStyleClass().add("unit-weekday-selector");
        
        header.getChildren().addAll(nameField, weekdayComboBox);
        
        // Description field
        descriptionField = new TextField(trainingUnit.getDescription());
        descriptionField.getStyleClass().add("unit-description-field");
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> 
            trainingUnit.setDescription(newVal));
        
        // Container for exercises
        exercisesContainer = new VBox(10);
        exercisesContainer.getStyleClass().add("exercises-container");
        
        // Add existing exercises
        for (TrainingExercise exercise : trainingUnit.getTrainingExercises().getAll()) {
            addExerciseToUI(exercise);
        }
        
        // Add exercise button
        Button addExerciseButton = new Button("+");
        addExerciseButton.getStyleClass().add("add-exercise-button");
        addExerciseButton.setOnAction(e -> handleAddExercise());
        
        HBox addExerciseContainer = new HBox(addExerciseButton);
        addExerciseContainer.setAlignment(Pos.CENTER);
        
        // Add all components to the VBox
        getChildren().addAll(header, descriptionField, exercisesContainer, addExerciseContainer);
    }
    
    /**
     * Adds an exercise to the UI.
     *
     * @param exercise the exercise to add
     */
    private void addExerciseToUI(TrainingExercise exercise) {
        ExerciseControl exerciseControl = new ExerciseControl(exercise);
        exercisesContainer.getChildren().add(exerciseControl);
    }
    
    /**
     * Handles the action of adding a new exercise.
     */
    private void handleAddExercise() {
        // Create a new exercise with default values
        TrainingExercise newExercise = new TrainingExercise(
            "New Exercise", "Description", "30 min", 3, false);
        
        // Add it to the training unit
        trainingUnit.getTrainingExercises().add(newExercise);
        
        // Add it to the UI
        addExerciseToUI(newExercise);
    }
    
    /**
     * Gets the TrainingUnit represented by this control.
     *
     * @return the TrainingUnit
     */
    public TrainingUnit getTrainingUnit() {
        return trainingUnit;
    }
}