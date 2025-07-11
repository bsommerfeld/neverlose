package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;

/** Controller for the home view that provides centralized access to main functions. */
@View
public class HomeViewController {

  private static final LogFacade log = LogFacadeFactory.getLogger();
  private final NotificationService notificationService;
  private NeverLoseMetaController metaController;

  @Inject
  public HomeViewController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Sets a reference to the meta controller for navigation.
   *
   * @param metaController the meta controller
   */
  public void setMetaController(NeverLoseMetaController metaController) {
    this.metaController = metaController;
  }

  /** Handles the "Show Plans" button click event. Navigates to the plan list view. */
  @FXML
  private void handleShowPlans() {
    log.debug(Messages.getString("log.debug.showPlansClicked"));
    if (metaController != null) {
      metaController.showPlanListView();
    } else {
      log.error(Messages.getString("log.error.noMetaController"));
    }
  }

  /**
   * Handles the "New Plan" button click event. Creates a new training plan and opens it in the
   * editor.
   */
  @FXML
  private void handleNewPlan() {
    log.debug(Messages.getString("log.debug.newPlanClicked"));
    if (metaController != null) {
      TrainingPlan newPlan = new TrainingPlan(
          Messages.getString("general.defaultPlanName"), 
          Messages.getString("general.defaultPlanDescription"));
      metaController.showTrainingPlanEditor(newPlan);

      // Show a success notification
      notificationService.showSuccess(
          Messages.getString("ui.message.planCreated.title"), 
          Messages.getString("ui.message.planCreated.text"));
    } else {
      log.error(Messages.getString("log.error.noMetaControllerForPlan"));
    }
  }
}
