package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.controller.TopBarController.Alignment;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/** Controller for the meta view. */
@View
public class NeverLoseMetaController {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private final ViewProvider viewProvider;
    private final NotificationService notificationService;
    private final Map<Object, ControlsContainer> controlsContainerMap = new HashMap<>();

    private TopBarController topBarController;

    @FXML
    private HBox topBarPlaceholder;
    @FXML
    private AnchorPane centerContentPlaceholder;
    @FXML
    private HBox bottomBarPlaceholder;
    @FXML
    private VBox notificationContainer;

    @Inject
    public NeverLoseMetaController(
            ViewProvider viewProvider, NotificationService notificationService) {
        this.viewProvider = viewProvider;
        this.notificationService = notificationService;
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

        // Make the notification container NOT transparent for mouse events
        // so that notifications can be clicked
        notificationContainer.setMouseTransparent(false);

        // Initialize the notification service
        notificationService.init(notificationContainer);

        // Show the home view as the default view
        showHomeView();
    }

    private void loadTopBar() {
        ViewWrapper<TopBarController> viewWrapper = viewProvider.requestView(TopBarController.class);
        topBarController = viewWrapper.controller();
        Parent topBar = viewWrapper.parent();
        topBarPlaceholder.getChildren().add(topBar);
    }

    private void registerControls(Object controller) {
        if (controller instanceof ControlsProvider controlsProvider) {
            Alignment alignment = controlsProvider.alignment();
            Region container = controlsProvider.controlsContainer();

            // Automatically updates the values inside the map since there are no 2 instances of a controller
            controlsContainerMap.put(controller, new ControlsContainer(alignment, container));
        }
    }

    private void loadBottomBar() {
        Parent bottomBar = viewProvider.requestView(BottomBarController.class).parent();
        bottomBarPlaceholder.getChildren().add(bottomBar);
    }

    /**
     * Loads a view into the center content area.
     *
     * @param clazz the class of the controller for the view to load
     * @param <T>   the type of the controller
     *
     * @return the controller instance
     */
    public <T> T loadCenter(Class<T> clazz) {
        ViewWrapper<T> viewWrapper = viewProvider.requestView(clazz);
        T controller = viewWrapper.controller();

        Parent center = viewWrapper.parent();
        setAnchor(center);
        centerContentPlaceholder.getChildren().setAll(center);

        // Register controls if provided
        registerControls(controller);

        // Update the topbar with control nodes
        updateTopBarControlNodes(controller);

        return controller;
    }

    private void updateTopBarControlNodes(Object controller) {
        ControlsContainer container = controlsContainerMap.get(controller);
        if (container == null) {
            topBarController.unregisterAllComponents();
            log.debug("Could not show controls container because container is null.");
            return;
        }

        topBarController.registerComponents(container.alignment, container.container);
    }

    /** Shows the home view in the center content area. */
    public void showHomeView() {
        HomeViewController controller = loadCenter(HomeViewController.class);
        controller.setMetaController(this);
    }

    /** Shows the plan list view in the center content area. */
    public void showPlanListView() {
        PlanListViewController controller = loadCenter(PlanListViewController.class);
        controller.setMetaController(this);
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
    }

    static class ControlsContainer {
        Alignment alignment;
        Region container;

        ControlsContainer(Alignment alignment, Region container) {
            this.alignment = alignment;
            this.container = container;
        }
    }
}
