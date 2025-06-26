package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
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
     * Creates a new TrainingPlan object and opens the TrainingPlanEditor view.
     */
    @FXML
    private void handleNewPlan() {
        // Create a new TrainingPlan object
        TrainingPlan newPlan = new TrainingPlan("New Training Plan", "Description");

        // Show the TrainingPlanEditor with the new plan
        viewProvider.triggerViewChange(NeverLoseMetaController.class, controller -> {
            controller.showTrainingPlanEditor(newPlan);
        });
    }
}
