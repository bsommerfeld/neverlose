package de.sommerfeld.topspin.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.topspin.fx.view.View;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import de.sommerfeld.topspin.plan.TrainingPlan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

@View
public class BottomBarController {

    private final ViewProvider viewProvider;

    @Inject
    public BottomBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    public void onNewPlan(ActionEvent actionEvent) {
        viewProvider.triggerViewChange(TrainingPlanEditorController.class, p -> p.setPlan(new TrainingPlan("New Plan", "")));
    }

    @FXML
    public void onShowAllPlans(ActionEvent actionEvent) {
    }
}
