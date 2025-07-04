package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;

/** Controller for the home view that provides centralized access to main functions. */
@View
public class HomeViewController {

  private static final LogFacade log = LogFacadeFactory.getLogger();

  private NeverLoseMetaController metaController;

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
    log.debug("Show Plans button clicked");
    if (metaController != null) {
      metaController.showPlanListView();
    } else {
      log.error("Meta controller not set, cannot navigate to plan list view");
    }
  }

  /**
   * Handles the "New Plan" button click event. Creates a new training plan and opens it in the
   * editor.
   */
  @FXML
  private void handleNewPlan() {
    log.debug("New Plan button clicked");
    if (metaController != null) {
      TrainingPlan newPlan = new TrainingPlan("New Training Plan", "Description");
      metaController.showTrainingPlanEditor(newPlan);
    } else {
      log.error("Meta controller not set, cannot create new plan");
    }
  }
}
