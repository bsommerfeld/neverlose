package de.sommerfeld.neverlose.bootstrap;

import de.sommerfeld.neverlose.fx.NeverLoseApplication;
import javafx.application.Application;

public class NeverLoseBootstrap implements Bootstrap {

    @Override
    public void start() {
        Application.launch(NeverLoseApplication.class);
    }
}
