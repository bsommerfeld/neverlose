package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.messages.Messages;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;

/** Controller for the home view that provides centralized access to main functions. */
@View
public class HomeViewController {

    private static final LogFacade log = LogFacadeFactory.getLogger();
    private final NotificationService notificationService;
    private final ViewProvider viewProvider;

    @Inject
    public HomeViewController(ViewProvider viewProvider, NotificationService notificationService) {
        this.viewProvider = viewProvider;
        this.notificationService = notificationService;
    }

    /** Handles the "Show Plans" button click event. Navigates to the plan list view. */
    @FXML
    private void handleShowPlans() {
        log.debug(Messages.getString("log.debug.showPlansClicked"));

        /*
         * We need to refresh the plans here so that the recent changes (or initial plans)
         * are loaded correctly. If we don't do that any plan you want to open initially
         * would turn into a new plan.
         *
         * The same is (probably) to be expected when saving a plan and reopening it.
         * So we need to update the cache.
         */
        viewProvider.triggerViewChange(PlanListViewController.class, PlanListViewController::refreshPlans);
    }

    /**
     * Handles the "New Plan" button click event. Creates a new training plan and opens it in the editor.
     */
    @FXML
    private void handleNewPlan() {
        log.debug(Messages.getString("log.debug.newPlanClicked"));
        TrainingPlan newPlan = new TrainingPlan(
                Messages.getString("general.defaultPlanName"),
                Messages.getString("general.defaultPlanDescription"));
        viewProvider.triggerViewChange(TrainingPlanEditorController.class, controller -> controller.setTrainingPlan(newPlan));

        // Show a success notification
        notificationService.showSuccess(
                Messages.getString("ui.message.planCreated.title"),
                Messages.getString("ui.message.planCreated.text"));
    }
}
