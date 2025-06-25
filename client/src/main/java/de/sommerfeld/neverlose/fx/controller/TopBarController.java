package de.sommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.sommerfeld.neverlose.fx.state.SearchState;
import de.sommerfeld.neverlose.fx.view.View;
import de.sommerfeld.neverlose.fx.view.ViewProvider;
import de.sommerfeld.neverlose.plan.TrainingPlan;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

@View
public class TopBarController {

    private final ViewProvider viewProvider;
    private final SearchState searchState;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button createNewPlanButton;

    @Inject
    public TopBarController(ViewProvider viewProvider, SearchState searchState) {
        this.viewProvider = viewProvider;
        this.searchState = searchState;
    }

    @FXML
    private void initialize() {
        Bindings.bindBidirectional(searchTextField.textProperty(), searchState.searchTermProperty());
    }

    @FXML
    public void onNewPlan(ActionEvent actionEvent) {
        TrainingPlan newPlan = new TrainingPlan("New Plan", "Add description and units...");
        viewProvider.triggerViewChange(TrainingPlanEditorMetaController.class, planEditor -> planEditor.setPlan(newPlan));
        searchState.setSearchTerm("");
    }
}