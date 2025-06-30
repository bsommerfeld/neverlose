package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.fx.controller.ExerciseTemplateBrowserController;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A custom JavaFX control that represents a TrainingUnit in the WYSIWYG editor. This control allows
 * for direct editing of the TrainingUnit's properties and manages a list of ExerciseControl
 * components for the exercises in the unit.
 */
public class TrainingUnitControl extends VBox {

  private final LogFacade log = LogFacadeFactory.getLogger();
  private final TrainingUnit trainingUnit;
  private final TextField nameField;
  private final TextField descriptionField;
  private final ComboBox<Weekday> weekdayComboBox;
  private final VBox exercisesContainer;
  private final PlanStorageService planStorageService;
  private Consumer<TrainingUnit> saveAsTemplateCallback;
  private Consumer<TrainingUnit> onRemoveCallback;

  private boolean showAllExercises = false;
  private Button showMoreButton;

  // Placeholder for empty exercises list
  private Label placeholderLabel;

  // Toggle elements for collapsible functionality
  private Label toggleArrow;
  private VBox contentContainer; // Container for all collapsible elements
  private boolean isExpanded = true; // Default state is expanded

  /**
   * Creates a new TrainingUnitControl for the specified TrainingUnit.
   *
   * @param trainingUnit the TrainingUnit to represent
   * @param planStorageService the service for loading and saving templates
   */
  public TrainingUnitControl(TrainingUnit trainingUnit, PlanStorageService planStorageService) {
    this(trainingUnit, planStorageService, null, null);
  }

  /**
   * Creates a new TrainingUnitControl for the specified TrainingUnit with a callback for saving as
   * template.
   *
   * @param trainingUnit the TrainingUnit to represent
   * @param planStorageService the service for loading and saving templates
   * @param saveAsTemplateCallback callback to be called when the "Save as Template" button is
   *     clicked
   * @param onRemoveCallback callback to be called when the "Remove" button is clicked
   */
  public TrainingUnitControl(
      TrainingUnit trainingUnit,
      PlanStorageService planStorageService,
      Consumer<TrainingUnit> saveAsTemplateCallback,
      Consumer<TrainingUnit> onRemoveCallback) {
    this.trainingUnit = trainingUnit;
    this.planStorageService = planStorageService;
    this.saveAsTemplateCallback = saveAsTemplateCallback;
    this.onRemoveCallback = onRemoveCallback;

    // Configure the VBox
    setSpacing(10);
    setPadding(new Insets(15));
    getStyleClass().add("training-unit-control");

    // Create the header with toggle arrow, name field and weekday selector
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER_LEFT);

    // Toggle arrow for collapsible functionality
    toggleArrow = new Label("▼"); // Down arrow for expanded state
    toggleArrow.getStyleClass().add("toggle-arrow");
    toggleArrow.setStyle("-fx-cursor: hand;"); // Hand cursor to indicate it's clickable

    // Name field
    nameField = new TextField(trainingUnit.getName());
    nameField.getStyleClass().add("unit-name-field");
    nameField.textProperty().addListener((obs, oldVal, newVal) -> trainingUnit.setName(newVal));
    HBox.setHgrow(nameField, Priority.ALWAYS);

    // Weekday selector
    weekdayComboBox = new ComboBox<>();
    weekdayComboBox.getItems().addAll(Weekday.values());
    weekdayComboBox.setValue(trainingUnit.getWeekday());
    weekdayComboBox.setOnAction(e -> trainingUnit.setWeekday(weekdayComboBox.getValue()));
    weekdayComboBox.getStyleClass().add("unit-weekday-selector");

    // Save as Template button
    Button saveAsTemplateButton = new Button("Save");
    saveAsTemplateButton.getStyleClass().add("save-as-template-button");
    saveAsTemplateButton.setOnAction(e -> handleSaveAsTemplate());

    // Remove button (red X)
    Button removeButton = new Button("X");
    removeButton.getStyleClass().add("remove-button");
    removeButton.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    removeButton.setOnAction(e -> handleRemove());

    header
        .getChildren()
        .addAll(toggleArrow, nameField, weekdayComboBox, saveAsTemplateButton, removeButton);

    // Description field
    descriptionField = new TextField(trainingUnit.getDescription());
    descriptionField.getStyleClass().add("unit-description-field");
    descriptionField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> trainingUnit.setDescription(newVal));

    // Container for exercises
    exercisesContainer = new VBox(10);
    exercisesContainer.getStyleClass().add("exercises-container");

    // Create placeholder for empty exercises list
    placeholderLabel = new Label("No exercises yet. Click '+' to add one.");
    placeholderLabel.getStyleClass().add("placeholder-label");
    placeholderLabel.setAlignment(Pos.CENTER);
    placeholderLabel.setMaxWidth(Double.MAX_VALUE);
    placeholderLabel.setPadding(new Insets(20, 0, 20, 0));

    // Create the "Show more" button
    showMoreButton = new Button("Show more");
    showMoreButton.getStyleClass().add("show-more-button");
    showMoreButton.setMaxWidth(Double.MAX_VALUE); // Make button span full width
    showMoreButton.setOnAction(
        e -> {
          showAllExercises = true;
          updateExercisesVisibility();
        });

    // Add placeholder to exercises container
    exercisesContainer.getChildren().add(placeholderLabel);

    // Add existing exercises
    for (TrainingExercise exercise : trainingUnit.getTrainingExercises().getAll()) {
      addExerciseToUI(exercise);
    }

    // Add exercise button
    Button addExerciseButton = new Button("+");
    addExerciseButton.getStyleClass().add("add-exercise-button");
    addExerciseButton.setOnAction(e -> handleAddExercise());

    // Add from template button
    Button addFromTemplateButton = new Button("Load");
    addFromTemplateButton.getStyleClass().add("add-from-template-button");
    addFromTemplateButton.setOnAction(e -> handleAddExerciseFromTemplate());

    HBox addExerciseContainer = new HBox(10, addExerciseButton, addFromTemplateButton);
    addExerciseContainer.setAlignment(Pos.CENTER);

    // Create a container for all collapsible content
    contentContainer = new VBox(10);
    contentContainer
        .getChildren()
        .addAll(descriptionField, exercisesContainer, showMoreButton, addExerciseContainer);

    // Set up toggle functionality
    toggleArrow.setOnMouseClicked(e -> toggleContentVisibility());

    // Add all components to the VBox
    getChildren().addAll(header, contentContainer);

    // Initialize visibility of exercises
    updateExercisesVisibility();
  }

  /**
   * Returns whether this unit is currently expanded.
   *
   * @return true if expanded, false if collapsed
   */
  public boolean isExpanded() {
    return isExpanded;
  }

  /**
   * Sets the expanded state of this unit.
   *
   * @param expanded true to expand, false to collapse
   */
  public void setExpanded(boolean expanded) {
    if (this.isExpanded != expanded) {
      this.isExpanded = expanded;

      // Update the arrow icon based on the current state
      if (isExpanded) {
        toggleArrow.setText("▼"); // Down arrow for expanded state
      } else {
        toggleArrow.setText("▶"); // Right arrow for collapsed state
      }

      // Update the visibility of the content container
      contentContainer.setVisible(isExpanded);
      contentContainer.setManaged(isExpanded);
    }
  }

  /**
   * Adds an exercise to the UI.
   *
   * @param exercise the exercise to add
   */
  private void addExerciseToUI(TrainingExercise exercise) {
    ExerciseControl exerciseControl =
        new ExerciseControl(exercise, planStorageService, this::removeExercise);
    exercisesContainer.getChildren().add(exerciseControl);
    updateExercisesVisibility();
  }

  /**
   * Toggles the visibility of the content container. When collapsed, only the header is visible.
   * When expanded, all content is visible.
   */
  private void toggleContentVisibility() {
    isExpanded = !isExpanded;

    // Update the arrow icon based on the current state
    if (isExpanded) {
      toggleArrow.setText("▼"); // Down arrow for expanded state
    } else {
      toggleArrow.setText("▶"); // Right arrow for collapsed state
    }

    // Update the visibility of the content container
    contentContainer.setVisible(isExpanded);
    contentContainer.setManaged(isExpanded);
  }

  /**
   * Updates the visibility of exercises based on the showAllExercises flag. Shows only the first
   * three exercises if showAllExercises is false, or all exercises if showAllExercises is true.
   */
  private void updateExercisesVisibility() {
    List<Node> exercises = exercisesContainer.getChildren();
    // Count actual exercises (excluding the placeholder)
    int exerciseCount =
        (int) exercises.stream().filter(node -> !(node == placeholderLabel)).count();

    // Show placeholder if there are no exercises, hide it otherwise
    if (exerciseCount == 0) {
      placeholderLabel.setVisible(true);
      placeholderLabel.setManaged(true);

      // Hide the show more button if it exists
      if (showMoreButton != null) {
        showMoreButton.setVisible(false);
        showMoreButton.setManaged(false);
      }
      return;
    } else {
      placeholderLabel.setVisible(false);
      placeholderLabel.setManaged(false);
    }

    // If we have 3 or fewer exercises, show all and hide the button
    if (exerciseCount <= 3) {
      // Show all exercises
      for (Node exercise : exercises) {
        // Skip the placeholder
        if (exercise == placeholderLabel) continue;

        exercise.setVisible(true);
        exercise.setManaged(true);

        // Remove fade-out effect from all exercises
        exercise.getStyleClass().remove("exercise-fade-out");
      }

      // Hide the show more button if it exists
      if (showMoreButton != null) {
        showMoreButton.setVisible(false);
        showMoreButton.setManaged(false);
      }
      return;
    }

    // We have more than 3 exercises
    if (showAllExercises) {
      // Show all exercises
      for (Node exercise : exercises) {
        // Skip the placeholder
        if (exercise == placeholderLabel) continue;

        exercise.setVisible(true);
        exercise.setManaged(true);

        // Remove fade-out effect from all exercises
        exercise.getStyleClass().remove("exercise-fade-out");
      }

      // Hide the show more button
      showMoreButton.setVisible(false);
      showMoreButton.setManaged(false);
    } else {
      // Show only the first 3 exercises
      int visibleCount = 0;
      for (int i = 0; i < exercises.size(); i++) {
        Node exercise = exercises.get(i);

        // Skip the placeholder
        if (exercise == placeholderLabel) continue;

        boolean visible = visibleCount < 3;
        exercise.setVisible(visible);
        exercise.setManaged(visible);

        // Add fade-out effect to the third exercise
        if (visibleCount == 2) {
          exercise.getStyleClass().add("exercise-fade-out");
        } else {
          exercise.getStyleClass().remove("exercise-fade-out");
        }

        visibleCount++;
      }

      // Show the show more button
      showMoreButton.setVisible(true);
      showMoreButton.setManaged(true);
    }
  }

  /**
   * Removes an exercise from the training unit and updates the UI.
   *
   * @param exercise the exercise to remove
   */
  private void removeExercise(TrainingExercise exercise) {
    // Remove the exercise from the training unit
    trainingUnit.getTrainingExercises().remove(exercise);

    // Update the UI
    exercisesContainer.getChildren().clear();
    // Add placeholder back after clearing
    exercisesContainer.getChildren().add(placeholderLabel);
    for (TrainingExercise ex : trainingUnit.getTrainingExercises().getAll()) {
      addExerciseToUI(ex);
    }

    // Update visibility of exercises
    updateExercisesVisibility();
  }

  /** Handles the action of adding a new exercise. */
  private void handleAddExercise() {
    // Create a new exercise with default values
    TrainingExercise newExercise =
        new TrainingExercise("New Exercise", "Description", "30 min", 3, false);

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

  /**
   * Handles the action of saving the training unit as a template. If a callback is set, it will be
   * called with the training unit.
   */
  private void handleSaveAsTemplate() {
    if (saveAsTemplateCallback != null) {
      saveAsTemplateCallback.accept(trainingUnit);
    }
  }

  /**
   * Handles the action of removing the training unit. If a callback is set, it will be called with
   * the training unit after confirmation.
   */
  private void handleRemove() {
    // Show confirmation dialog
    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
    confirmDialog.setTitle("Remove Training Unit");
    confirmDialog.setHeaderText("Are you sure you want to remove this training unit?");
    confirmDialog.setContentText("This action cannot be undone.");

    // Apply application stylesheet to the dialog
    DialogPane dialogPane = confirmDialog.getDialogPane();
    if (getScene() != null && getScene().getRoot() != null) {
      dialogPane.getStylesheets().addAll(getScene().getStylesheets());
    }

    // Wait for user response
    Optional<ButtonType> result = confirmDialog.showAndWait();

    // If user confirmed, call the callback
    if (result.isPresent() && result.get() == ButtonType.OK) {
      if (onRemoveCallback != null) {
        onRemoveCallback.accept(trainingUnit);
      }
    }
  }

  /**
   * Handles the action of adding an exercise from a template. Opens the ExerciseTemplateBrowser and
   * adds the selected exercise to the training unit.
   */
  private void handleAddExerciseFromTemplate() {
    try {
      // Load the exercise template browser view
      FXMLLoader loader =
          new FXMLLoader(
              getClass()
                  .getResource(
                      "/de/bsommerfeld/neverlose/fx/controller/ExerciseTemplateBrowser.fxml"));
      // Set the controller factory to create the controller with the PlanStorageService
      loader.setControllerFactory(
          param -> new ExerciseTemplateBrowserController(planStorageService));
      Parent root = loader.load();

      // Get the controller and set the callback
      ExerciseTemplateBrowserController controller = loader.getController();
      controller.setTemplateSelectedCallback(this::addExerciseFromTemplate);

      // Create a new stage for the template browser
      Stage templateBrowserStage = new Stage();
      templateBrowserStage.setTitle("Exercise Templates");
      templateBrowserStage.initModality(Modality.APPLICATION_MODAL);
      templateBrowserStage.initOwner(getScene().getWindow());

      // Set the scene and show the stage
      Scene scene = new Scene(root, 600, 400);
      scene.getStylesheets().addAll(getScene().getStylesheets());
      templateBrowserStage.setScene(scene);
      templateBrowserStage.showAndWait();

    } catch (IOException e) {
      log.error("Error opening exercise template browser", e);
      showAlert(
          Alert.AlertType.ERROR,
          "Error Opening",
          "The exercise template browser could not be opened.",
          "An error occurred: " + e.getMessage());
    }
  }

  /**
   * Adds an exercise from a template to the training unit.
   *
   * @param templateExercise the template exercise to add
   */
  private void addExerciseFromTemplate(TrainingExercise templateExercise) {
    // Create a new exercise with a new ID
    TrainingExercise newExercise =
        new TrainingExercise(
            templateExercise.getName(),
            templateExercise.getDescription(),
            templateExercise.getDuration(),
            templateExercise.getSets(),
            templateExercise.isBallBucket());

    // Add it to the training unit
    trainingUnit.getTrainingExercises().add(newExercise);

    // Add it to the UI
    addExerciseToUI(newExercise);

    log.info("Added exercise template '{}' to training unit", templateExercise.getName());
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
