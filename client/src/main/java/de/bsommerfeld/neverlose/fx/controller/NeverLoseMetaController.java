package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/** Controller for the meta view. */
@View
public class NeverLoseMetaController {

  private final ViewProvider viewProvider;

  @FXML private HBox topBarPlaceholder;

  @FXML private AnchorPane centerContentPlaceholder;

  @FXML private HBox bottomBarPlaceholder;

  @Inject
  public NeverLoseMetaController(ViewProvider viewProvider) {
    this.viewProvider = viewProvider;
  }

  private static void setAnchor(Parent center) {
    AnchorPane.setTopAnchor(center, 0d);
    AnchorPane.setLeftAnchor(center, 0d);
    AnchorPane.setRightAnchor(center, 0d);
    AnchorPane.setBottomAnchor(center, 0d);
  }

  @FXML
  private void initialize() {
    loadTopBar();
    loadBottomBar();

    // Show the plan list view as the default view
    showPlanListView();

    bottomBarPlaceholder.requestFocus(); // to get away from the search field
  }

  private void loadTopBar() {
    Parent topBar = viewProvider.requestView(TopBarController.class).parent();
    topBarPlaceholder.getChildren().add(topBar);
  }

  private void loadBottomBar() {
    Parent bottomBar = viewProvider.requestView(BottomBarController.class).parent();
    bottomBarPlaceholder.getChildren().add(bottomBar);
  }

  /**
   * Loads a view into the center content area.
   *
   * @param clazz the class of the controller for the view to load
   * @param <T> the type of the controller
   * @return the controller instance
   */
  public <T> T loadCenter(Class<T> clazz) {
    ViewWrapper<T> viewWrapper = viewProvider.requestView(clazz);
    T controller = viewWrapper.controller();

    Parent center = viewWrapper.parent();
    setAnchor(center);
    centerContentPlaceholder.getChildren().setAll(center);

    return controller;
  }

  /** Shows the plan list view in the center content area. */
  public void showPlanListView() {
    PlanListViewController controller = loadCenter(PlanListViewController.class);
    controller.setMetaController(this);

    // Connect the TopBarController with the PlanListViewController for search functionality
    ViewWrapper<TopBarController> topBarWrapper = viewProvider.requestView(TopBarController.class);
    TopBarController topBarController = topBarWrapper.controller();
    topBarController.setPlanListViewController(controller);

    // Refresh the plan list to ensure it's up-to-date
    controller.refreshPlans();
  }

  /**
   * Shows the training plan editor for the given plan in the center content area.
   *
   * @param plan the plan to edit
   */
  public void showTrainingPlanEditor(TrainingPlan plan) {
    TrainingPlanEditorController controller = loadCenter(TrainingPlanEditorController.class);
    controller.setTrainingPlan(plan);
    controller.setMetaController(this);
  }
}
