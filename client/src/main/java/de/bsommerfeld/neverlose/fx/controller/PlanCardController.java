package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * Controller for the plan card view that displays a single training plan.
 */
@View
public class PlanCardController {
    private static final LogFacade log = LogFacadeFactory.getLogger();

    @FXML
    private ImageView planIcon;

    @FXML
    private Label planNameLabel;

    @FXML
    private Button deleteButton;

    private PlanSummary plan;
    private PlanStorageService planStorageService;
    private PlanListViewController parentController;

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

    /**
     * Sets the plan storage service for this controller.
     *
     * @param planStorageService the plan storage service
     */
    public void setPlanStorageService(PlanStorageService planStorageService) {
        this.planStorageService = planStorageService;
    }

    /**
     * Sets the parent controller for this card.
     *
     * @param parentController the parent controller
     */
    public void setParentController(PlanListViewController parentController) {
        this.parentController = parentController;
    }

    /**
     * Handles the delete button action.
     * Shows a confirmation dialog and deletes the plan if confirmed.
     */
    @FXML
    private void handleDeleteButtonAction() {
        if (plan == null || planStorageService == null || parentController == null) {
            log.error("Cannot delete plan: plan, planStorageService, or parentController is null");
            showErrorAlert("Error", "The plan could not be deleted.");
            return;
        }

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Plan");
        confirmDialog.setHeaderText("Delete Plan \"" + plan.name() + "\"?");
        confirmDialog.setContentText("Do you really want to delete this plan? This action cannot be made undo.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = planStorageService.deletePlan(plan.identifier());
                if (deleted) {
                    log.info("Successfully deleted plan: {}", plan.name());
                    // Refresh the plan list view
                    parentController.refreshPlans();
                } else {
                    log.warn("Failed to delete plan: {}", plan.name());
                    showErrorAlert("Error", "The plan could not be deleted.");
                }
            } catch (IOException e) {
                log.error("Error deleting plan: {}", plan.name(), e);
                showErrorAlert("Error", "An error occured while trying to delete the plan: " + e.getMessage());
            }
        }
    }

    /**
     * Shows an error alert with the given title and message.
     *
     * @param title the alert title
     * @param message the alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
