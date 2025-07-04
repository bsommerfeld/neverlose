package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.view.View;
import java.awt.Desktop;
import java.net.URI;
import javafx.application.Platform;
import javafx.fxml.FXML;

/** Controller for the bottom bar of the application. Provides branding information. */
@View
public class BottomBarController {

  private static final String GITHUB_URL = "https://github.com/bsommerfeld";

  @FXML
  private void handleSommerfeldLink() {
    try {
      // Use the appropriate method based on the platform
      if (Platform.isFxApplicationThread()) {
        // Try to use Desktop API first
        Desktop.getDesktop().browse(new URI(GITHUB_URL));
      } else {
        // Fallback to Runtime exec
        Runtime.getRuntime().exec("xdg-open " + GITHUB_URL);
      }
    } catch (Exception e) {
      System.err.println("Could not open URL: " + e.getMessage());
    }
  }
}
