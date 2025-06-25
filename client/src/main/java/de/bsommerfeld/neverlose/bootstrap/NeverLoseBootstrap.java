package de.bsommerfeld.neverlose.bootstrap;

import de.bsommerfeld.neverlose.fx.NeverLoseApplication;
import javafx.application.Application;

public class NeverLoseBootstrap implements Bootstrap {

  @Override
  public void start() {
    Application.launch(NeverLoseApplication.class);
  }
}
