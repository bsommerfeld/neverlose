package de.sommerfeld.topspin.updater.service;

import de.sommerfeld.topspin.updater.model.AppVersion;
import de.sommerfeld.topspin.updater.model.UpdateResult;
import de.sommerfeld.topspin.updater.model.UpdateState;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Centralized service to manage an update process.
 * Handles checking for updates, downloading, and triggering installations.
 */
public interface UpdateService {

    /**
     * Gets the currently installed application version.
     *
     * @return The current AppVersion.
     */
    AppVersion getCurrentVersion();

    /**
     * Gets the current state of the update process (e.g., IDLE, CHECKING).
     * The implementation should manage this state internally.
     *
     * @return The current UpdateState.
     */
    UpdateState getUpdateState();

    /**
     * Gets the last determined result of an update check or download attempt.
     * The implementation should store the last relevant result internally.
     * Will return null or an initial state if no check/download has been performed yet.
     *
     * @return The last UpdateResult (e.g., UpToDate, UpdateAvailable, CheckFailed, DownloadOk, DownloadFailed).
     */
    UpdateResult getUpdateResult();

    /**
     * Gets the current download progress.
     * Returns a value between 0.0 (not started) and 1.0 (complete).
     * Might return -1.0 or 0.0 if no download is in progress.
     * Note: Without observable properties, callers might need to poll this or use a callback mechanism
     * if detailed progress updates are required during the download.
     *
     * @return The download progress (0.0 to 1.0), or an indicator value like -1.0.
     */
    double getDownloadProgress();

    /**
     * Asynchronously checks for available updates against the configured source.
     * Updates the internal state (accessible via {@link #getUpdateState()} and {@link #getUpdateResult()}).
     *
     * @return A CompletableFuture that completes with the result of the check (an instance of UpdateResult).
     * The future completes exceptionally if the check itself fails fundamentally before determining a result.
     */
    CompletableFuture<UpdateResult> checkAsync();

    /**
     * Asynchronously downloads the update if one is available.
     * Sets the internal state to DOWNLOADING and reports progress via the callback.
     * Requires that {@link #checkAsync()} completed successfully beforehand with an {@link UpdateResult.UpdateAvailable}.
     *
     * @param progressCallback A Consumer that accepts progress updates (a Double between 0.0 and 1.0).
     *                         It will be called periodically on the background thread during download.
     *                         Implementations consuming this must handle thread safety (e.g., use SwingUtilities.invokeLater for UI updates).
     * @return A CompletableFuture that completes with the downloaded {@link File} on success.
     * The internal result will be set to {@link UpdateResult.DownloadOk} or {@link UpdateResult.DownloadFailed}.
     * @throws IllegalStateException if the current state does not indicate that an update is available for download.
     */
    CompletableFuture<File> downloadAsync(Consumer<Double> progressCallback);

    /**
     * Attempts to launch the downloaded installer file using the default OS mechanism.
     * Typically, uses {@link java.awt.Desktop#open(File)}.
     * The calling application should consider exiting gracefully after invoking this method.
     *
     * @param installerFile The downloaded installer file. Must not be null and must exist.
     * @throws UnsupportedOperationException if {@link java.awt.Desktop#isDesktopSupported()} is false or
     *                                       {@link java.awt.Desktop.Action#OPEN} is not supported.
     * @throws IOException                   if the operating system cannot open the specified file.
     * @throws IllegalArgumentException      if the installerFile is null or does not exist.
     * @throws SecurityException             if a security manager exists and its checkRead method denies read access
     *                                       to the file or denies the AWTPermission("showWindowWithoutWarningBanner").
     */
    void launchInstaller(File installerFile);

    /**
     * Convenience method to trigger the entire standard update workflow asynchronously:
     * Check -> Download (if available) -> Launch Installer (if downloaded) -> Exit App (potentially).
     * This method should internally chain the asynchronous calls using CompletableFuture methods
     * like thenCompose and thenAcceptAsync. The calling application might typically exit
     * after successfully launching the installer.
     *
     * @return A CompletableFuture that completes normally when the workflow finishes (either successfully launching
     * the installer, determining no update is needed, or handling an error internally).
     * It completes exceptionally if an unhandled error occurs during the workflow.
     */
    CompletableFuture<Void> triggerUpdateWorkflow();
}