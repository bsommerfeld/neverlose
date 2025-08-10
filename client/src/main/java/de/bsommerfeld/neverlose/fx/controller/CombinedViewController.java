package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Combined view that places the plan list (left) and the editor (right) side-by-side. It embeds the search controls of
 * the list in the left header and the save/export controls of the editor in the right header. Selecting a plan in the
 * list updates the editor on the right, avoiding any view navigation.
 */
@View
public class CombinedViewController {

    private final ViewProvider viewProvider;

    @FXML
    private HBox leftHeaderBox;

    @FXML
    private StackPane leftContent;

    @FXML
    private HBox rightHeaderBox;

    @FXML
    private StackPane rightContent;

    private PlanListViewController planListController;
    private TrainingPlanEditorController editorController;

    @Inject
    public CombinedViewController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
        // Load and embed PlanList on the left
        ViewWrapper<PlanListViewController> listWrapper = viewProvider.requestView(PlanListViewController.class);
        planListController = listWrapper.controller();
        Node listNode = listWrapper.parent();
        leftContent.getChildren().setAll(listNode);

        // Load and embed Editor on the right
        ViewWrapper<TrainingPlanEditorController> editorWrapper = viewProvider.requestView(TrainingPlanEditorController.class);
        editorController = editorWrapper.controller();
        Node editorNode = editorWrapper.parent();
        rightContent.getChildren().setAll(editorNode);

        // Wire selection: open selected plan in the embedded editor instead of navigating
        planListController.setOnPlanSelected(this::showPlanInEditor);

        // Place the dynamic controls from both views into the headers
        embedHeaderControls();
    }

    private void showPlanInEditor(TrainingPlan plan) {
        if (editorController != null && plan != null) {
            editorController.setTrainingPlan(plan);
        }
    }

    private void embedHeaderControls() {
        // Left: search components from plan list
        if (planListController instanceof ControlsProvider providerLeft) {
            leftHeaderBox.getChildren().add(providerLeft.controlsContainer());
        }
        // Right: save/export from editor
        if (editorController instanceof ControlsProvider providerRight) {
            rightHeaderBox.getChildren().add(providerRight.controlsContainer());
        }
    }
}
