package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import javafx.fxml.FXML;

/**
 * Controller for the bottom bar of the application.
 * Provides branding information.
 */
@View
public class BottomBarController {

    private final ViewProvider viewProvider;

    @Inject
    public BottomBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
        // No initialization needed for now
    }
}
