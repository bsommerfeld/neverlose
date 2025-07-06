package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A custom JavaFX control that represents a TrainingExercise in the WYSIWYG editor. This control
 * allows for direct editing of the TrainingExercise's properties.
 */
public class ExerciseControl extends VBox {

  private final LogFacade log = LogFacadeFactory.getLogger();
  private final TrainingExercise exercise;
  private final PlanStorageService planStorageService;
  private final NotificationService notificationService;
  private final Consumer<TrainingExercise> onRemoveCallback;

  // UI components for action buttons
  private final HBox actionButtonsContainer;
  private final HBox moreButtonContainer;

  // Event handlers for mouse events
  private EventHandler<MouseEvent> mouseEnteredHandler;
  private EventHandler<MouseEvent> mouseExitedHandler;
  private ChangeListener<Boolean> visibilityListener;
  private boolean listenersActive = false;

  /**
   * Creates a new ExerciseControl for the specified TrainingExercise.
   *
   * @param exercise the TrainingExercise to represent
   * @param planStorageService the service for loading and saving templates
   * @param notificationService the service for displaying notifications
   */
  public ExerciseControl(
      TrainingExercise exercise,
      PlanStorageService planStorageService,
      NotificationService notificationService) {
    this(exercise, planStorageService, notificationService, null);
  }

  /**
   * Creates a new ExerciseControl for the specified TrainingExercise with a callback for removal.
   *
   * @param exercise the TrainingExercise to represent
   * @param planStorageService the service for loading and saving templates
   * @param notificationService the service for displaying notifications
   * @param onRemoveCallback callback to be called when the "Remove" button is clicked
   */
  public ExerciseControl(
      TrainingExercise exercise,
      PlanStorageService planStorageService,
      NotificationService notificationService,
      Consumer<TrainingExercise> onRemoveCallback) {
    this.exercise = exercise;
    this.planStorageService = planStorageService;
    this.notificationService = notificationService;
    this.onRemoveCallback = onRemoveCallback;

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
    Label nameLabel = new Label(Messages.getString("ui.label.name"));
    nameLabel.getStyleClass().add("exercise-label");
    TextField nameField = new TextField(exercise.getName());
    nameField.getStyleClass().add("exercise-name-field");
    nameField.textProperty().addListener((obs, oldVal, newVal) -> exercise.setName(newVal));
    GridPane.setHgrow(nameField, Priority.ALWAYS);

    // Description field
    Label descLabel = new Label(Messages.getString("ui.label.description"));
    descLabel.getStyleClass().add("exercise-label");
    TextField descriptionField = new TextField(exercise.getDescription());
    descriptionField.getStyleClass().add("exercise-description-field");
    descriptionField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> exercise.setDescription(newVal));
    GridPane.setHgrow(descriptionField, Priority.ALWAYS);

    // Duration field
    Label durationLabel = new Label(Messages.getString("ui.label.duration"));
    durationLabel.getStyleClass().add("exercise-label");
    TextField durationField = new TextField(exercise.getDuration());
    durationField.getStyleClass().add("exercise-duration-field");
    durationField.textProperty().addListener((obs, oldVal, newVal) -> exercise.setDuration(newVal));

    // Sets spinner
    Label setsLabel = new Label(Messages.getString("ui.label.sets"));
    setsLabel.getStyleClass().add("exercise-label");
    Spinner<Integer> setsSpinner = new Spinner<>(1, 100, exercise.getSets());
    setsSpinner.setEditable(true);
    setsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> exercise.setSets(newVal));
    setsSpinner.getStyleClass().add("exercise-sets-spinner");

    // Ball bucket checkbox
    Label ballBucketLabel = new Label(Messages.getString("ui.label.ballBucket"));
    ballBucketLabel.getStyleClass().add("exercise-label");
    CheckBox ballBucketCheckBox = new CheckBox();
    ballBucketCheckBox.setSelected(exercise.isBallBucket());
    ballBucketCheckBox
        .selectedProperty()
        .addListener((obs, oldVal, newVal) -> exercise.setBallBucket(newVal));
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
    Button saveAsTemplateButton = new Button(Messages.getString("ui.button.save"));
    saveAsTemplateButton.getStyleClass().add("save-as-template-button");
    saveAsTemplateButton.setOnAction(e -> handleSaveAsTemplate());

    // Create a remove button (red X)
    Button removeButton = new Button(Messages.getString("ui.button.remove"));
    removeButton.getStyleClass().add("remove-button");
    removeButton.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    removeButton.setOnAction(e -> handleRemove());

    // Create a more button for touch devices
    Button moreButton = new Button(Messages.getString("exercise.moreButton"));
    moreButton.getStyleClass().add("more-button");
    moreButton.setOnAction(e -> toggleActionButtons());

    // Create container for action buttons (initially hidden)
    this.actionButtonsContainer = new HBox(10, saveAsTemplateButton, removeButton);
    this.actionButtonsContainer.setAlignment(Pos.CENTER_RIGHT);
    this.actionButtonsContainer.getStyleClass().add("action-buttons-container");
    this.actionButtonsContainer.setVisible(false);
    this.actionButtonsContainer.setManaged(false);

    // Create container for the more button
    this.moreButtonContainer = new HBox(moreButton);
    this.moreButtonContainer.setAlignment(Pos.CENTER_RIGHT);
    this.moreButtonContainer.getStyleClass().add("more-button-container");

    // Main button container that holds both the action buttons and more button
    HBox buttonContainer = new HBox(10);
    buttonContainer.getChildren().addAll(actionButtonsContainer, moreButtonContainer);
    buttonContainer.setAlignment(Pos.CENTER_RIGHT);
    buttonContainer.setPadding(new Insets(5, 0, 0, 0));
    buttonContainer.getStyleClass().add("button-container");

    // Initialize event handlers and set up visibility-based activation
    initializeEventHandlers();

    // Add the grid and button to the VBox
    getChildren().addAll(grid, buttonContainer);

    // Set up visibility listener to activate/deactivate mouse listeners
    setupVisibilityListener();

    // Apply caching for better performance
    applyLayoutCaching();
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
   * Handles the action of saving the exercise as a template. Checks for existing templates with the
   * same name and asks for confirmation before overwriting. Saves the exercise to the storage
   * service and shows a confirmation message.
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
        notificationService.showConfirmation(
            Messages.getString("exercise.overwriteDialogTitle"),
            Messages.getString("exercise.overwriteDialogMessage", exerciseName),
            () -> {
              // User confirmed, create a new exercise with the existing ID
              log.info(
                  Messages.getString("exercise.overwriteConfirmed"), exerciseName);
              try {
                TrainingExercise newExercise =
                    new TrainingExercise(
                        existingExerciseId.get(),
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getDuration(),
                        exercise.getSets(),
                        exercise.isBallBucket());

                planStorageService.saveExercise(newExercise);
                log.info(Messages.getString("exercise.saveSuccess"), newExercise.getName());

                // Show success message
                notificationService.showSuccess(
                    Messages.getString("notification.exercise.saved.title"), 
                    Messages.getString("notification.exercise.saved.text"));
              } catch (IOException e) {
                log.error(Messages.getString("exercise.saveError"), e);

                // Show error message
                notificationService.showError(
                    Messages.getString("notification.exercise.saveFailed.title"),
                    Messages.getString("notification.exercise.saveFailed.text", e.getMessage()));
              }
            },
            () -> {
              // User canceled, abort save operation
              log.info(Messages.getString("exercise.overwriteCanceled"), exerciseName);
            });
        return;
      }

      planStorageService.saveExercise(exerciseToSave);
      log.info("Exercise saved as template successfully: {}", exerciseToSave.getName());

      // Show success message
      notificationService.showSuccess(
          "Template Saved", "The exercise was successfully saved as a template.");
    } catch (IOException e) {
      log.error("Error saving exercise as template", e);

      // Show error message
      notificationService.showError(
          "Error Saving", "Saving as template has failed. An error occurred: " + e.getMessage());
    }
  }

  /**
   * Handles the action of removing the exercise. If a callback is set, it will be called with the
   * exercise after confirmation.
   */
  private void handleRemove() {
    // Show confirmation notification using NotificationService
    notificationService.showConfirmation(
        "Remove Exercise",
        "Are you sure you want to remove this exercise? This action cannot be undone.",
        () -> {
          // If user confirmed, call the callback
          if (onRemoveCallback != null) {
            onRemoveCallback.accept(exercise);
          }
        },
        () -> {
          // User canceled, do nothing
        });
  }


  /**
   * Toggles the visibility of the action buttons when the "More" button is clicked. This is
   * primarily for touch devices where hover is not available.
   */
  private void toggleActionButtons() {
    boolean isVisible = actionButtonsContainer.isVisible();
    actionButtonsContainer.setVisible(!isVisible);
    actionButtonsContainer.setManaged(!isVisible);
    moreButtonContainer.setVisible(isVisible);
    moreButtonContainer.setManaged(isVisible);
  }

  /**
   * Initializes the event handlers for mouse events but does not attach them. This allows for
   * on-demand activation of listeners.
   */
  private void initializeEventHandlers() {
    // Create mouse entered handler
    mouseEnteredHandler =
        e -> {
          actionButtonsContainer.setVisible(true);
          actionButtonsContainer.setManaged(true);
          moreButtonContainer.setVisible(false);
          moreButtonContainer.setManaged(false);
        };

    // Create mouse exited handler
    mouseExitedHandler =
        e -> {
          if (!actionButtonsContainer.isHover()) {
            actionButtonsContainer.setVisible(false);
            actionButtonsContainer.setManaged(false);
            moreButtonContainer.setVisible(true);
            moreButtonContainer.setManaged(true);
          }
        };

    // Create visibility change listener
    visibilityListener =
        (obs, oldValue, newValue) -> {
          if (newValue) {
            // Component became visible, activate listeners if not already active
            activateListeners();
          } else {
            // Component became invisible, deactivate listeners
            deactivateListeners();
          }
        };
  }

  /**
   * Sets up a listener to monitor visibility changes and activate/deactivate mouse listeners
   * accordingly.
   */
  private void setupVisibilityListener() {
    // Listen for visibility changes
    visibleProperty().addListener(visibilityListener);

    // Initial activation based on current visibility
    if (isVisible()) {
      activateListeners();
    }
  }

  /** Activates the mouse event listeners if they're not already active. */
  private void activateListeners() {
    if (!listenersActive) {
      setOnMouseEntered(mouseEnteredHandler);
      setOnMouseExited(mouseExitedHandler);
      listenersActive = true;
      log.debug("Activated mouse listeners for ExerciseControl: {}", exercise.getName());
    }
  }

  /** Deactivates the mouse event listeners if they're active. */
  private void deactivateListeners() {
    if (listenersActive) {
      setOnMouseEntered(null);
      setOnMouseExited(null);
      listenersActive = false;
      log.debug("Deactivated mouse listeners for ExerciseControl: {}", exercise.getName());
    }
  }

  /**
   * Cleans up all listeners to prevent memory leaks. This should be called when the control is no
   * longer needed.
   */
  public void cleanup() {
    // Remove all listeners
    deactivateListeners();
    visibleProperty().removeListener(visibilityListener);
    log.debug("Cleaned up all listeners for ExerciseControl: {}", exercise.getName());
  }

  /**
   * Applies caching hints to improve layout performance. This reduces the need for frequent layout
   * calculations.
   */
  private void applyLayoutCaching() {
    // Set cache hint to SPEED for the entire control
    setCache(true);
    setCacheHint(CacheHint.SPEED);

    // Cache the bounds to avoid recalculating layout
    setSnapToPixel(true);

    // Apply caching to child nodes that don't change frequently
    for (Node child : getChildren()) {
      if (child instanceof GridPane) {
        GridPane grid = (GridPane) child;
        grid.setCache(true);
        grid.setCacheHint(CacheHint.SPEED);
      } else if (child instanceof HBox) {
        HBox box = (HBox) child;
        box.setCache(true);
        box.setCacheHint(CacheHint.SPEED);
      }
    }

    log.debug("Applied layout caching to ExerciseControl: {}", exercise.getName());
  }
}
