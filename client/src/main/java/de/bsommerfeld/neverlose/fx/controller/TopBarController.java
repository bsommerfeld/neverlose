package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;

@View
public class TopBarController {

    private static final LogFacade log = LogFacadeFactory.getLogger();
    private final ViewProvider viewProvider;
    @FXML
    private Separator separator;
    @FXML
    private HBox dynamicComponentsContainer;

    @Inject
    public TopBarController(ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @FXML
    private void initialize() {
        BooleanBinding isEmptyBinding = Bindings.isEmpty(dynamicComponentsContainer.getChildren());
        separator.visibleProperty().bind(isEmptyBinding.not());
    }

    /** Handles the home button click event. Navigates to the home view. */
    @FXML
    private void handleHomeButton() {
        log.debug("Home button clicked");
        viewProvider.triggerViewChange(HomeViewController.class);
    }

    /**
     * Registers components to be displayed in the TopBar. These components will be added to the dynamic components
     * container.
     *
     * @param alignment  The alignment of the components (LEFT, CENTER, RIGHT)
     * @param components The components to register
     */
    public void registerComponents(Alignment alignment, Node... components) {
        if (components == null || components.length == 0) {
            return;
        }

        log.debug("Registering {} components in TopBar with alignment {}", components.length, alignment);
        dynamicComponentsContainer.setAlignment(alignment.getPosition());
        dynamicComponentsContainer.getChildren().addAll(components);
    }

    /**
     * Registers components to be displayed in the TopBar with CENTER alignment. This is a convenience method for
     * backward compatibility.
     *
     * @param components The components to register
     */
    public void registerComponents(Node... components) {
        registerComponents(Alignment.CENTER, components);
    }

    /**
     * Unregisters all components from the TopBar. This removes all components from the dynamic components container.
     */
    public void unregisterAllComponents() {
        log.debug("Unregistering all components from TopBar");
        dynamicComponentsContainer.getChildren().clear();
    }

    /**
     * Unregisters specific components from the TopBar. This removes the specified components from the dynamic
     * components container.
     *
     * @param components The components to unregister
     */
    public void unregisterComponents(Node... components) {
        if (components == null || components.length == 0) {
            return;
        }

        log.debug("Unregistering {} specific components from TopBar", components.length);
        dynamicComponentsContainer.getChildren().removeAll(components);
    }

    /**
     * Enum for the alignment options of components in the TopBar.
     */
    public enum Alignment {
        LEFT(Pos.CENTER_LEFT),
        CENTER(Pos.CENTER),
        RIGHT(Pos.CENTER_RIGHT);

        private final Pos position;

        Alignment(Pos position) {
            this.position = position;
        }

        public Pos getPosition() {
            return position;
        }
    }
}
