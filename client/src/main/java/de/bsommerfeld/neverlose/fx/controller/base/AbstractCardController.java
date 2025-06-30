package de.bsommerfeld.neverlose.fx.controller.base;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstract base controller for card views that display a single item.
 * Handles selection and deletion of the item.
 *
 * @param <T> the type of item displayed in the card
 */
public abstract class AbstractCardController<T> {

    @FXML
    protected VBox rootPane;
    
    @FXML
    protected Label templateNameLabel;
    
    @FXML
    protected Button deleteButton;
    
    protected T item;
    protected Consumer<UUID> onDeleteAction;
    protected Consumer<UUID> onSelectAction;
    
    /**
     * Initializes the controller after FXML fields are injected.
     */
    @FXML
    protected void initialize() {
        // Ensure the delete button doesn't trigger the card click event
        deleteButton.setOnMouseClicked(event -> event.consume());
    }
    
    /**
     * Sets the item to display in this card.
     *
     * @param item the item to display
     */
    public void setItem(T item) {
        this.item = item;
        templateNameLabel.setText(getItemName(item));
    }
    
    /**
     * Sets the action to perform when the delete button is clicked.
     *
     * @param onDeleteAction a consumer that accepts the item ID
     */
    public void setOnDeleteAction(Consumer<UUID> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }
    
    /**
     * Sets the action to perform when the card is clicked (selected).
     *
     * @param onSelectAction a consumer that accepts the item ID
     */
    public void setOnSelectAction(Consumer<UUID> onSelectAction) {
        this.onSelectAction = onSelectAction;
    }
    
    /**
     * Handles a click on the card, which selects the item.
     */
    @FXML
    protected void handleCardClick() {
        if (onSelectAction != null && item != null) {
            onSelectAction.accept(getItemId(item));
        }
    }
    
    /**
     * Handles a click on the delete button.
     */
    @FXML
    protected void handleDeleteButtonClick() {
        if (onDeleteAction != null && item != null) {
            onDeleteAction.accept(getItemId(item));
        }
    }
    
    /**
     * Gets the name of the item to display in the card.
     *
     * @param item the item
     * @return the name of the item
     */
    protected abstract String getItemName(T item);
    
    /**
     * Gets the ID of the item for use in callbacks.
     *
     * @param item the item
     * @return the ID of the item
     */
    protected abstract UUID getItemId(T item);
}