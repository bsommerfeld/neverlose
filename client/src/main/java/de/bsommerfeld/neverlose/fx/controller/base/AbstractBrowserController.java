package de.bsommerfeld.neverlose.fx.controller.base;

import de.bsommerfeld.neverlose.fx.util.DialogUtils;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstract base controller for browser overlays that display available templates.
 * Allows users to select a template to add to their plan or delete existing templates.
 *
 * @param <S> the type of summary (e.g., UnitSummary, ExerciseSummary)
 * @param <T> the type of item (e.g., TrainingUnit, TrainingExercise)
 * @param <C> the type of card controller (e.g., TemplateCardController, ExerciseCardController)
 */
public abstract class AbstractBrowserController<S, T, C> {

    protected final LogFacade log = LogFacadeFactory.getLogger();
    protected final PlanStorageService planStorageService;

    @FXML
    protected BorderPane rootPane;

    @FXML
    protected FlowPane templatesContainer;

    protected Consumer<T> templateSelectedCallback;

    /**
     * Constructor for Guice injection.
     *
     * @param planStorageService the service for loading and managing templates
     */
    protected AbstractBrowserController(PlanStorageService planStorageService) {
        this.planStorageService = planStorageService;
    }

    /**
     * Initializes the controller after FXML fields are injected.
     */
    @FXML
    protected void initialize() {
        loadTemplates();
    }

    /**
     * Sets the callback to be called when a template is selected.
     *
     * @param callback the callback function that accepts a template item
     */
    public void setTemplateSelectedCallback(Consumer<T> callback) {
        this.templateSelectedCallback = callback;
    }

    /**
     * Loads all available templates and displays them in the UI.
     */
    protected void loadTemplates() {
        templatesContainer.getChildren().clear();

        try {
            List<S> templates = loadTemplateSummaries();

            if (templates.isEmpty()) {
                // Show a message when no templates are available
                Label noTemplatesLabel = new Label(getNoTemplatesMessage());
                noTemplatesLabel.getStyleClass().add("no-templates-message");
                templatesContainer.getChildren().add(noTemplatesLabel);
                return;
            }

            // Create a card for each template
            for (S template : templates) {
                addTemplateCard(template);
            }

        } catch (IOException e) {
            log.error("Error loading templates", e);
            DialogUtils.showAlert(
                Alert.AlertType.ERROR,
                "Error Loading",
                "The templates could not be loaded.",
                "An error occurred: " + e.getMessage(),
                rootPane);
        }
    }

    /**
     * Adds a template card to the UI for the given template summary.
     *
     * @param template the template summary to display
     */
    protected void addTemplateCard(S template) {
        try {
            // Load the card FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(getCardFxmlPath()));
            Parent cardRoot = loader.load();

            // Get the controller and set up the card
            C cardController = loader.getController();
            setupCardController(cardController, template);

            // Add the card to the container
            templatesContainer.getChildren().add(cardRoot);

        } catch (IOException e) {
            log.error("Error creating template card for {}", getTemplateName(template), e);
        }
    }

    /**
     * Handles the selection of a template.
     *
     * @param templateId the ID of the selected template
     */
    protected void handleSelectTemplate(UUID templateId) {
        if (templateSelectedCallback == null) {
            log.warn("Template selected but no callback is set");
            return;
        }

        try {
            Optional<T> itemOpt = loadTemplateItem(templateId);

            if (itemOpt.isPresent()) {
                T item = itemOpt.get();
                log.info("Template selected: {}", getItemName(item));

                // Call the callback with the loaded item
                templateSelectedCallback.accept(item);

                // Close the dialog
                DialogUtils.closeDialog(rootPane);
            } else {
                log.warn("Selected template not found: {}", templateId);
                DialogUtils.showAlert(
                    Alert.AlertType.WARNING,
                    "Template Not Found",
                    null,
                    "The selected template could not be loaded.",
                    rootPane);
            }
        } catch (IOException e) {
            log.error("Error loading template {}", templateId, e);
            DialogUtils.showAlert(
                Alert.AlertType.ERROR,
                "Error Loading",
                "The template could not be loaded.",
                "An error occurred: " + e.getMessage(),
                rootPane);
        }
    }

    /**
     * Handles the deletion of a template.
     *
     * @param templateId the ID of the template to delete
     */
    protected void handleDeleteTemplate(UUID templateId) {
        // Show confirmation dialog
        Optional<ButtonType> result = DialogUtils.showConfirmationDialog(
            getDeleteDialogTitle(),
            "Do you really want to delete this template?",
            "This action cannot be undone.",
            rootPane);

        // If user confirmed, delete the template
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = deleteTemplate(templateId);

                if (deleted) {
                    log.info("Template deleted: {}", templateId);

                    // Reload the templates to update the UI
                    loadTemplates();
                } else {
                    log.warn("Template not found for deletion: {}", templateId);
                    DialogUtils.showAlert(
                        Alert.AlertType.WARNING,
                        "Template Not Found",
                        null,
                        "The template to be deleted could not be found.",
                        rootPane);
                }
            } catch (IOException e) {
                log.error("Error deleting template {}", templateId, e);
                DialogUtils.showAlert(
                    Alert.AlertType.ERROR,
                    "Error Deleting",
                    "The template could not be deleted.",
                    "An error occurred: " + e.getMessage(),
                    rootPane);
            }
        }
    }

    /**
     * Gets the message to display when no templates are available.
     *
     * @return the no templates message
     */
    protected abstract String getNoTemplatesMessage();

    /**
     * Gets the path to the FXML file for the card.
     *
     * @return the card FXML path
     */
    protected abstract String getCardFxmlPath();

    /**
     * Gets the title for the delete confirmation dialog.
     *
     * @return the delete dialog title
     */
    protected abstract String getDeleteDialogTitle();

    /**
     * Gets the name of a template summary.
     *
     * @param template the template summary
     * @return the name of the template
     */
    protected abstract String getTemplateName(S template);

    /**
     * Gets the name of a template item.
     *
     * @param item the template item
     * @return the name of the item
     */
    protected abstract String getItemName(T item);

    /**
     * Loads all template summaries from the storage service.
     *
     * @return a list of template summaries
     * @throws IOException if an I/O error occurs
     */
    protected abstract List<S> loadTemplateSummaries() throws IOException;

    /**
     * Loads a template item by its ID.
     *
     * @param templateId the ID of the template to load
     * @return an Optional containing the loaded item, or empty if not found
     * @throws IOException if an I/O error occurs
     */
    protected abstract Optional<T> loadTemplateItem(UUID templateId) throws IOException;

    /**
     * Deletes a template by its ID.
     *
     * @param templateId the ID of the template to delete
     * @return true if the template was deleted, false if not found
     * @throws IOException if an I/O error occurs
     */
    protected abstract boolean deleteTemplate(UUID templateId) throws IOException;

    /**
     * Sets up a card controller with a template summary.
     *
     * @param cardController the card controller to set up
     * @param template the template summary to display
     */
    protected abstract void setupCardController(C cardController, S template);
}