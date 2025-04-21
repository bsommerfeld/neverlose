package de.sommerfeld.topspin.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

@View
public class TopBarController {

    private final ViewProvider viewProvider;

    @FXML
    private HBox searchComponentPlaceholder;

    @Inject
    public TopBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
    }
}
