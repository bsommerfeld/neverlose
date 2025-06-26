package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for the bottom bar of the application.
 * Provides functionality for creating a new training plan.
 */
@View
public class BottomBarController {

    private final ViewProvider viewProvider;

    @FXML
    private Button newPlanButton;

    @Inject
    public BottomBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    /**
     * Handles the action of creating a new training plan.
     * Opens the TrainingPlanEditor view.
     */
    @FXML
    private void handleNewPlan() {
        // Load the TrainingPlanEditor view into the center content area
        viewProvider.triggerViewChange(NeverLoseMetaController.class, controller -> {
            controller.loadCenter(TrainingPlanEditorController.class);
        });
    }
}
