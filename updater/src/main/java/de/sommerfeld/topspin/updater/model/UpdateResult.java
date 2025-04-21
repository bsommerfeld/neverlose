package de.sommerfeld.topspin.updater.model;

import java.io.File;
import java.net.URL;

/**
 * Represents the result of an update-check or of a download.
 */
public sealed interface UpdateResult permits
        UpdateResult.UpToDate,
        UpdateResult.UpdateAvailable,
        UpdateResult.CheckFailed,
        UpdateResult.DownloadFailed,
        UpdateResult.DownloadOk {

    /**
     * The current installed version is the latest.
     */
    record UpToDate() implements UpdateResult {
    }

    /**
     * A new version is available.
     *
     * @param latestVersion   The version of the latest release.
     * @param releaseNotesUrl URL to the release notes.
     * @param downloadUrl     The download URL to the respective assets of the platform.
     * @param assetSize       Size of the assets in bytes.
     */
    record UpdateAvailable(AppVersion latestVersion, URL releaseNotesUrl, URL downloadUrl,
                           long assetSize) implements UpdateResult {
    }

    /**
     * Error while update parsing.
     * F.e. network or parsing errors
     *
     * @param cause The cause of the error.
     */
    record CheckFailed(Throwable cause) implements UpdateResult {
    }

    /**
     * Error while downloading
     *
     * @param cause The cause of the error.
     */
    record DownloadFailed(Throwable cause) implements UpdateResult {
    }

    /**
     * Download successful.
     *
     * @param downloadedFile The downloaded installer file.
     */
    record DownloadOk(File downloadedFile) implements UpdateResult {
    }
}
