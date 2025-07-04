package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.fxml.FXML;

@View
public class TopBarController {

  private static final LogFacade log = LogFacadeFactory.getLogger();

  private final ViewProvider viewProvider;

  @Inject
  public TopBarController(ViewProvider viewProvider) {
    this.viewProvider = viewProvider;
  }

  /** Handles the home button click event. Navigates to the home view. */
  @FXML
  private void handleHomeButton() {
    log.debug("Home button clicked");
    viewProvider.triggerViewChange(
        NeverLoseMetaController.class, NeverLoseMetaController::showHomeView);
  }
}
