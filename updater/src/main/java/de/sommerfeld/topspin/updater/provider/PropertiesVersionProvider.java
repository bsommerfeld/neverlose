package de.sommerfeld.topspin.updater.provider;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.updater.model.AppVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Implementation of VersionProvider that reads the version from a specified properties file
 * located in the classpath using a specified property key.
 * The filename and key are provided via the constructor (Dependency Injection).
 */
public class PropertiesVersionProvider implements VersionProvider {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private final String propertiesFilename;
    private final String versionPropertyKey;

    private AppVersion cachedVersion = null;

    /**
     * Creates a new instance. Parameters are injected by Guice.
     *
     * @param propertiesFilename The name of the properties file on the classpath (injected via @Named).
     * @param versionPropertyKey The key within the properties file holding the version string (injected via @Named).
     */
    @Inject
    public PropertiesVersionProvider(@Named("version.properties.filename") String propertiesFilename, @Named("version.properties.key") String versionPropertyKey) {
        this.propertiesFilename = Objects.requireNonNull(propertiesFilename, "propertiesFilename must not be null");
        this.versionPropertyKey = Objects.requireNonNull(versionPropertyKey, "versionPropertyKey must not be null");
    }

    @Override
    public AppVersion getCurrentVersion() {
        if (cachedVersion == null) {
            cachedVersion = loadVersionFromProperties();
        }
        return cachedVersion;
    }

    private AppVersion loadVersionFromProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFilename)) {

            if (inputStream == null) {
                String errorMsg = "Could not find properties file '" + propertiesFilename + "' in classpath.";
                log.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            properties.load(inputStream);
            String versionValue = properties.getProperty(versionPropertyKey);

            if (versionValue == null || versionValue.trim().isEmpty() || versionValue.contains("${")) {
                String errorMsg = "Property '" + versionPropertyKey + "' not found or not filtered in '" + propertiesFilename + "'. Value: " + versionValue;
                log.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            log.info("Loaded application version: {}", versionValue);
            return new AppVersion(versionValue.trim());

        } catch (IOException e) {
            String errorMsg = "Error loading version from properties file '" + propertiesFilename + "'";
            log.error(errorMsg, e);
            System.err.println(errorMsg + ": " + e.getMessage());
            throw new IllegalStateException(errorMsg, e);
        }
    }
}
