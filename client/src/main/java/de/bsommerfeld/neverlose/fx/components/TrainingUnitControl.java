package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.fx.controller.ExerciseTemplateBrowserController;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.messages.MessagesResourceBundle;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * A custom JavaFX control that represents a TrainingUnit in the WYSIWYG editor. This control allows for direct editing
 * of the TrainingUnit's properties and manages a list of ExerciseControl components for the exercises in the unit.
 */
public class TrainingUnitControl extends VBox {

    private final LogFacade log = LogFacadeFactory.getLogger();
    private final TrainingUnit trainingUnit;
    private final ComboBox<Weekday> weekdayComboBox;
    private final VBox exercisesContainer;
    private final PlanStorageService planStorageService;
    private final Consumer<TrainingUnit> saveAsTemplateCallback;
    private final Consumer<TrainingUnit> onRemoveCallback;
    private final Button showMoreButton;
    // Placeholder for empty exercises list
    private final Label placeholderLabel;
    // Toggle elements for collapsible functionality
    private final Label toggleArrow;
    private final VBox contentContainer; // Container for all collapsible elements
    private final NotificationService notificationService;
    private boolean showAllExercises = false;
    private boolean isExpanded = true; // Default state is expanded

    /**
     * Creates a new TrainingUnitControl for the specified TrainingUnit with a callback for saving as template.
     *
     * @param trainingUnit           the TrainingUnit to represent
     * @param planStorageService     the service for loading and saving templates
     * @param saveAsTemplateCallback callback to be called when the "Save as Template" button is clicked
     * @param onRemoveCallback       callback to be called when the "Remove" button is clicked
     * @param notificationService    service for showing notifications
     *
     * @throws IllegalArgumentException if trainingUnit, planStorageService, or notificationService is null
     */
    public TrainingUnitControl(
            TrainingUnit trainingUnit,
            PlanStorageService planStorageService,
            Consumer<TrainingUnit> saveAsTemplateCallback,
            Consumer<TrainingUnit> onRemoveCallback,
            NotificationService notificationService) {
        // Check for null required parameters
        if (trainingUnit == null) {
            throw new IllegalArgumentException("TrainingUnit cannot be null");
        }
        if (planStorageService == null) {
            throw new IllegalArgumentException("PlanStorageService cannot be null");
        }
        if (notificationService == null) {
            throw new IllegalArgumentException("NotificationService cannot be null");
        }

        this.trainingUnit = trainingUnit;
        this.planStorageService = planStorageService;
        this.saveAsTemplateCallback = saveAsTemplateCallback; // Can be null
        this.onRemoveCallback = onRemoveCallback; // Can be null
        this.notificationService = notificationService;

        // Configure the VBox
        setSpacing(10);
        setPadding(new Insets(15));
        getStyleClass().add("training-unit-control");

        // Create the header with toggle arrow, name field and weekday selector
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("unit-header");

        // Toggle arrow for collapsible functionality
        toggleArrow =
                new Label(Messages.getString("ui.button.expandToggle")); // Down arrow for expanded state
        toggleArrow.getStyleClass().add("toggle-arrow");
        toggleArrow.setStyle("-fx-cursor: hand;"); // Hand cursor to indicate it's clickable

        // Name field
        TextField nameField = new TextField(trainingUnit.getName());
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
        Button saveAsTemplateButton = new Button(Messages.getString("ui.button.save"));
        saveAsTemplateButton.getStyleClass().add("save-as-template-button");
        saveAsTemplateButton.setOnAction(e -> handleSaveAsTemplate());

        // Remove button (red X)
        Button removeButton = new Button(Messages.getString("ui.button.remove"));
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> handleRemove());

        header
                .getChildren()
                .addAll(toggleArrow, nameField, weekdayComboBox, saveAsTemplateButton, removeButton);

        // Description field
        TextField descriptionField = new TextField(trainingUnit.getDescription());
        descriptionField.getStyleClass().add("unit-description-field");
        descriptionField
                .textProperty()
                .addListener((obs, oldVal, newVal) -> trainingUnit.setDescription(newVal));

        // Container for exercises
        exercisesContainer = new VBox(10);
        exercisesContainer.getStyleClass().add("exercises-container");

        // Create placeholder for empty exercises list
        placeholderLabel = new Label(Messages.getString("ui.label.noExercises"));
        placeholderLabel.getStyleClass().add("placeholder-label");
        placeholderLabel.setAlignment(Pos.CENTER);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        placeholderLabel.setPadding(new Insets(20, 0, 20, 0));

        // Create the "Show more" button
        showMoreButton = new Button(Messages.getString("ui.button.showMore"));
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
        Button addExerciseButton = new Button(Messages.getString("ui.button.add"));
        addExerciseButton.getStyleClass().add("add-exercise-button");
        addExerciseButton.setOnAction(e -> handleAddExercise());

        // Add from template button
        Button addFromTemplateButton = new Button(Messages.getString("ui.button.load"));
        addFromTemplateButton.getStyleClass().add("add-from-template-button");
        addFromTemplateButton.setOnAction(e -> handleAddExerciseFromTemplate());

        HBox addExerciseContainer = new HBox(10, addExerciseButton, addFromTemplateButton);
        addExerciseContainer.setAlignment(Pos.CENTER);
        addExerciseContainer.getStyleClass().add("unit-footer-actions");

        // Create a container for all collapsible content
        contentContainer = new VBox(10);
        contentContainer.getStyleClass().add("unit-content");
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
                toggleArrow.setText(
                        Messages.getString("ui.button.expandToggle")); // Down arrow for expanded state
            } else {
                toggleArrow.setText(
                        Messages.getString("ui.button.collapseToggle")); // Right arrow for collapsed state
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
                new ExerciseControl(
                        exercise, planStorageService, notificationService, this::removeExercise);
        exercisesContainer.getChildren().add(exerciseControl);
        updateExercisesVisibility();
    }

    /**
     * Toggles the visibility of the content container. When collapsed, only the header is visible. When expanded, all
     * content is visible.
     */
    private void toggleContentVisibility() {
        isExpanded = !isExpanded;

        // Update the arrow icon based on the current state
        if (isExpanded) {
            toggleArrow.setText(
                    Messages.getString("ui.button.expandToggle")); // Down arrow for expanded state
        } else {
            toggleArrow.setText(
                    Messages.getString("ui.button.collapseToggle")); // Right arrow for collapsed state
        }

        // Update the visibility of the content container
        contentContainer.setVisible(isExpanded);
        contentContainer.setManaged(isExpanded);
    }

    /**
     * Updates the visibility of exercises based on the showAllExercises flag. Shows only the first three exercises if
     * showAllExercises is false, or all exercises if showAllExercises is true. This method is optimized to minimize
     * iterations over the children list.
     */
    private void updateExercisesVisibility() {
        if (exercisesContainer == null || placeholderLabel == null) {
            log.error("Exercise container or placeholder label is null");
            return;
        }

        try {
            List<Node> exercises = exercisesContainer.getChildren();
            if (exercises == null) {
                log.error("Exercise container children list is null");
                return;
            }

            // Calculate exercise count and handle placeholder visibility in a single pass
            int exerciseCount = 0;
            for (Node node : exercises) {
                if (node != placeholderLabel) {
                    exerciseCount++;
                }
            }

            // Handle placeholder visibility
            boolean hasExercises = exerciseCount > 0;
            placeholderLabel.setVisible(!hasExercises);
            placeholderLabel.setManaged(!hasExercises);

            // Early return if no exercises
            if (!hasExercises) {
                if (showMoreButton != null) {
                    showMoreButton.setVisible(false);
                    showMoreButton.setManaged(false);
                }
                return;
            }

            // Determine if we should show the "show more" button
            boolean showAllExercisesNow = showAllExercises || exerciseCount <= 3;
            if (showMoreButton != null) {
                showMoreButton.setVisible(!showAllExercisesNow && exerciseCount > 3);
                showMoreButton.setManaged(!showAllExercisesNow && exerciseCount > 3);
            }

            // Update exercise visibility in a single pass
            int visibleCount = 0;
            for (Node exercise : exercises) {
                // Skip the placeholder
                if (exercise == placeholderLabel) continue;

                boolean visible = showAllExercisesNow || visibleCount < 3;
                exercise.setVisible(visible);
                exercise.setManaged(visible);

                // Handle fade-out effect
                if (visibleCount == 2 && !showAllExercisesNow) {
                    exercise.getStyleClass().add("exercise-fade-out");
                } else {
                    exercise.getStyleClass().remove("exercise-fade-out");
                }

                visibleCount++;
            }
        } catch (Exception e) {
            log.error("Error updating exercise visibility: {}", e.getMessage(), e);
        }
    }

    /**
     * Shows a confirmation dialog before removing an exercise.
     *
     * @param exercise the exercise to potentially remove
     */
    private void removeExerciseWithConfirmation(TrainingExercise exercise) {
        // Show confirmation notification using NotificationService
        notificationService.showConfirmation(
                Messages.getString("exercise.removeDialogTitle"),
                Messages.getString("exercise.removeDialogMessage"),
                () -> {
                    // If user confirmed, remove the exercise
                    removeExercise(exercise);
                },
                () -> {
                    // User canceled, do nothing
                });
    }

    /**
     * Removes an exercise from the training unit and updates the UI.
     *
     * @param exercise the exercise to remove
     */
    private void removeExercise(TrainingExercise exercise) {
        // Remove the exercise from the training unit
        trainingUnit.getTrainingExercises().remove(exercise);

        // Find and remove the corresponding ExerciseControl
        ExerciseControl controlToRemove = null;
        for (Node node : exercisesContainer.getChildren()) {
            if (node instanceof ExerciseControl) {
                ExerciseControl control = (ExerciseControl) node;
                if (control.getExercise().getId().equals(exercise.getId())) {
                    controlToRemove = control;
                    break;
                }
            }
        }

        if (controlToRemove != null) {
            // Clean up listeners before removing
            controlToRemove.cleanup();
            exercisesContainer.getChildren().remove(controlToRemove);
        }

        // Check if we need to show the placeholder
        if (trainingUnit.getTrainingExercises().getAll().isEmpty()) {
            placeholderLabel.setVisible(true);
            placeholderLabel.setManaged(true);
        }

        // Update visibility of exercises
        updateExercisesVisibility();
    }

    /** Handles the action of adding a new exercise. */
    private void handleAddExercise() {
        // Create a new exercise with default values
        TrainingExercise newExercise =
                new TrainingExercise(
                        Messages.getString("unit.defaultExerciseName"),
                        Messages.getString("unit.defaultExerciseDescription"),
                        Messages.getString("unit.defaultExerciseDuration"),
                        3,
                        false);

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
     * Handles the action of saving the training unit as a template. If a callback is set, it will be called with the
     * training unit.
     */
    private void handleSaveAsTemplate() {
        if (saveAsTemplateCallback != null) {
            saveAsTemplateCallback.accept(trainingUnit);
        } else {
            log.warn(
                    "Save as template callback is null, cannot save unit as template: {}",
                    trainingUnit.getName());
        }
    }

    /**
     * Handles the action of removing the training unit. If a callback is set, it will be called with the training unit
     * after confirmation.
     */
    private void handleRemove() {
        if (notificationService == null) {
            log.error("NotificationService is null, cannot show confirmation dialog");
            return;
        }

        // Show confirmation notification using NotificationService
        notificationService.showConfirmation(
                Messages.getString("unit.removeDialogTitle"),
                Messages.getString("unit.removeDialogMessage"),
                () -> {
                    // If user confirmed, call the callback
                    if (onRemoveCallback != null) {
                        onRemoveCallback.accept(trainingUnit);
                    } else {
                        log.warn("Remove callback is null, cannot remove unit: {}", trainingUnit.getName());
                    }
                },
                () -> {
                    // User canceled, do nothing
                    log.debug("User canceled removal of training unit: {}", trainingUnit.getName());
                });
    }

    /**
     * Handles the action of adding an exercise from a template. Opens the ExerciseTemplateBrowser in a draggable
     * container within the main application window and adds the selected exercise to the training unit.
     * <p>
     * The template browser is displayed as a floating panel that can be moved around within the main window. It will
     * appear above the main content but below any notifications, allowing notifications to remain interactable while
     * the template browser is open.
     * <p>
     * The container is positioned in the center of the parent pane, regardless of the application window size. This is
     * achieved by immediately calculating and setting the position before adding the container to the parent.
     * <p>
     * When a template is selected, the exercise is added to the training unit and the template browser is automatically
     * closed (removed from the scene). Error handling is in place to prevent crashes during template selection.
     */
    private void handleAddExerciseFromTemplate() {
        try {
            // Load the exercise template browser view
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(Messages.getString("path.exerciseTemplateBrowser.fxml")));
            // Set the resource bundle for internationalization
            ResourceBundle resourceBundle = new MessagesResourceBundle();
            loader.setResources(resourceBundle);
            // Set the controller factory to create the controller with the PlanStorageService
            loader.setControllerFactory(
                    param -> new ExerciseTemplateBrowserController(planStorageService, notificationService));
            Parent root = loader.load();

            // Create a draggable container for the template browser
            DraggableContainer container = new DraggableContainer(root);
            container.setPrefSize(600, 400);
            container.setMaxSize(600, 400);

            // Get the controller and set the callback
            ExerciseTemplateBrowserController controller = loader.getController();
            controller.setTemplateSelectedCallback(exercise -> {
                try {
                    // Add the exercise to the training unit
                    addExerciseFromTemplate(exercise);

                    // Remove the container using its own method
                    container.removeFromParent();
                } catch (Exception e) {
                    log.error("Error handling template selection", e);
                    notificationService.showError(
                            Messages.getString("unit.errorSelectingTemplate"),
                            e.getMessage());
                }
            });

            // Find the root pane of the application
            Scene scene = getScene();
            if (scene != null) {
                // Get the root pane of the scene
                Parent rootNode = scene.getRoot();

                // Find a suitable parent pane to add the container to
                Pane parentPane = findSuitableParentPane(rootNode);

                if (parentPane != null) {
                    // Add the container to the parent pane
                    container.addToParent(parentPane);

                    // Apply stylesheets
                    container.getStylesheets().addAll(scene.getStylesheets());
                } else {
                    // Fallback to old behavior if no suitable parent pane is found
                    log.warn("No suitable parent pane found for draggable container, falling back to dialog");
                    showAsDialog(root);
                }
            } else {
                // Fallback to old behavior if no scene is available
                log.warn("No scene available for draggable container, falling back to dialog");
                showAsDialog(root);
            }

        } catch (IOException e) {
            log.error(Messages.getString("unit.errorOpeningBrowser"), e);
            notificationService.showError(
                    Messages.getString("unit.errorOpeningTitle"),
                    Messages.getString("unit.errorOpeningDetail", e.getMessage()));
        }
    }

    /**
     * Finds a suitable parent pane to add the draggable container to.
     * <p>
     * This method searches the scene graph for the centerContentPlaceholder, which is the preferred container for the
     * draggable template browser. If it can't find the centerContentPlaceholder, it will recursively search for any
     * suitable Pane in the scene graph. As a last resort, it will use the root pane of the scene.
     *
     * @param node the node to search from
     *
     * @return a suitable parent pane, or null if none is found
     */
    private Pane findSuitableParentPane(Node node) {
        // Try to find the centerContentPlaceholder
        if (node instanceof Pane) {
            // Look for the centerContentPlaceholder by ID
            for (Node child : ((Pane) node).getChildren()) {
                if (child.getId() != null && child.getId().equals("centerContentPlaceholder")) {
                    return (Pane) child;
                }
            }

            // If not found, recursively search children
            for (Node child : ((Pane) node).getChildren()) {
                Pane result = findSuitableParentPane(child);
                if (result != null) {
                    return result;
                }
            }

            // If still not found, use the root pane as a fallback
            return (Pane) node;
        }

        return null;
    }

    /**
     * Shows the template browser in a dialog (old behavior).
     * <p>
     * This is a fallback method that's used if we can't find a suitable parent pane to add the draggable container to.
     * It creates a separate modal dialog window for the template browser, which is the original behavior before the
     * implementation of the embedded draggable container.
     *
     * @param root the root node of the template browser
     */
    private void showAsDialog(Parent root) {
        // Create a new stage for the template browser
        Stage templateBrowserStage = new Stage();
        templateBrowserStage.setTitle(Messages.getString("exercise.templateBrowserTitle"));
        templateBrowserStage.initModality(Modality.APPLICATION_MODAL);
        templateBrowserStage.initOwner(getScene().getWindow());

        // Set the scene and show the stage
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().addAll(getScene().getStylesheets());
        templateBrowserStage.setScene(scene);
        templateBrowserStage.showAndWait();
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

        log.info(Messages.getString("unit.exerciseAdded", templateExercise.getName()));
    }
}
