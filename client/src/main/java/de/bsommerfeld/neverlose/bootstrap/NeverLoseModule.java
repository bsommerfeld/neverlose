package de.bsommerfeld.neverlose.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import de.bsommerfeld.neverlose.export.ExportService;
import de.bsommerfeld.neverlose.export.PdfExportService;
import de.bsommerfeld.neverlose.fx.animation.BreathingAnimationService;
import de.bsommerfeld.neverlose.fx.animation.BreathingAnimationServiceImpl;
import de.bsommerfeld.neverlose.fx.animation.FeedbackAnimationService;
import de.bsommerfeld.neverlose.fx.animation.FeedbackAnimationServiceImpl;
import de.bsommerfeld.neverlose.fx.animation.MicroNarrativeAnimationService;
import de.bsommerfeld.neverlose.fx.animation.MicroNarrativeAnimationServiceImpl;
import de.bsommerfeld.neverlose.fx.state.SearchState;
import de.bsommerfeld.neverlose.fx.theme.ContextualThemeService;
import de.bsommerfeld.neverlose.fx.theme.ContextualThemeServiceImpl;
import de.bsommerfeld.neverlose.fx.theme.EmotionalThemeService;
import de.bsommerfeld.neverlose.fx.theme.EmotionalThemeServiceImpl;
import de.bsommerfeld.neverlose.fx.theme.TemporalThemeService;
import de.bsommerfeld.neverlose.fx.theme.TemporalThemeServiceImpl;
import de.bsommerfeld.neverlose.fx.theme.TimeBasedThemeService;
import de.bsommerfeld.neverlose.fx.theme.TimeBasedThemeServiceImpl;
import de.bsommerfeld.neverlose.fx.tracking.UsageTrackingService;
import de.bsommerfeld.neverlose.fx.tracking.UsageTrackingServiceImpl;
import de.bsommerfeld.neverlose.fx.view.ViewLoader;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import de.bsommerfeld.neverlose.fx.viewmodel.UIViewModel;
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

        // Animation service bindings
        bind(MicroNarrativeAnimationService.class).to(MicroNarrativeAnimationServiceImpl.class).in(Singleton.class);
        bind(BreathingAnimationService.class).to(BreathingAnimationServiceImpl.class).in(Singleton.class);
        bind(FeedbackAnimationService.class).to(FeedbackAnimationServiceImpl.class).in(Singleton.class);

        // Theme service bindings
        bind(TimeBasedThemeService.class).to(TimeBasedThemeServiceImpl.class).in(Singleton.class);
        bind(EmotionalThemeService.class).to(EmotionalThemeServiceImpl.class).in(Singleton.class);
        bind(TemporalThemeService.class).to(TemporalThemeServiceImpl.class).in(Singleton.class);
        bind(ContextualThemeService.class).to(ContextualThemeServiceImpl.class).in(Singleton.class);

        // Tracking service bindings
        bind(UsageTrackingService.class).to(UsageTrackingServiceImpl.class).in(Singleton.class);

        // ViewModel bindings
        bind(UIViewModel.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    NeverloseConfig provideNeverloseConfig() {
        Path configPath = Paths.get(LogDirectorySetup.getApplicationDataBaseDirectory().resolve("neverlose").toUri().getPath(), "config.json");
        return ConfigurationLoader.load(configPath, NeverloseConfig::new, false);
    }
}
