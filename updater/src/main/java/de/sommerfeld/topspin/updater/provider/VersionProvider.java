package de.sommerfeld.topspin.updater.provider;

import de.sommerfeld.topspin.updater.model.AppVersion;

/**
 * Provides the version of the currently installed application.
 */
public interface VersionProvider {

    /**
     * Returns the current {@link AppVersion} of the application.
     *
     * @return the current AppVersion
     * @throws IllegalStateException if the current version can't be processed.
     */
    AppVersion getCurrentVersion();
}
