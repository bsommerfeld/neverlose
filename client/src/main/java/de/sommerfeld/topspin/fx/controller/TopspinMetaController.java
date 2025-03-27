package de.sommerfeld.topspin.fx.controller;

import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
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
    private AnchorPane centerContentPlaceholder;

    @FXML
    private HBox bottomBarPlaceholder;

    public TopspinMetaController() {
        this.viewProvider = new ViewProvider(); // TODO: Injection
    }

    private static void setAnchor(Parent center) {
        AnchorPane.setTopAnchor(center, 0d);
        AnchorPane.setLeftAnchor(center, 0d);
        AnchorPane.setRightAnchor(center, 0d);
        AnchorPane.setBottomAnchor(center, 0d);
    }

    @FXML
    private void initialize() {
        loadTopBar();
        loadCenter();
        loadBottomBar();
    }

    private void loadTopBar() {
        Parent topBar = viewProvider.requestView(TopBarController.class).parent();
        topBarPlaceholder.getChildren().add(topBar);
    }

    private void loadCenter() {
        Parent center = viewProvider.requestView(TrainingPlanEditorController.class).parent();
        setAnchor(center);
        centerContentPlaceholder.getChildren().add(center);
    }

    private void loadBottomBar() {
        Parent bottomBar = viewProvider.requestView(BottomBarController.class).parent();
        bottomBarPlaceholder.getChildren().add(bottomBar);
    }
}
