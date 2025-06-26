package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Controller for the exercise template browser overlay that displays available training exercise templates.
 * Allows users to select a template to add to their unit or delete existing templates.
 */
@View
public class ExerciseTemplateBrowserController {

    private final LogFacade log = LogFacadeFactory.getLogger();
    private final PlanStorageService planStorageService;

    @FXML
    private BorderPane rootPane;

    @FXML
    private FlowPane templatesContainer;

    private Consumer<TrainingExercise> templateSelectedCallback;

    /**
     * Constructor for Guice injection.
     *
     * @param planStorageService the service for loading and managing templates
     */
    @Inject
    public ExerciseTemplateBrowserController(PlanStorageService planStorageService) {
        this.planStorageService = planStorageService;
    }

    /**
     * Initializes the controller after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        loadTemplates();
    }

    /**
     * Sets the callback to be called when a template is selected.
     *
     * @param callback the callback function that accepts a TrainingExercise
     */
    public void setTemplateSelectedCallback(Consumer<TrainingExercise> callback) {
        this.templateSelectedCallback = callback;
    }

    /**
     * Loads all available exercise templates and displays them in the UI.
     */
    private void loadTemplates() {
        templatesContainer.getChildren().clear();

        try {
            List<ExerciseSummary> templates = planStorageService.loadExerciseSummaries();

            if (templates.isEmpty()) {
                // Show a message when no templates are available
                javafx.scene.control.Label noTemplatesLabel = new javafx.scene.control.Label(
                    "No exercise templates available. Save exercises as templates to display them here.");
                noTemplatesLabel.getStyleClass().add("no-templates-message");
                templatesContainer.getChildren().add(noTemplatesLabel);
                return;
            }

            // Create a card for each template
            for (ExerciseSummary template : templates) {
                addTemplateCard(template);
            }

        } catch (IOException e) {
            log.error("Error loading exercise templates", e);
            showAlert(
                Alert.AlertType.ERROR,
                "Error Loading",
                "The exercise templates could not be loaded.",
                "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Adds a template card to the UI for the given template summary.
     *
     * @param template the template summary to display
     */
    private void addTemplateCard(ExerciseSummary template) {
        try {
            // Load the ExerciseCard.fxml
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/de/bsommerfeld/neverlose/fx/controller/ExerciseCard.fxml"));
            javafx.scene.Parent cardRoot = loader.load();

            // Get the controller and set up the card
            ExerciseCardController cardController = loader.getController();
            cardController.setTemplate(template);
            cardController.setOnDeleteAction(this::handleDeleteTemplate);
            cardController.setOnSelectAction(this::handleSelectTemplate);

            // Add the card to the container
            templatesContainer.getChildren().add(cardRoot);

        } catch (IOException e) {
            log.error("Error creating exercise template card for {}", template.name(), e);
        }
    }

    /**
     * Handles the selection of a template.
     *
     * @param templateId the ID of the selected template
     */
    private void handleSelectTemplate(UUID templateId) {
        if (templateSelectedCallback == null) {
            log.warn("Exercise template selected but no callback is set");
            return;
        }

        try {
            Optional<TrainingExercise> exerciseOpt = planStorageService.loadExercise(templateId);

            if (exerciseOpt.isPresent()) {
                TrainingExercise exercise = exerciseOpt.get();
                log.info("Exercise template selected: {}", exercise.getName());

                // Call the callback with the loaded exercise
                templateSelectedCallback.accept(exercise);

                // Close the dialog
                closeDialog();
            } else {
                log.warn("Selected exercise template not found: {}", templateId);
                showAlert(
                    Alert.AlertType.WARNING,
                    "Template Not Found",
                    null,
                    "The selected exercise template could not be loaded.");
            }
        } catch (IOException e) {
            log.error("Error loading exercise template {}", templateId, e);
            showAlert(
                Alert.AlertType.ERROR,
                "Error Loading",
                "The exercise template could not be loaded.",
                "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the deletion of a template.
     *
     * @param templateId the ID of the template to delete
     */
    private void handleDeleteTemplate(UUID templateId) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Exercise Template");
        confirmDialog.setHeaderText("Do you really want to delete this exercise template?");
        confirmDialog.setContentText("This action cannot be undone.");

        // Apply application stylesheet to the dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        if (rootPane.getScene() != null && rootPane.getScene().getRoot() != null) {
            dialogPane.getStylesheets().addAll(rootPane.getScene().getRoot().getStylesheets());
        }

        // Wait for user response
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();

        // If user confirmed, delete the template
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                boolean deleted = planStorageService.deleteExercise(templateId);

                if (deleted) {
                    log.info("Exercise template deleted: {}", templateId);

                    // Reload the templates to update the UI
                    loadTemplates();
                } else {
                    log.warn("Exercise template not found for deletion: {}", templateId);
                    showAlert(
                        Alert.AlertType.WARNING,
                        "Template Not Found",
                        null,
                        "The exercise template to be deleted could not be found.");
                }
            } catch (IOException e) {
                log.error("Error deleting exercise template {}", templateId, e);
                showAlert(
                    Alert.AlertType.ERROR,
                    "Error Deleting",
                    "The exercise template could not be deleted.",
                    "An error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
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
        if (rootPane.getScene() != null && rootPane.getScene().getRoot() != null) {
            dialogPane.getStylesheets().addAll(rootPane.getScene().getRoot().getStylesheets());
        }

        alert.showAndWait();
    }
}
