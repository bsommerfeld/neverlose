package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
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

    @FXML
    private SplitPane rootSplitPane;

    @FXML
    private BorderPane leftPane;

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

        // Keep the left panel width stable across window resizes, while allowing manual resizing via the divider
        final SplitPane.Divider divider = rootSplitPane.getDividers().isEmpty() ? null : rootSplitPane.getDividers().get(0);
        if (divider != null) {
            final double min = leftPane.getMinWidth() > 0 ? leftPane.getMinWidth() : 150.0;
            final double[] leftWidthPx = new double[]{Math.max(240.0, min)};
            final boolean[] adjusting = new boolean[]{false};

            Runnable applyPositionFromWidth = () -> {
                double total = rootSplitPane.getWidth();
                if (total <= 0) return;
                double pos = Math.max(0.0, Math.min(1.0, leftWidthPx[0] / total));
                adjusting[0] = true;
                divider.setPosition(pos);
                adjusting[0] = false;
            };

            // Initialize once layout passes have occurred
            Platform.runLater(() -> {
                // If we can compute an ideal width from the plan list, use it
                try {
                    if (planListController != null) {
                        double ideal = planListController.computeIdealListWidth();
                        // Clamp the initial width so a single long plan name cannot expand the pane excessively
                        double maxInitial = 300.0; // sensible upper bound for initial left pane width
                        if (ideal > 0) {
                            leftWidthPx[0] = Math.max(min, Math.min(ideal, maxInitial));
                        }
                    }
                } catch (Exception ignored) {
                }
                applyPositionFromWidth.run();
            });

            // Keep constant pixel width on SplitPane width changes
            rootSplitPane.widthProperty().addListener((obs, oldW, newW) -> {
                applyPositionFromWidth.run();
            });

            // When user drags the divider, update the target pixel width
            divider.positionProperty().addListener((obs, oldP, newP) -> {
                if (adjusting[0]) return;
                double total = rootSplitPane.getWidth();
                leftWidthPx[0] = Math.max(min, newP.doubleValue() * total);
            });
        }
    }

    private void showPlanInEditor(TrainingPlan plan) {
        if (editorController != null && plan != null) {
            editorController.setTrainingPlan(plan);
        }
    }

    private void embedHeaderControls() {
        // Left: search components from plan list
        if (planListController instanceof ControlsProvider providerLeft) {
            Node leftControls = providerLeft.controlsContainer();
            if (leftControls instanceof javafx.scene.layout.Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
            }
            HBox.setHgrow(leftControls, javafx.scene.layout.Priority.ALWAYS);
            leftHeaderBox.getChildren().add(leftControls);
        }
        // Right: save/export from editor
        if (editorController instanceof ControlsProvider providerRight) {
            Node rightControls = providerRight.controlsContainer();
            rightHeaderBox.getChildren().add(rightControls);
        }
    }
}
