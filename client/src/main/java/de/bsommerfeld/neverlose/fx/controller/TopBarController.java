package de.bsommerfeld.neverlose.fx.controller;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.state.SearchState;
import de.bsommerfeld.neverlose.fx.view.View;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@View
public class TopBarController {

  private final SearchState searchState;

  @FXML private TextField searchTextField;

  @Inject
  public TopBarController(SearchState searchState) {
    this.searchState = searchState;
  }

  @FXML
  private void initialize() {
    Bindings.bindBidirectional(searchTextField.textProperty(), searchState.searchTermProperty());
  }
}
