package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.controller.ControlsProvider.Alignment;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.view.ViewWrapper;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
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
        registerViewChangeListener();

        loadBottomBar();

        // Make the notification container NOT transparent for mouse events
        // so that notifications can be clicked
        notificationContainer.setMouseTransparent(false);

        // Initialize the notification service
        notificationService.init(notificationContainer);

        // Show the combined view as the default view
        viewProvider.triggerViewChange(CombinedViewController.class);
    }

    private void registerViewChangeListener() {
        viewProvider.registerViewChangeListener(PlanListViewController.class, this::displayView);
        viewProvider.registerViewChangeListener(TrainingPlanEditorController.class, this::displayView);
        viewProvider.registerViewChangeListener(CombinedViewController.class, this::displayView);
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

    private <T> void displayView(ViewWrapper<T> viewWrapper) {
        Parent center = viewWrapper.parent();
        T controller = viewWrapper.controller();

        setAnchor(center);
        centerContentPlaceholder.getChildren().setAll(center);

        registerControls(controller);

        if (controller instanceof PlanListViewController planListViewController) {
            planListViewController.refreshPlans();
        }
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
