package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.view.View;
import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Controller for the exercise card view that displays a single training exercise template.
 * Handles selection and deletion of the template.
 */
@View
public class ExerciseCardController {

    @FXML
    private VBox rootPane;
    
    @FXML
    private Label templateNameLabel;
    
    @FXML
    private Button deleteButton;
    
    private ExerciseSummary template;
    private Consumer<UUID> onDeleteAction;
    private Consumer<UUID> onSelectAction;
    
    /**
     * Initializes the controller after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        // Ensure the delete button doesn't trigger the card click event
        deleteButton.setOnMouseClicked(event -> event.consume());
    }
    
    /**
     * Sets the template to display in this card.
     *
     * @param template the template summary to display
     */
    public void setTemplate(ExerciseSummary template) {
        this.template = template;
        templateNameLabel.setText(template.name());
    }
    
    /**
     * Sets the action to perform when the delete button is clicked.
     *
     * @param onDeleteAction a consumer that accepts the template ID
     */
    public void setOnDeleteAction(Consumer<UUID> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }
    
    /**
     * Sets the action to perform when the card is clicked (selected).
     *
     * @param onSelectAction a consumer that accepts the template ID
     */
    public void setOnSelectAction(Consumer<UUID> onSelectAction) {
        this.onSelectAction = onSelectAction;
    }
    
    /**
     * Handles a click on the card, which selects the template.
     */
    @FXML
    private void handleCardClick() {
        if (onSelectAction != null && template != null) {
            onSelectAction.accept(template.identifier());
        }
    }
    
    /**
     * Handles a click on the delete button.
     */
    @FXML
    private void handleDeleteButtonClick() {
        if (onDeleteAction != null && template != null) {
            onDeleteAction.accept(template.identifier());
        }
    }
}