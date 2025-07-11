package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.export.ExportService;
import de.bsommerfeld.neverlose.fx.components.TrainingUnitControl;
import de.bsommerfeld.neverlose.fx.controller.TopBarController.Alignment;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.messages.MessagesResourceBundle;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import de.bsommerfeld.neverlose.plan.components.Weekday;
import de.bsommerfeld.neverlose.plan.components.collection.TrainingExercises;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for the TrainingPlan WYSIWYG editor view. This controller manages the editing of a TrainingPlan object in
 * a document-like interface.
 */
@View
public class TrainingPlanEditorController implements ControlsProvider {

    private final LogFacade log = LogFacadeFactory.getLogger();

    private final PlanStorageService planStorageService;
    private final ExportService exportService;
    private final NotificationService notificationService;
    // Map to store the expanded state of each unit, keyed by the unit's ID
    private final Map<UUID, Boolean> unitExpandedStates = new HashMap<>();
    // Timeline for throttling scroll events to improve performance
    private final Timeline scrollThrottleTimeline = new Timeline();
    // Flag to track if a scroll update is pending
    private final AtomicBoolean scrollUpdatePending = new AtomicBoolean(false);
    @FXML
    private BorderPane rootPane;
    @FXML
    private TextField planNameField;
    @FXML
    private TextField planDescriptionField;
    @FXML
    private VBox trainingUnitsContainer;
    private TrainingPlan trainingPlan;
    private ScrollPane editorScrollPane;

    // Buttons for the TopBar
    private Button saveButton;
    private Button exportButton;
    private HBox buttonsContainer;

    /**
     * Constructor for Guice injection.
     *
     * @param planStorageService  the service for saving and loading training plans
     * @param exportService       the service for exporting training plans to PDF
     * @param notificationService the service for displaying notifications
     */
    @Inject
    public TrainingPlanEditorController(
            PlanStorageService planStorageService,
            ExportService exportService,
            NotificationService notificationService) {
        this.planStorageService = planStorageService;
        this.exportService = exportService;
        this.notificationService = notificationService;
    }

    @Override
    public Alignment alignment() {
        return Alignment.RIGHT;
    }

    @Override
    public Node[] controls() {
        if (buttonsContainer != null) {
            log.debug("Providing action buttons for top bar with RIGHT alignment");
            return new Node[]{buttonsContainer};
        }

        throw new IllegalStateException("ButtonsContainer cannot be null.");
    }

    /** Initializes the controller after FXML fields are injected. */
    @FXML
    private void initialize() {
        // Initialize with an empty training plan if none is set
        if (trainingPlan == null) {
            trainingPlan =
                    new TrainingPlan(
                            Messages.getString("general.defaultPlanName"),
                            Messages.getString("general.defaultPlanDescription"));
        }

        // Create the action buttons for the TopBar
        createActionButtons();

        // Set up scroll throttling
        setupScrollThrottling();

        // Bind the training plan properties to the UI
        updateUIFromModel();
    }

    /** Creates the action buttons that will be registered with the TopBar. */
    private void createActionButtons() {
        // Create buttons container
        buttonsContainer = new HBox();
        buttonsContainer.setSpacing(10);
        buttonsContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Create save button
        saveButton = new Button(Messages.getString("ui.button.save"));
        saveButton.getStyleClass().add("editor-action-button");
        saveButton.setOnAction(event -> handleSave());

        // Create export button
        exportButton = new Button(Messages.getString("fxml.trainingPlanEditor.export"));
        exportButton.getStyleClass().add("editor-action-button");
        exportButton.setOnAction(event -> handleExport());

        // Add buttons to container
        buttonsContainer.getChildren().addAll(saveButton, exportButton);
    }

    /**
     * Sets up scroll event throttling to improve performance. This reduces layout updates to 60 FPS (16ms interval)
     * during scrolling.
     */
    private void setupScrollThrottling() {
        // Configure the timeline for throttling (60 FPS = 16ms interval)
        scrollThrottleTimeline
                .getKeyFrames()
                .add(
                        new KeyFrame(
                                Duration.millis(16),
                                event -> {
                                    // Only update if there's a pending update
                                    if (scrollUpdatePending.getAndSet(false)) {
                                        // Apply any pending layout updates
                                        rootPane.layout();
                                        log.debug(Messages.getString("log.debug.scrollUpdate"));
                                    }
                                }));
        scrollThrottleTimeline.setCycleCount(Timeline.INDEFINITE);

        // Find the ScrollPane in the scene graph after the scene is fully initialized
        rootPane
                .sceneProperty()
                .addListener(
                        (obs, oldScene, newScene) -> {
                            if (newScene != null) {
                                // Use Platform.runLater to ensure the scene is fully initialized
                                Platform.runLater(() -> findScrollPane(rootPane));
                            }
                        });
    }

    /**
     * Recursively searches for the ScrollPane in the scene graph.
     *
     * @param node the starting node for the search
     */
    private void findScrollPane(Node node) {
        if (node instanceof ScrollPane) {
            editorScrollPane = (ScrollPane) node;
            setupScrollListener(editorScrollPane);
            log.debug(Messages.getString("log.debug.scrollPaneFound"));
            return;
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                findScrollPane(child);
                if (editorScrollPane != null) {
                    return; // ScrollPane found, stop searching
                }
            }
        }
    }

    /**
     * Sets up the scroll event listener on the ScrollPane.
     *
     * @param scrollPane the ScrollPane to add the listener to
     */
    private void setupScrollListener(ScrollPane scrollPane) {
        scrollPane.setOnScroll(
                event -> {
                    // Mark that an update is pending
                    if (!scrollUpdatePending.getAndSet(true)) {
                        // Start the timeline if it's not already running
                        if (!scrollThrottleTimeline.getStatus().equals(Timeline.Status.RUNNING)) {
                            scrollThrottleTimeline.play();
                        }
                    }
                    // Don't consume the event to allow normal scrolling
                });
    }

    /**
     * Sets the training plan to be edited and updates the UI.
     *
     * @param trainingPlan the training plan to edit
     */
    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
        updateUIFromModel();
    }

    /** Updates the UI components with the current state of the training plan model. */
    private void updateUIFromModel() {
        if (trainingPlan != null) {
            planNameField.setText(trainingPlan.getName());
            planDescriptionField.setText(trainingPlan.getDescription());

            // Store the expanded state of each unit before clearing
            for (Node node : trainingUnitsContainer.getChildren()) {
                if (node instanceof TrainingUnitControl) {
                    TrainingUnitControl unitControl = (TrainingUnitControl) node;
                    TrainingUnit unit = unitControl.getTrainingUnit();
                    // Store whether this unit is expanded or collapsed
                    unitExpandedStates.put(unit.getId(), unitControl.isExpanded());
                }
            }

            // Clear existing units
            trainingUnitsContainer.getChildren().clear();

            // Check if there are any training units
            List<TrainingUnit> units = trainingPlan.getTrainingUnits().getAll();
            if (units.isEmpty()) {
                // Add placeholder when there are no units
                addEmptyPlaceholder();
            } else {
                // Add each training unit to the container
                for (TrainingUnit unit : units) {
                    addTrainingUnitToUI(unit);
                }
            }

            // Add the "Add Unit" button at the end
            addAddUnitButton();
        }
    }

    /**
     * Adds a training unit to the UI.
     *
     * @param unit the training unit to add
     */
    private void addTrainingUnitToUI(TrainingUnit unit) {
        TrainingUnitControl unitControl =
                new TrainingUnitControl(
                        unit,
                        planStorageService,
                        this::saveUnitAsTemplate,
                        this::removeTrainingUnit,
                        notificationService);

        // Apply the stored expanded state if available
        Boolean expandedState = unitExpandedStates.get(unit.getId());
        if (expandedState != null) {
            unitControl.setExpanded(expandedState);
        }

        trainingUnitsContainer.getChildren().add(unitControl);
    }

    /**
     * Removes a training unit from the training plan and updates the UI.
     *
     * @param unit the training unit to remove
     */
    private void removeTrainingUnit(TrainingUnit unit) {
        // Remove the unit from the training plan
        trainingPlan.getTrainingUnits().remove(unit);

        // Update the UI
        updateUIFromModel();
    }

    /**
     * Saves a training unit as a template.
     *
     * @param unit the training unit to save as a template
     */
    private void saveUnitAsTemplate(TrainingUnit unit) {
        try {
            // Check if a unit with the same name already exists
            String unitName = unit.getName();
            Optional<UUID> existingUnitId = planStorageService.findUnitIdByName(unitName);

            if (existingUnitId.isPresent() && !existingUnitId.get().equals(unit.getId())) {
                // A unit with this name exists but has a different ID
                // We need to create a final copy of the unit for use in the lambda
                final TrainingUnit finalUnit = unit;

                // Show confirmation dialog before overwriting
                notificationService.showConfirmation(
                        Messages.getString("dialog.overwrite.template.title"),
                        Messages.getString("dialog.overwrite.template.message", unitName),
                        () -> {
                            // This code runs when the user confirms
                            log.info(Messages.getString("log.template.overwriteConfirmed", unitName));

                            // Use the existing ID for the template unit
                            TrainingUnit updatedUnit =
                                    new TrainingUnit(
                                            existingUnitId.get(),
                                            unitName,
                                            finalUnit.getDescription(),
                                            finalUnit.getWeekday(),
                                            finalUnit.getTrainingExercises());

                            // Continue with the save operation using the updated unit
                            saveUnitAsTemplateInternal(updatedUnit);
                        },
                        () -> {
                            // This code runs when the user cancels
                            log.info(Messages.getString("log.template.overwriteCanceled", unitName));
                            // No further action needed
                        });

                // Return early since we'll continue in the callback if confirmed
                return;
            }

            // If we get here, there's no conflict, so continue with the save operation
            saveUnitAsTemplateInternal(unit);
        } catch (Exception e) {
            log.error(Messages.getString("log.template.saveFailed"), e);

            // Show error message
            showStyledAlert(
                    Alert.AlertType.ERROR,
                    Messages.getString("error.template.saveFailed.title"),
                    Messages.getString("error.template.saveFailed.text"),
                    Messages.getString("error.template.saveFailed.detail", e.getMessage()));
        }
    }

    /**
     * Internal method to save a training unit as a template after any confirmation dialogs.
     *
     * @param unit the training unit to save as a template
     */
    private void saveUnitAsTemplateInternal(TrainingUnit unit) {
        try {
            // Create a new unit with the same ID to ensure it overwrites any existing template with the
            // same ID
            TrainingUnit templateUnit =
                    new TrainingUnit(
                            unit.getId(),
                            unit.getName(),
                            unit.getDescription(),
                            unit.getWeekday(),
                            new TrainingExercises());

            // Copy all exercises from the original unit to the template unit
            for (TrainingExercise exercise : unit.getTrainingExercises().getAll()) {
                // Create a copy of each exercise
                TrainingExercise templateExercise =
                        new TrainingExercise(
                                exercise.getName(),
                                exercise.getDescription(),
                                exercise.getDuration(),
                                exercise.getSets(),
                                exercise.isBallBucket());

                // Add it to the template unit
                templateUnit.getTrainingExercises().add(templateExercise);
            }

            // Save the template unit
            planStorageService.saveUnit(templateUnit);
            log.info(Messages.getString("log.template.saved", templateUnit.getName()));

            // Show success message
            showStyledAlert(
                    Alert.AlertType.INFORMATION,
                    Messages.getString("notification.template.saved.title"),
                    null,
                    Messages.getString("notification.template.saved.text"));
        } catch (Exception e) {
            log.error(Messages.getString("log.template.saveFailed"), e);

            // Show error message
            showStyledAlert(
                    Alert.AlertType.ERROR,
                    Messages.getString("error.template.saveFailed.title"),
                    Messages.getString("error.template.saveFailed.text"),
                    Messages.getString("error.template.saveFailed.detail", e.getMessage()));
        }
    }

    /** Adds a placeholder to the training units container when there are no units. */
    private void addEmptyPlaceholder() {
        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(10);
        placeholder.setPadding(new Insets(20));
        placeholder.getStyleClass().add("empty-placeholder");

        Text message = new Text(Messages.getString("ui.label.noTrainingUnits"));
        message.getStyleClass().add("placeholder-text");

        placeholder.getChildren().add(message);
        trainingUnitsContainer.getChildren().add(placeholder);
    }

    /** Adds the "Add Unit" button to the container. */
    private void addAddUnitButton() {
        Button addButton = new Button(Messages.getString("ui.button.add"));
        addButton.getStyleClass().add("add-unit-button");
        addButton.setOnAction(event -> handleAddUnit());

        Button addFromTemplate = new Button(Messages.getString("ui.button.load"));
        addFromTemplate.getStyleClass().add("add-from-template-button");
        addFromTemplate.setOnAction(event -> handleAddFromTemplate());

        // Create an HBox to center the button
        HBox buttonContainer = new HBox(addButton, addFromTemplate);
        buttonContainer.setSpacing(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setPadding(new Insets(0, 0, 0, 15));

        trainingUnitsContainer.getChildren().add(buttonContainer);
    }

    /** Handles the action of adding a new training unit. */
    @FXML
    private void handleAddUnit() {
        // Save current UI state to model
        updateModelFromUI();

        // Create a new training unit with default values
        TrainingUnit newUnit =
                new TrainingUnit(
                        Messages.getString("general.defaultUnitName"),
                        Messages.getString("general.defaultUnitDescription"),
                        Weekday.MONDAY);

        // Add it to the training plan
        trainingPlan.getTrainingUnits().add(newUnit);

        // Update the UI
        updateUIFromModel();
    }

    /** Handles the action of adding a unit from a template. */
    @FXML
    private void handleAddFromTemplate() {
        try {
            // Create the controller instance with the required dependencies
            TemplateBrowserController controller =
                    new TemplateBrowserController(planStorageService, notificationService);

            // Load the template browser view
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource("/de/bsommerfeld/neverlose/fx/controller/TemplateBrowser.fxml"));

            // Set the resource bundle for internationalization
            ResourceBundle resourceBundle = new MessagesResourceBundle();
            loader.setResources(resourceBundle);

            // Set the controller before loading
            loader.setController(controller);
            Parent root = loader.load();

            // Set the callback
            controller.setTemplateSelectedCallback(this::addTemplateToTrainingPlan);

            // Create a new stage for the template browser
            Stage templateBrowserStage = new Stage();
            templateBrowserStage.setTitle(Messages.getString("ui.title.templateBrowser"));
            templateBrowserStage.initModality(Modality.APPLICATION_MODAL);
            templateBrowserStage.initOwner(rootPane.getScene().getWindow());

            // Set the scene and show the stage
            Scene scene = new Scene(root, 600, 400);
            scene.getStylesheets().addAll(rootPane.getScene().getStylesheets());
            templateBrowserStage.setScene(scene);
            templateBrowserStage.showAndWait();

        } catch (Exception e) {
            log.error("Error opening template browser", e);
            showStyledAlert(
                    Alert.AlertType.ERROR,
                    Messages.getString("error.browser.openFailed.title"),
                    Messages.getString("error.browser.openFailed.text"),
                    Messages.getString("error.browser.openFailed.detail", e.getMessage()));
        }
    }

    /**
     * Adds a template unit to the training plan.
     *
     * @param templateUnit the template unit to add
     */
    private void addTemplateToTrainingPlan(TrainingUnit templateUnit) {
        // Save current UI state to model
        updateModelFromUI();

        // Create a new unit with a new ID
        TrainingUnit newUnit =
                new TrainingUnit(
                        templateUnit.getName(), templateUnit.getDescription(), templateUnit.getWeekday());

        // Copy all exercises from the template to the new unit
        for (TrainingExercise exercise : templateUnit.getTrainingExercises().getAll()) {
            // Create a copy of each exercise
            TrainingExercise newExercise =
                    new TrainingExercise(
                            exercise.getName(),
                            exercise.getDescription(),
                            exercise.getDuration(),
                            exercise.getSets(),
                            exercise.isBallBucket());

            // Add it to the new unit
            newUnit.getTrainingExercises().add(newExercise);
        }

        // Add the new unit to the training plan
        trainingPlan.getTrainingUnits().add(newUnit);

        // Update the UI
        updateUIFromModel();

        log.info(Messages.getString("log.template.added", templateUnit.getName()));
    }

    /** Handles the save button action. */
    @FXML
    private void handleSave() {
        updateModelFromUI();

        try {
            // Check if a plan with the same name already exists (but with a different ID)
            String planName = trainingPlan.getName();
            UUID existingPlanId = findExistingPlanByName(planName);

            if (existingPlanId != null && !existingPlanId.equals(trainingPlan.getId())) {
                // A plan with this name exists but has a different ID
                // We need to create final copies of the variables for use in the lambda
                final String finalPlanName = planName;
                final UUID finalExistingPlanId = existingPlanId;
                final TrainingPlan finalTrainingPlan = trainingPlan;

                // Show confirmation dialog before overwriting
                notificationService.showConfirmation(
                        Messages.getString("dialog.overwrite.plan.title"),
                        Messages.getString("dialog.overwrite.plan.message", planName),
                        () -> {
                            // This code runs when the user confirms
                            log.info(Messages.getString("log.plan.overwriteConfirmed", finalPlanName));

                            // Update the existing plan
                            TrainingPlan updatedPlan =
                                    new TrainingPlan(
                                            finalExistingPlanId,
                                            finalPlanName,
                                            finalTrainingPlan.getDescription(),
                                            finalTrainingPlan.getTrainingUnits());

                            // Continue with the save operation using the updated plan
                            savePlanInternal(updatedPlan);
                        },
                        () -> {
                            // This code runs when the user cancels
                            log.info(Messages.getString("log.plan.overwriteCanceled", finalPlanName));
                            // No further action needed
                        });

                // Return early since we'll continue in the callback if confirmed
                return;
            }

            // If we get here, there's no conflict, so continue with the save operation
            savePlanInternal(trainingPlan);
        } catch (Exception e) {
            log.error("Error saving training plan", e);

            // Show error message
            showStyledAlert(
                    Alert.AlertType.ERROR,
                    "Save Error",
                    "The save operation failed.",
                    "An error occurred while saving the plan: " + e.getMessage());
        }
    }

    /**
     * Internal method to save a training plan after any confirmation dialogs.
     *
     * @param plan the training plan to save
     */
    private void savePlanInternal(TrainingPlan plan) {
        try {
            String identifier = planStorageService.savePlan(plan);
            log.info(Messages.getString("log.plan.saved", identifier));

            // Show success message
            showStyledAlert(
                    Alert.AlertType.INFORMATION,
                    Messages.getString("notification.plan.saved.title"),
                    null,
                    Messages.getString("notification.plan.saved.text"));
        } catch (Exception e) {
            log.error(Messages.getString("log.error.savePlan"), e);

            // Show error message
            showStyledAlert(
                    Alert.AlertType.ERROR,
                    Messages.getString("error.plan.saveFailed.title"),
                    Messages.getString("error.plan.saveFailed.text"),
                    Messages.getString("error.plan.saveFailed.detail", e.getMessage()));
        }
    }

    /**
     * Finds an existing plan by name.
     *
     * @param name the name to search for
     *
     * @return the UUID of the existing plan, or null if no plan with that name exists
     */
    private UUID findExistingPlanByName(String name) {
        try {
            List<PlanSummary> summaries = planStorageService.loadPlanSummaries();
            return summaries.stream()
                    .filter(summary -> summary.name().equals(name))
                    .map(PlanSummary::identifier)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("Error loading plan summaries", e);
            return null;
        }
    }

    /** Handles the export button action by showing a file chooser and exporting the plan. */
    @FXML
    private void handleExport() {
        updateModelFromUI();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Messages.getString("ui.title.exportDialog"));

        String initialFileName = trainingPlan.getName().replaceAll("\\s+", "_") + ".pdf";
        fileChooser.setInitialFileName(initialFileName);

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(
                        Messages.getString("ui.title.exportFilter"),
                        Messages.getString("ui.title.exportFilterExtension"));
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportService.export(trainingPlan, file);
                log.info(Messages.getString("log.plan.exported", file.getAbsolutePath()));

                showStyledAlertWithLink(
                        Alert.AlertType.INFORMATION,
                        Messages.getString("notification.export.success.title"),
                        null,
                        Messages.getString("notification.export.success.text"),
                        Messages.getString("notification.export.openFile"),
                        file);

            } catch (Exception e) {
                log.error(Messages.getString("log.error.exportPlan"), e);

                showStyledAlert(
                        Alert.AlertType.ERROR,
                        Messages.getString("error.export.failed.title"),
                        Messages.getString("error.export.failed.text"),
                        Messages.getString("error.export.failed.detail", e.getMessage()));
            }
        } else {
            log.info(Messages.getString("log.plan.exportCanceled"));
        }
    }

    /** Updates the training plan model with the current state of the UI. */
    private void updateModelFromUI() {
        if (trainingPlan != null) {
            trainingPlan.setName(planNameField.getText());
            trainingPlan.setDescription(planDescriptionField.getText());

            // The training units are updated directly by their controls
        }
    }

    /**
     * Creates and shows a notification with the given parameters.
     *
     * @param alertType   the type of the alert
     * @param title       the title of the alert
     * @param headerText  the header text (can be null)
     * @param contentText the content text
     */
    private void showStyledAlert(
            Alert.AlertType alertType, String title, String headerText, String contentText) {
        String displayTitle = title;
        String displayContent =
                headerText != null && !headerText.isEmpty() ? headerText + "\n" + contentText : contentText;

        switch (alertType) {
            case INFORMATION:
                notificationService.showInfo(displayTitle, displayContent);
                break;
            case WARNING:
                notificationService.showWarning(displayTitle, displayContent);
                break;
            case ERROR:
                notificationService.showError(displayTitle, displayContent);
                break;
            case CONFIRMATION:
                // For confirmation, we should use a different method
                notificationService.showInfo(displayTitle, displayContent);
                break;
            default:
                notificationService.showInfo(displayTitle, displayContent);
                break;
        }
    }

    /**
     * Creates and shows a notification with a button to open a file.
     *
     * @param alertType   the type of the alert
     * @param title       the title of the alert
     * @param headerText  the header text (can be null)
     * @param contentText the content text to display
     * @param linkText    the text to display for the button
     * @param file        the file to open when the button is clicked
     */
    private void showStyledAlertWithLink(
            Alert.AlertType alertType,
            String title,
            String headerText,
            String contentText,
            String linkText,
            File file) {
        String displayTitle = title;
        String displayContent =
                headerText != null && !headerText.isEmpty() ? headerText + "\n" + contentText : contentText;

        // Use a confirmation notification with custom button text
        notificationService.showConfirmation(
                displayTitle,
                displayContent,
                linkText,
                Messages.getString("ui.button.close"),
                () -> {
                    // Action when the "Open File" button is clicked
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        log.error(Messages.getString("log.error.openFile", file.getAbsolutePath()), ex);
                        showStyledAlert(
                                Alert.AlertType.ERROR,
                                Messages.getString("error.file.openFailed.title"),
                                null,
                                Messages.getString("error.file.openFailed.text", ex.getMessage()));
                    }
                },
                null // No action needed for the Close button
        );
    }
}
