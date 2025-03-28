package de.sommerfeld.topspin.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import de.sommerfeld.topspin.bootstrap.provider.LogFacadeProvider;
import de.sommerfeld.topspin.fx.view.ViewLoader;
import de.sommerfeld.topspin.fx.view.ViewProvider;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;

public class TopspinModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Bootstrap.class).to(TopspinBootstrap.class);

        bind(ViewProvider.class).asEagerSingleton();

        bind(ViewLoader.class).in(Singleton.class);
        bind(LogFacadeFactory.class).in(Singleton.class);

        bind(LogFacade.class).toProvider(LogFacadeProvider.class);
    }
}
