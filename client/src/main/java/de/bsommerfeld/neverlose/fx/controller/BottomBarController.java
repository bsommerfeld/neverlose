package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import java.awt.Desktop;
import java.net.URI;
import javafx.application.Platform;
import javafx.fxml.FXML;

/** Controller for the bottom bar of the application. Provides branding information. */
@View
public class BottomBarController {

  private static final LogFacade log = LogFacadeFactory.getLogger();

  @FXML
  private void handleSommerfeldLink() {
    try {
      String githubUrl = Messages.getString("url.github");
      // Use the appropriate method based on the platform
      if (Platform.isFxApplicationThread()) {
        // Try to use Desktop API first
        Desktop.getDesktop().browse(new URI(githubUrl));
      } else {
        // Fallback to Runtime exec
        Runtime.getRuntime().exec("xdg-open " + githubUrl);
      }
    } catch (Exception e) {
      log.error(Messages.getString("error.url.openFailed", e.getMessage()), e);
    }
  }
}
