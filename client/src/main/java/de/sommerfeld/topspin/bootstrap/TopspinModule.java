package de.sommerfeld.topspin.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.spi.InjectionPoint;
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
    }

    /**
     * Provides an instance of {@link LogFacade} for the specified injection point.
     * The logger is specific to the class where the injection point is declared.
     *
     * @param injectionPoint The injection point requiring a {@link LogFacade} instance.
     * @return A {@link LogFacade} instance associated with the class of the injection point.
     */
    @Provides
    LogFacade provideLogFacade(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LogFacadeFactory.getLogger(targetClass);
    }
}
