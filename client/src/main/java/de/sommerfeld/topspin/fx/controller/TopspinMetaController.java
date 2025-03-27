package de.sommerfeld.topspin.fx.controller;

import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

/**
 * Controller for the meta view.
 */
@View
public class TopspinMetaController {

    private final ViewProvider viewProvider;

    @FXML
    private HBox topBarPlaceholder;

    @FXML
    private HBox bottomBarPlaceholder;

    public TopspinMetaController() {
        this.viewProvider = new ViewProvider(); // TODO: Injection
    }

    @FXML
    private void initialize() {
        loadTopBar();
        loadBottomBar();
    }

    private void loadTopBar() {
        Parent topBar = viewProvider.requestView(TopBarController.class).parent();
        topBarPlaceholder.getChildren().add(topBar);
    }

    private void loadBottomBar() {
        Parent bottomBar = viewProvider.requestView(BottomBarController.class).parent();
        bottomBarPlaceholder.getChildren().add(bottomBar);
    }
}
