package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
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

    private final LogFacade log = LogFacadeFactory.getLogger();
    private final TrainingExercise exercise;
    private final TextField nameField;
    private final TextField descriptionField;
    private final TextField durationField;
    private final Spinner<Integer> setsSpinner;
    private final CheckBox ballBucketCheckBox;
    private final PlanStorageService planStorageService;

    /**
     * Creates a new ExerciseControl for the specified TrainingExercise.
     *
     * @param exercise the TrainingExercise to represent
     * @param planStorageService the service for loading and saving templates
     */
    public ExerciseControl(TrainingExercise exercise, PlanStorageService planStorageService) {
        this.exercise = exercise;
        this.planStorageService = planStorageService;

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
        nameLabel.getStyleClass().add("exercise-label");
        nameField = new TextField(exercise.getName());
        nameField.getStyleClass().add("exercise-name-field");
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setName(newVal));
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        // Description field
        Label descLabel = new Label("Description:");
        descLabel.getStyleClass().add("exercise-label");
        descriptionField = new TextField(exercise.getDescription());
        descriptionField.getStyleClass().add("exercise-description-field");
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setDescription(newVal));
        GridPane.setHgrow(descriptionField, Priority.ALWAYS);

        // Duration field
        Label durationLabel = new Label("Duration:");
        durationLabel.getStyleClass().add("exercise-label");
        durationField = new TextField(exercise.getDuration());
        durationField.getStyleClass().add("exercise-duration-field");
        durationField.textProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setDuration(newVal));

        // Sets spinner
        Label setsLabel = new Label("Sets:");
        setsLabel.getStyleClass().add("exercise-label");
        setsSpinner = new Spinner<>(1, 100, exercise.getSets());
        setsSpinner.setEditable(true);
        setsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            exercise.setSets(newVal));
        setsSpinner.getStyleClass().add("exercise-sets-spinner");

        // Ball bucket checkbox
        Label ballBucketLabel = new Label("Ball Bucket:");
        ballBucketLabel.getStyleClass().add("exercise-label");
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

        // Create a save as template button
        Button saveAsTemplateButton = new Button("Save As Template");
        saveAsTemplateButton.getStyleClass().add("save-as-template-button");
        saveAsTemplateButton.setOnAction(e -> handleSaveAsTemplate());

        HBox buttonContainer = new HBox(saveAsTemplateButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(5, 0, 0, 0));

        // Add the grid and button to the VBox
        getChildren().addAll(grid, buttonContainer);
    }

    /**
     * Gets the TrainingExercise represented by this control.
     *
     * @return the TrainingExercise
     */
    public TrainingExercise getExercise() {
        return exercise;
    }

    /**
     * Handles the action of saving the exercise as a template.
     * Checks for existing templates with the same name and asks for confirmation before overwriting.
     * Saves the exercise to the storage service and shows a confirmation message.
     */
    private void handleSaveAsTemplate() {
        try {
            // Check if an exercise with the same name already exists
            String exerciseName = exercise.getName();
            Optional<UUID> existingExerciseId = planStorageService.findExerciseIdByName(exerciseName);

            TrainingExercise exerciseToSave = exercise;

            if (existingExerciseId.isPresent() && !existingExerciseId.get().equals(exercise.getId())) {
                // An exercise with this name exists but has a different ID
                // Show confirmation dialog before overwriting
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Overwrite Template?");
                confirmDialog.setHeaderText("An Exercise template with the name '" + exerciseName + "' already exists.");
                confirmDialog.setContentText("Do you really want to overwrite the existing template?");

                // Apply application stylesheet to the dialog
                DialogPane dialogPane = confirmDialog.getDialogPane();
                if (getScene() != null && getScene().getRoot() != null) {
                    dialogPane.getStylesheets().addAll(getScene().getStylesheets());
                }

                // Wait for user response
                java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();

                // If user confirmed, create a new exercise with the existing ID
                if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                    log.info("User confirmed overwriting exercise template with name '{}'.", exerciseName);
                    // Create a new exercise with the existing ID
                    exerciseToSave = new TrainingExercise(
                        existingExerciseId.get(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getDuration(),
                        exercise.getSets(),
                        exercise.isBallBucket());
                } else {
                    // User canceled, abort save operation
                    log.info("User canceled overwriting exercise template with name '{}'.", exerciseName);
                    return;
                }
            }

            planStorageService.saveExercise(exerciseToSave);
            log.info("Exercise saved as template successfully: {}", exerciseToSave.getName());

            // Show success message
            showAlert(
                Alert.AlertType.INFORMATION,
                "Template Saved",
                null,
                "The exercise was successfully saved as a template.");
        } catch (IOException e) {
            log.error("Error saving exercise as template", e);

            // Show error message
            showAlert(
                Alert.AlertType.ERROR,
                "Error Saving",
                "Saving as template has failed.",
                "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog with the given parameters.
     *
     * @param alertType the type of alert
     * @param title the title of the alert
     * @param header the header text of the alert (can be null)
     * @param content the content text of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply application stylesheet to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        if (getScene() != null && getScene().getRoot() != null) {
            dialogPane.getStylesheets().addAll(getScene().getStylesheets());
        }

        alert.showAndWait();
    }
}
