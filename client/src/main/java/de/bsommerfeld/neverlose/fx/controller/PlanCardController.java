package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * Controller for the plan card view that displays a single training plan.
 */
@View
public class PlanCardController {

    @FXML
    private ImageView planIcon;
    
    @FXML
    private Label planNameLabel;
    
    private PlanSummary plan;
    
    /**
     * Sets the plan to display in this card.
     *
     * @param plan the plan summary to display
     */
    public void setPlan(PlanSummary plan) {
        this.plan = plan;
        planNameLabel.setText(plan.name());
    }
    
    /**
     * Gets the plan displayed in this card.
     *
     * @return the plan summary
     */
    public PlanSummary getPlan() {
        return plan;
    }
}