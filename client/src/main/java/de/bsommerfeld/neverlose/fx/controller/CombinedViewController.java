package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.bootstrap.NeverloseConfig;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;

/**
 * Combined view that places the plan list (left) and the editor (right) side-by-side. It embeds the search controls of
 * the list in the left header and the save/export controls of the editor in the right header. Selecting a plan in the
 * list updates the editor on the right, avoiding any view navigation.
 */
@View
public class CombinedViewController {

    private final ViewProvider viewProvider;
    private final NeverloseConfig neverloseConfig;

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

    private PlanListViewController planListController;
    private TrainingPlanEditorController editorController;

    @Inject
    public CombinedViewController(ViewProvider viewProvider, NeverloseConfig neverloseConfig) {
        this.viewProvider = viewProvider;
        this.neverloseConfig = neverloseConfig;
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

        // Persist and restore divider using pixel-based left width (primary) and ratio (fallback)
        final SplitPane.Divider divider = rootSplitPane.getDividers().isEmpty() ? null : rootSplitPane.getDividers().get(0);
        if (divider != null) {
            Runnable applySavedPosition = () -> {
                try {
                    double leftPx = neverloseConfig.getCombinedLeftWidthPx();
                    double total = rootSplitPane.getWidth();
                    double pos;
                    if (leftPx > 0.0 && total > 0.0) {
                        pos = leftPx / total;
                    } else {
                        pos = neverloseConfig.getCombinedDividerPosition();
                    }
                    if (Double.isNaN(pos) || Double.isInfinite(pos)) {
                        pos = 0.3;
                    }
                    pos = Math.max(0.0, Math.min(1.0, pos));
                    divider.setPosition(pos);
                } catch (Exception ignored) {
                }
            };

            // Initialize once layout passes have occurred
            Platform.runLater(() -> {
                applySavedPosition.run();

                // Ensure the divider position is applied once the window is actually shown
                if (rootSplitPane.getScene() != null) {
                    var scene = rootSplitPane.getScene();
                    if (scene.getWindow() != null) {
                        scene.getWindow().addEventHandler(WindowEvent.WINDOW_SHOWN, e -> applySavedPosition.run());
                    } else {
                        scene.windowProperty().addListener((o, oldW, newW) -> {
                            if (newW != null) {
                                newW.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> applySavedPosition.run());
                            }
                        });
                    }
                } else {
                    rootSplitPane.sceneProperty().addListener((o, oldS, newS) -> {
                        if (newS != null) {
                            if (newS.getWindow() != null) {
                                newS.getWindow().addEventHandler(WindowEvent.WINDOW_SHOWN, e -> applySavedPosition.run());
                            } else {
                                newS.windowProperty().addListener((oo, ow, nw) -> {
                                    if (nw != null) {
                                        nw.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> applySavedPosition.run());
                                    }
                                });
                            }
                        }
                    });
                }

                // Keep left panel width consistent when window is resized
                final boolean[] dragging = new boolean[]{false};
                rootSplitPane.widthProperty().addListener((o, oldV, newV) -> {
                    if (neverloseConfig.getCombinedLeftWidthPx() > 0.0 && !dragging[0]) {
                        applySavedPosition.run();
                    }
                });

                // Persist divider when the user releases the mouse after dragging
                rootSplitPane.lookupAll(".split-pane-divider").forEach(div -> {
                    div.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> dragging[0] = true);
                    div.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
                        try {
                            double pos = divider.getPosition();
                            double leftWidth = leftContent.getWidth();
                            if (!Double.isNaN(pos) && !Double.isInfinite(pos)) {
                                if (neverloseConfig != null) {
                                    // Save both pixel width and normalized position as fallback
                                    if (!Double.isNaN(leftWidth) && !Double.isInfinite(leftWidth) && leftWidth > 0.0) {
                                        neverloseConfig.setCombinedLeftWidthPx(leftWidth);
                                    }
                                    neverloseConfig.setCombinedDividerPosition(Math.max(0.0, Math.min(1.0, pos)));
                                    neverloseConfig.save();
                                }
                            }
                        } catch (Exception ignored) {
                        } finally {
                            dragging[0] = false;
                        }
                    });
                });
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
            if (leftControls instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
            }
            HBox.setHgrow(leftControls, Priority.ALWAYS);
            leftHeaderBox.getChildren().add(leftControls);
        }
        // Right: save/export from editor
        if (editorController instanceof ControlsProvider providerRight) {
            Node rightControls = providerRight.controlsContainer();
            rightHeaderBox.getChildren().add(rightControls);
        }
    }
}
