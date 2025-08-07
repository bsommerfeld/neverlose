package de.bsommerfeld.neverlose.bootstrap;

import com.google.inject.Inject;
import de.bsommerfeld.neverlose.fx.NeverLoseApplication;
import javafx.application.Application;

public class NeverLoseBootstrap implements Bootstrap {

    private final NeverloseConfig neverloseConfig;
    private boolean isFirstStart = false;

    @Inject
    public NeverLoseBootstrap(NeverloseConfig neverloseConfig) {
        this.neverloseConfig = neverloseConfig;
    }

    @Override
    public void start() {
        if (neverloseConfig.isFirstStart()) {
            isFirstStart = true;
            neverloseConfig.setFirstStart(false);
            neverloseConfig.save();
        }

        Application.launch(NeverLoseApplication.class);
    }

    public boolean isFirstStart() {
        return isFirstStart;
    }
}
