package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

@View
public class TopBarController {

  private static final LogFacade log = LogFacadeFactory.getLogger();

  private final ViewProvider viewProvider;
  private PlanListViewController planListViewController;

  @FXML private Button homeButton;

  @Inject
  public TopBarController(ViewProvider viewProvider) {
    this.viewProvider = viewProvider;
  }

  @FXML
  private void initialize() {
    // No initialization needed
  }

  /**
   * Sets a reference to the plan list view controller for filtering.
   *
   * @param planListViewController the plan list view controller
   */
  public void setPlanListViewController(PlanListViewController planListViewController) {
    this.planListViewController = planListViewController;
  }


  /**
   * Handles the home button click event.
   * Navigates to the home view.
   */
  @FXML
  private void handleHomeButton() {
    log.debug("Home button clicked");
    viewProvider.triggerViewChange(NeverLoseMetaController.class, controller -> {
      controller.showHomeView();
    });
  }
}
