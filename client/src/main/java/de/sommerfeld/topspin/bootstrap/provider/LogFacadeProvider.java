package de.sommerfeld.topspin.bootstrap.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;

public class LogFacadeProvider implements Provider<LogFacade> {

    private final LogFacadeFactory factory;

    @Inject
    public LogFacadeProvider(LogFacadeFactory factory) {
        this.factory = factory;
    }

    @Override
    public LogFacade get() {
        return factory.getLogger();
    }
}
