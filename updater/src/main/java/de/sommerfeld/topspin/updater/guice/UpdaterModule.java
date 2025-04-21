package de.sommerfeld.topspin.updater.guice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import de.sommerfeld.topspin.updater.client.GitHubApiHttpClient;
import de.sommerfeld.topspin.updater.client.UpdateApiClient;
import de.sommerfeld.topspin.updater.provider.PropertiesVersionProvider;
import de.sommerfeld.topspin.updater.provider.VersionProvider;
import de.sommerfeld.topspin.updater.service.DefaultUpdateService;
import de.sommerfeld.topspin.updater.service.UpdateService;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Guice module for configuring updater-related services and constants.
 */
public class UpdaterModule extends AbstractModule {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("version.properties.filename")).to("version.properties");
        bindConstant().annotatedWith(Names.named("version.properties.key")).to("app.version");

        bindConstant().annotatedWith(Names.named("github.repo.owner")).to("Metaphoriker");
        bindConstant().annotatedWith(Names.named("github.repo.name")).to("topspin");

        bind(VersionProvider.class).to(PropertiesVersionProvider.class).in(Scopes.SINGLETON);
        bind(UpdateApiClient.class).to(GitHubApiHttpClient.class).in(Scopes.SINGLETON);
        bind(UpdateService.class).to(DefaultUpdateService.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    HttpClient provideHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }
}
