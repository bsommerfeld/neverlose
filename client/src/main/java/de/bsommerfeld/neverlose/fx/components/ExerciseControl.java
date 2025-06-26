package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A custom JavaFX control that represents a TrainingExercise in the WYSIWYG editor.
 * This control allows for direct editing of the TrainingExercise's properties.
 */
public class ExerciseControl extends VBox {

    private final TrainingExercise exercise;
    private final TextField nameField;
    private final TextField descriptionField;
    private final TextField durationField;
    private final Spinner<Integer> setsSpinner;
    private final CheckBox ballBucketCheckBox;

    /**
     * Creates a new ExerciseControl for the specified TrainingExercise.
     *
     * @param exercise the TrainingExercise to represent
     */
    public ExerciseControl(TrainingExercise exercise) {
        this.exercise = exercise;
        
        // Configure the VBox
        setSpacing(8);
        setPadding(new Insets(10));
        getStyleClass().add("exercise-control");
        
        // Create a grid for the exercise properties
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));
        
        // Name field
        Label nameLabel = new Label("Name:");
        nameField = new TextField(exercise.getName());
        nameField.getStyleClass().add("exercise-name-field");
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setName(newVal));
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        
        // Description field
        Label descLabel = new Label("Description:");
        descriptionField = new TextField(exercise.getDescription());
        descriptionField.getStyleClass().add("exercise-description-field");
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setDescription(newVal));
        GridPane.setHgrow(descriptionField, Priority.ALWAYS);
        
        // Duration field
        Label durationLabel = new Label("Duration:");
        durationField = new TextField(exercise.getDuration());
        durationField.getStyleClass().add("exercise-duration-field");
        durationField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setDuration(newVal));
        
        // Sets spinner
        Label setsLabel = new Label("Sets:");
        setsSpinner = new Spinner<>(1, 100, exercise.getSets());
        setsSpinner.setEditable(true);
        setsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setSets(newVal));
        setsSpinner.getStyleClass().add("exercise-sets-spinner");
        
        // Ball bucket checkbox
        Label ballBucketLabel = new Label("Ball Bucket:");
        ballBucketCheckBox = new CheckBox();
        ballBucketCheckBox.setSelected(exercise.isBallBucket());
        ballBucketCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setBallBucket(newVal));
        ballBucketCheckBox.getStyleClass().add("exercise-ball-bucket-checkbox");
        
        // Add components to the grid
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        
        grid.add(descLabel, 0, 1);
        grid.add(descriptionField, 1, 1);
        
        grid.add(durationLabel, 0, 2);
        grid.add(durationField, 1, 2);
        
        grid.add(setsLabel, 0, 3);
        grid.add(setsSpinner, 1, 3);
        
        grid.add(ballBucketLabel, 0, 4);
        grid.add(ballBucketCheckBox, 1, 4);
        
        // Add the grid to the VBox
        getChildren().add(grid);
    }
    
    /**
     * Gets the TrainingExercise represented by this control.
     *
     * @return the TrainingExercise
     */
    public TrainingExercise getExercise() {
        return exercise;
    }
}