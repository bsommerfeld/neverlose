package de.sommerfeld.neverlose.fx.controller;

import de.sommerfeld.neverlose.fx.view.View;
import de.sommerfeld.neverlose.fx.viewmodel.TrainingPlanEditorViewModel;
import de.sommerfeld.neverlose.logger.LogFacade;
import de.sommerfeld.neverlose.logger.LogFacadeFactory;
import de.sommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;

import java.util.Objects;

@View
public class TrainingPlanEditorMetaController {

    private final LogFacade log = LogFacadeFactory.getLogger();

    private TrainingPlanEditorViewModel viewModel;

    @FXML
    private TrainingPlanEditorFormController editorFormController;
    @FXML
    private TrainingPlanEditorPreviewController editorPreviewController;

    @FXML
    public void initialize() {
        log.info("Initializing...");
        this.viewModel = new TrainingPlanEditorViewModel();

        Objects.requireNonNull(editorFormController, "Editor Form Controller not injected!");
        Objects.requireNonNull(editorPreviewController, "Editor Preview Controller not injected!");

        editorFormController.initViewModel(viewModel);
        editorPreviewController.initViewModel(viewModel);

        log.info("Initialization complete. ViewModel injected.");
    }

    /**
     * Loads a new TrainingPlan into the editor.
     * This is now the primary entry point for loading data.
     * It updates the central ViewModel, and the sub-views react via bindings/listeners.
     *
     * @param plan The new TrainingPlan to load.
     */
    public void setPlan(TrainingPlan plan) {
        log.info("Setting new plan...");
        Objects.requireNonNull(plan, "TrainingPlan cannot be null");
        Objects.requireNonNull(viewModel, "ViewModel must not be null when setting a plan.");
        viewModel.setTrainingPlan(plan);
        log.info("New plan set in ViewModel.");
    }


    /**
     * Call this method when the entire editor view is being closed or disposed of.
     * It delegates cleanup tasks to the sub-controllers.
     */
    public void cleanup() {
        log.info("Starting cleanup...");
        if (editorFormController != null) {
            editorFormController.cleanup();
        }
        if (editorPreviewController != null) {
            editorPreviewController.cleanupListeners();
        }
        if (viewModel != null) {
            // viewModel.dispose(); // Once ViewModel holds resources
        }
        log.info("Cleanup complete.");
    }

    public TrainingPlanEditorViewModel getViewModel() {
        return viewModel;
    }

    public TrainingPlanEditorFormController getEditorFormController() {
        return editorFormController;
    }

    public TrainingPlanEditorPreviewController getEditorPreviewController() {
        return editorPreviewController;
    }
}