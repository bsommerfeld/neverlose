package de.bsommerfeld.neverlose.fx.controller;

import de.bsommerfeld.neverlose.fx.controller.TopBarController.Alignment;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public interface ControlsProvider {

  default Region controlsContainer() {
    return new HBox(controls());
  }

  Alignment alignment();

  Node[] controls();
}
