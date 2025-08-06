package de.bsommerfeld.neverlose.fx.components;

import de.bsommerfeld.neverlose.fx.messages.Messages;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

/**
 * A container that can be dragged around within its parent container. This is used to create floating panels within the
 * main application window.
 * <p>
 * The container can be moved by clicking and dragging anywhere on it. It will automatically be brought to the front
 * when clicked, but will remain below the notification container due to z-index settings in CSS.
 * <p>
 * The container includes a close button in the top-right corner that allows the user to dismiss the container
 * without selecting any content.
 */
public class DraggableContainer extends BorderPane {
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double initialTranslateX;
    private double initialTranslateY;
    private boolean isDragging = false;

    /**
     * Creates a new DraggableContainer with the specified content.
     * The container includes a close button in the top-right corner that allows
     * the user to remove the container from its parent.
     *
     * @param content the content to display in the container
     */
    public DraggableContainer(Node content) {
        // Initialize the BorderPane without content
        super();

        // Add styling
        getStyleClass().add("draggable-container");

        // Create the close button
        Button closeButton = createCloseButton();

        // Create a header with the close button aligned to the right
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.getChildren().add(closeButton);
        header.setPadding(new Insets(0, 0, 5, 0)); // Add some space below the header

        // Set the header as the top of the BorderPane
        setTop(header);

        // Set the content as the center of the BorderPane
        setCenter(content);

        // Set up mouse event handlers for dragging
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(this::handleMouseReleased);

        // Make sure the container is visible and can receive mouse events
        setPickOnBounds(true);
        setMouseTransparent(false);
    }

    /**
     * Creates a close button with an X icon.
     *
     * @return the close button
     */
    private Button createCloseButton() {
        // Create the SVG path for the X icon
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M 0 0 L 10 10 M 0 10 L 10 0");
        closeIcon.getStyleClass().add("notification-close-icon");

        // Create the button with the icon
        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().add("notification-close-button");
        closeButton.setOnAction(event -> removeFromParent());
        closeButton.setTooltip(new Tooltip(Messages.getString("ui.button.close")));

        return closeButton;
    }

    /**
     * Handles the mouse pressed event to start dragging.
     *
     * @param event the mouse event
     */
    private void handleMousePressed(MouseEvent event) {
        // Store the initial mouse position and container position
        mouseAnchorX = event.getSceneX();
        mouseAnchorY = event.getSceneY();
        initialTranslateX = getTranslateX();
        initialTranslateY = getTranslateY();
        isDragging = true;

        // Bring to front when clicked
        toFront();

        // Consume the event to prevent it from propagating
        event.consume();
    }

    /**
     * Handles the mouse dragged event to move the container.
     *
     * @param event the mouse event
     */
    private void handleMouseDragged(MouseEvent event) {
        if (isDragging) {
            // Calculate the new position based on the mouse movement
            double deltaX = event.getSceneX() - mouseAnchorX;
            double deltaY = event.getSceneY() - mouseAnchorY;

            // Update the container position
            setTranslateX(initialTranslateX + deltaX);
            setTranslateY(initialTranslateY + deltaY);

            // Consume the event to prevent it from propagating
            event.consume();
        }
    }

    /**
     * Handles the mouse released event to stop dragging.
     *
     * @param event the mouse event
     */
    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;

        // Consume the event to prevent it from propagating
        event.consume();
    }

    /**
     * Adds this container to the specified parent pane and centers it immediately.
     *
     * @param parent the parent pane to add this container to
     */
    public void addToParent(Pane parent) {
        // Calculate center position based on parent size and preferred size
        double parentWidth = parent.getWidth();
        double parentHeight = parent.getHeight();
        double containerWidth = getPrefWidth();
        double containerHeight = getPrefHeight();

        // Center the container before adding it to the parent
        setTranslateX((parentWidth - containerWidth) / 2);
        setTranslateY((parentHeight - containerHeight) / 2);

        // Add the container to the parent
        parent.getChildren().add(this);

        // Make sure it's on top of other content
        toFront();
    }

    /**
     * Removes this container from its parent.
     */
    public void removeFromParent() {
        if (getParent() instanceof Pane) {
            ((Pane) getParent()).getChildren().remove(this);
        }
    }
}