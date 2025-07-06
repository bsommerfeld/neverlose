package de.bsommerfeld.neverlose.fx.controller.base;

import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.messages.MessagesResourceBundle;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.checkerframework.checker.units.qual.C;

/**
 * Abstract base controller for browser overlays that display available templates. Allows users to
 * select a template to add to their plan or delete existing templates.
 *
 * @param <S> the type of summary (e.g., UnitSummary, ExerciseSummary)
 * @param <T> the type of item (e.g., TrainingUnit, TrainingExercise)
 * @param <C> the type of card controller (e.g., TemplateCardController, ExerciseCardController)
 */
public abstract class AbstractBrowserController<S, T, C> {

  protected final LogFacade log = LogFacadeFactory.getLogger();
  protected final PlanStorageService planStorageService;
  protected final NotificationService notificationService;

  @FXML protected BorderPane rootPane;

  @FXML protected FlowPane templatesContainer;

  protected Consumer<T> templateSelectedCallback;

  /**
   * Constructor for Guice injection.
   *
   * @param planStorageService the service for loading and managing templates
   * @param notificationService the service for displaying notifications
   */
  protected AbstractBrowserController(
      PlanStorageService planStorageService, NotificationService notificationService) {
    this.planStorageService = planStorageService;
    this.notificationService = notificationService;
  }

  /** Initializes the controller after FXML fields are injected. */
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

  /** Loads all available templates and displays them in the UI. */
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
      log.error(Messages.getString("log.plan.loadFailed"), e);
      notificationService.showError(
          Messages.getString("error.template.loadFailed.title"),
          Messages.getString("error.template.loadFailed.text", e.getMessage()));
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
      FXMLLoader loader = new FXMLLoader(getClass().getResource(getCardFxmlPath()));
      // Set the resource bundle for internationalization
      ResourceBundle resourceBundle = new MessagesResourceBundle();
      loader.setResources(resourceBundle);
      Parent cardRoot = loader.load();

      // Get the controller and set up the card
      C cardController = loader.getController();
      setupCardController(cardController, template);

      // Add the card to the container
      templatesContainer.getChildren().add(cardRoot);

    } catch (IOException e) {
      log.error(Messages.getString("error.template.createFailed", getTemplateName(template)), e);
    }
  }

  /**
   * Handles the selection of a template.
   *
   * @param templateId the ID of the selected template
   */
  protected void handleSelectTemplate(UUID templateId) {
    if (templateSelectedCallback == null) {
      log.warn(Messages.getString("log.error.noCallback"));
      return;
    }

    try {
      Optional<T> itemOpt = loadTemplateItem(templateId);

      if (itemOpt.isPresent()) {
        T item = itemOpt.get();
        log.info(Messages.getString("log.template.selected", getItemName(item)));

        // Call the callback with the loaded item
        templateSelectedCallback.accept(item);

        // Close the window
        close();
      } else {
        log.warn(Messages.getString("log.template.notFound", templateId));
        notificationService.showWarning(
            Messages.getString("error.template.notFound.title"),
            Messages.getString("error.template.notFound.text"));
      }
    } catch (IOException e) {
      log.error(Messages.getString("log.template.notFound", templateId), e);
      notificationService.showError(
          Messages.getString("error.template.loadFailed.single.title"),
          Messages.getString("error.template.loadFailed.single.text", e.getMessage()));
    }
  }

  private void close() {
    Node node = rootPane;
    if (node != null && node.getScene() != null && node.getScene().getWindow() != null) {
      Stage stage = (Stage) node.getScene().getWindow();
      stage.close();
    }
  }

  /**
   * Handles the deletion of a template.
   *
   * @param templateId the ID of the template to delete
   */
  protected void handleDeleteTemplate(UUID templateId) {
    // Show confirmation dialog
    notificationService.showConfirmation(
        getDeleteDialogTitle(),
        Messages.getString("dialog.delete.template.message"),
        () -> {
          try {
            boolean deleted = deleteTemplate(templateId);

            if (deleted) {
              log.info(Messages.getString("log.template.deleted", templateId));

              // Reload the templates to update the UI
              loadTemplates();
            } else {
              log.warn(Messages.getString("log.template.notFoundForDeletion", templateId));
              notificationService.showWarning(
                  Messages.getString("error.template.notFoundForDeletion.title"),
                  Messages.getString("error.template.notFoundForDeletion.text"));
            }
          } catch (IOException e) {
            log.error(Messages.getString("log.template.notFoundForDeletion", templateId), e);
            notificationService.showError(
                Messages.getString("error.template.deleteFailed.title"),
                Messages.getString("error.template.deleteFailed.text", e.getMessage()));
          }
        },
        null);
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
