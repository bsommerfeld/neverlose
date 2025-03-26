package de.sommerfeld.topspin.bootstrap;

import de.sommerfeld.topspin.fx.TopspinApplication;
import javafx.application.Application;

public class TopspinBootstrap implements Bootstrap {

    @Override
    public void start() {
        Application.launch(TopspinApplication.class);
    }
}
