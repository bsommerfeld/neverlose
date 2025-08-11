package de.bsommerfeld.neverlose.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import de.bsommerfeld.neverlose.export.ExportService;
import de.bsommerfeld.neverlose.export.PdfExportService;
import de.bsommerfeld.neverlose.fx.state.SearchState;
import de.bsommerfeld.neverlose.fx.view.ViewLoader;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.persistence.guice.PersistenceModule;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NeverLoseModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PersistenceModule());

        // Core bindings
        bind(Bootstrap.class).to(NeverLoseBootstrap.class);
        bind(ExportService.class).to(PdfExportService.class);

        // View system bindings
        bind(ViewProvider.class).asEagerSingleton();
        bind(ViewLoader.class).in(Singleton.class);
        bind(SearchState.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    NeverloseConfig provideNeverloseConfig() {
        Path configPath = Paths.get(LogDirectorySetup.getApplicationDataBaseDirectory().resolve("neverlose").toUri().getPath(), "config.yaml");
        return ConfigurationLoader.load(configPath, NeverloseConfig::new, false);
    }
}
