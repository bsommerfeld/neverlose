package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

/** Controller for the plan card view that displays a single training plan. */
@View
public class PlanCardController {
  private static final LogFacade log = LogFacadeFactory.getLogger();

  @FXML private Label planNameLabel;

  @FXML private Button deleteButton;

  private PlanSummary plan;
  private PlanStorageService planStorageService;
  private PlanListViewController parentController;
  private NotificationService notificationService;

  /**
   * Sets the plan to display in this card.
   *
   * @param plan the plan summary to display
   */
  public void setPlan(PlanSummary plan) {
    this.plan = plan;
    planNameLabel.setText(plan.name());
  }

  /**
   * Gets the plan displayed in this card.
   *
   * @return the plan summary
   */
  public PlanSummary getPlan() {
    return plan;
  }

  /**
   * Sets the plan storage service for this controller.
   *
   * @param planStorageService the plan storage service
   */
  public void setPlanStorageService(PlanStorageService planStorageService) {
    this.planStorageService = planStorageService;
  }

  /**
   * Sets the parent controller for this card.
   *
   * @param parentController the parent controller
   */
  public void setParentController(PlanListViewController parentController) {
    this.parentController = parentController;
  }

  /**
   * Sets the notification service for this controller.
   *
   * @param notificationService the notification service
   */
  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Handles the delete button action. Shows a confirmation dialog and deletes the plan if
   * confirmed.
   */
  @FXML
  private void handleDeleteButtonAction() {
    if (plan == null || planStorageService == null || parentController == null) {
      log.error(Messages.getString("log.error.cannotDeletePlan"));
      showErrorAlert(Messages.getString("error.general.title"), Messages.getString("error.plan.cannotDelete.text"));
      return;
    }

    if (notificationService == null) {
      log.error(Messages.getString("log.error.cannotShowConfirmation"));
      return;
    }

    // Show confirmation dialog using NotificationService
    notificationService.showConfirmation(
        Messages.getString("dialog.delete.plan.title"),
        Messages.getString("dialog.delete.plan.message", plan.name()),
        () -> {
          // This code runs when the user confirms
          try {
            boolean deleted = planStorageService.deletePlan(plan.identifier());
            if (deleted) {
              log.info(Messages.getString("log.plan.deleted", plan.name()));
              // Refresh the plan list view
              parentController.refreshPlans();
            } else {
              log.warn(Messages.getString("log.plan.deleteFailed", plan.name()));
              showErrorAlert(Messages.getString("error.general.title"), Messages.getString("error.plan.cannotDelete.text"));
            }
          } catch (IOException e) {
            log.error(Messages.getString("log.error.deletePlan", plan.name()), e);
            showErrorAlert(
                Messages.getString("error.general.title"), Messages.getString("error.general.text", e.getMessage()));
          }
        },
        null // No action on cancel
    );
  }

  /**
   * Shows an error alert with the given title and message.
   *
   * @param title the alert title
   * @param message the alert message
   */
  private void showErrorAlert(String title, String message) {
    if (notificationService != null) {
      notificationService.showError(title, message);
    } else {
      log.error(Messages.getString("log.error.cannotShowError", title, message));
    }
  }
}
