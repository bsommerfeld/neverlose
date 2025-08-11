package de.bsommerfeld.neverlose.fx.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Defines a provider of UI controls along with a pluggable container factory. Implementations supply the individual
 * control nodes and can customize how they are wrapped in a layout container.
 */
public interface ControlsProvider {

    /**
     * Creates and returns the container in which the control nodes will be placed. By default this delegates to
     * {@link #createContainer(Node...)}.
     *
     * @return a {@link Region} that wraps the controls returned by {@link #controls()}
     */
    default Region controlsContainer() {
        return createContainer(controls());
    }

    /**
     * Factory hook for creating a container around the given controls. Implementations may override this to produce a
     * different {@link Region} (e.g. {@code FlowPane}, {@code GridPane}, etc.).
     *
     * @param ctrls the control nodes to be placed in the container
     *
     * @return a {@link Region} instance wrapping the provided nodes
     */
    default Region createContainer(Node... ctrls) {
        return new HBox(ctrls);
    }

    /**
     * Returns the desired alignment for the container in the parent layout.
     *
     * @return an {@link Alignment} value indicating how the container should be aligned
     */
    Alignment alignment();

    /**
     * Supplies the array of control nodes that will be shown in the UI.
     *
     * @return an array of {@link Node} objects representing the controls
     */
    Node[] controls();

    enum Alignment {
        CENTER,
        RIGHT
    }
}