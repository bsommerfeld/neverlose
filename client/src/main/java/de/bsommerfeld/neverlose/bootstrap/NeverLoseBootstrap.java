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
            // Mark first start as handled in-memory. Do not persist immediately to avoid overwriting
            // other config values (e.g., UI sizes) that might be loaded/applied later during startup.
            // Persisting can happen later when user-triggered saves occur.
            neverloseConfig.setFirstStart(false);
        }

        Application.launch(NeverLoseApplication.class);
    }

    public boolean isFirstStart() {
        return isFirstStart;
    }
}
