package de.sommerfeld.topspin.updater.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.updater.client.UpdateApiClient;
import de.sommerfeld.topspin.updater.model.*;
import de.sommerfeld.topspin.updater.provider.VersionProvider;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Default implementation of the UpdateService.
 */
@Singleton
public class DefaultUpdateService implements UpdateService {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private final UpdateApiClient apiClient;
    private final GitHubRepository repository;
    private final HttpClient downloadClient;
    private final ExecutorService backgroundExecutor;

    private final AppVersion currentVersion;
    private final AtomicReference<UpdateResult> lastResult = new AtomicReference<>(null);
    private volatile UpdateState currentState = UpdateState.IDLE;
    private volatile double currentProgress = 0.0;

    @Inject
    public DefaultUpdateService(
            VersionProvider versionProvider,
            UpdateApiClient apiClient,
            @Named("github.repo.owner") String repoOwner,
            @Named("github.repo.name") String repoName,
            HttpClient httpClient
    ) {
        Objects.requireNonNull(versionProvider, "versionProvider");
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
        this.repository = new GitHubRepository(
                Objects.requireNonNull(repoOwner, "repoOwner"),
                Objects.requireNonNull(repoName, "repoName")
        );
        this.downloadClient = Objects.requireNonNull(httpClient, "httpClient");
        this.currentVersion = versionProvider.getCurrentVersion();

        this.backgroundExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread t = Executors.defaultThreadFactory().newThread(runnable);
            t.setName("UpdateService-Worker");
            t.setDaemon(true);
            return t;
        });

        log.info("UpdateService initialized. Current version: {}", currentVersion.value());
    }

    @Override
    public AppVersion getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public UpdateState getUpdateState() {
        return currentState;
    }

    @Override
    public UpdateResult getUpdateResult() {
        return lastResult.get();
    }

    @Override
    public double getDownloadProgress() {
        // Simple implementation - only valid during DOWNLOADING state
        // Needs enhancement for real progress reporting during download stream processing
        return (currentState == UpdateState.DOWNLOADING) ? currentProgress : 0.0;
    }

    @Override
    public CompletableFuture<UpdateResult> checkAsync() {
        if (currentState == UpdateState.CHECKING || currentState == UpdateState.DOWNLOADING) {
            log.warn("Update check or download already in progress.");
            return CompletableFuture.completedFuture(lastResult.get());
        }

        currentState = UpdateState.CHECKING;
        lastResult.set(null);
        log.info("Checking for updates...");

        return apiClient.fetchLatestReleaseInfo(repository)
                .thenApplyAsync(releaseInfo -> {
                    log.debug("Latest release version found: {}", releaseInfo.latestVersion().value());
                    if (releaseInfo.latestVersion().compareTo(currentVersion) > 0) {
                        String platformKey = determinePlatformAssetKey();
                        AssetDetails details = releaseInfo.assets().get(platformKey);

                        if (details != null) {
                            log.info("Update available: Version {} -> {}", currentVersion.value(), releaseInfo.latestVersion().value());
                            UpdateResult result = new UpdateResult.UpdateAvailable(
                                    releaseInfo.latestVersion(),
                                    releaseInfo.releaseNotesUrl(),
                                    details.downloadUrl(),
                                    details.size()
                            );
                            lastResult.set(result);
                        } else {
                            log.warn("Update found (Version {}), but no suitable asset found for platform key '{}'.", releaseInfo.latestVersion().value(), platformKey);
                            lastResult.set(new UpdateResult.CheckFailed(new Exception("No suitable download asset found for this platform.")));
                        }

                    } else {
                        log.info("Application is up-to-date (Version {}).", currentVersion.value());
                        lastResult.set(new UpdateResult.UpToDate());
                    }
                    currentState = UpdateState.IDLE;
                    return lastResult.get();
                }, backgroundExecutor)
                .exceptionally(ex -> {
                    log.error("Update check failed.", ex);
                    UpdateResult result = new UpdateResult.CheckFailed(ex);
                    lastResult.set(result);
                    currentState = UpdateState.ERROR;
                    return result;
                });
    }

    @Override
    public CompletableFuture<File> downloadAsync(Consumer<Double> progressCallback) {
        UpdateResult currentResult = lastResult.get();
        if (!(currentResult instanceof UpdateResult.UpdateAvailable availableResult)) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("No update available or check not performed successfully. Current result: " + currentResult)
            );
        }
        final Consumer<Double> finalProgressCallback = (progressCallback != null) ? progressCallback : progress -> {
        };

        if (currentState == UpdateState.DOWNLOADING) {
            log.warn("Download already in progress.");
            return CompletableFuture.failedFuture(new IllegalStateException("Download already in progress."));
        }

        currentState = UpdateState.DOWNLOADING;
        currentProgress = 0.0;
        finalProgressCallback.accept(0.0);

        URL downloadUrl = availableResult.downloadUrl();
        final long totalSize = availableResult.assetSize();
        log.info("Starting download ({} bytes) from: {}", totalSize > 0 ? totalSize : "unknown size", downloadUrl);

        return CompletableFuture.supplyAsync(() -> {
            Path tempFile = null;
            HttpRequest request;

            try {
                String filename = new File(downloadUrl.getPath()).getName();
                String suffix = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : ".download";
                if (".tmp".equals(suffix)) suffix = ".download"; // Avoid .tmp suffix conflict potentially
                tempFile = Files.createTempFile("topspin-update-", suffix);
                log.debug("Downloading to temporary file: {}", tempFile);

                request = HttpRequest.newBuilder(downloadUrl.toURI()).GET().build();

                HttpResponse<InputStream> response = downloadClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Download failed with HTTP status: " + response.statusCode());
                }

                long bytesRead = 0;
                byte[] buffer = new byte[8192]; // 8KB buffer
                int read;

                try (InputStream inputStream = response.body();
                     java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile.toFile())) {

                    while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                        outputStream.write(buffer, 0, read);
                        bytesRead += read;

                        if (totalSize > 0) {
                            currentProgress = (double) bytesRead / totalSize;
                            finalProgressCallback.accept(currentProgress);
                        } else {
                            finalProgressCallback.accept(0.0); // Indicate activity but unknown progress
                        }
                    }
                }

                currentProgress = 1.0;
                finalProgressCallback.accept(1.0);

                currentState = UpdateState.IDLE;
                UpdateResult result = new UpdateResult.DownloadOk(tempFile.toFile());
                lastResult.set(result);
                log.info("Download complete: {} ({} bytes)", tempFile, bytesRead);
                return tempFile.toFile();

            } catch (IOException | InterruptedException | URISyntaxException e) {
                log.error("Download failed", e);
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException suppress) {
                        log.error("Failed to delete temporary download file: {}", tempFile, suppress);
                    }
                }
                UpdateResult result = new UpdateResult.DownloadFailed(e);
                lastResult.set(result);
                currentState = UpdateState.ERROR;
                throw new RuntimeException("Download failed", e);
            }
        }, backgroundExecutor);
    }

    @Override
    public void launchInstaller(File installerFile) {
        Objects.requireNonNull(installerFile, "installerFile cannot be null");
        if (!installerFile.exists()) {
            throw new IllegalArgumentException("Installer file does not exist: " + installerFile.getAbsolutePath());
        }
        log.info("Attempting to launch installer: {}", installerFile.getAbsolutePath());

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(installerFile);
            } catch (IOException | SecurityException e) {
                log.error("Failed to launch installer via Desktop.open()", e);
                throw new RuntimeException("Failed to launch installer", e);
            }
        } else {
            String errorMsg = "Desktop.open() is not supported on this platform.";
            log.error(errorMsg);
            throw new UnsupportedOperationException(errorMsg);
        }
    }

    @Override
    public CompletableFuture<Void> triggerUpdateWorkflow() {
        log.info("Starting update workflow...");
        return checkAsync().thenComposeAsync(result -> {
            if (result instanceof UpdateResult.UpdateAvailable) {
                log.info("Update available, proceeding to download...");
                return downloadAsync(progress -> {
                    if (progress == 0.0 || progress == 1.0 || ((int) (progress * 100)) % 10 == 0) {
                        log.debug("Update download progress: {}%", (int) (progress * 100.0));
                    }
                });
            } else if (result instanceof UpdateResult.UpToDate) {
                log.info("Application is up-to-date. Workflow finished.");
                return CompletableFuture.completedFuture(null);
            } else {
                log.error("Update check failed. Workflow aborted.");
                throw new RuntimeException("Update check failed", ((UpdateResult.CheckFailed) result).cause());
            }
        }, backgroundExecutor).thenAcceptAsync(downloadedFile -> {
            if (downloadedFile != null) {
                log.info("Download successful. Launching installer and exiting application.");
                System.out.println("Download successful. Launching installer and exiting application.");
                try {
                    launchInstaller(downloadedFile);
                    // Give installer a moment to launch before exiting
                    Thread.sleep(1000);

                    // Use Platform.exit() if this is called from JavaFX thread
                    System.out.println("Exiting application to allow update...");
                    System.exit(0);

                } catch (Exception e) {
                    log.error("Failed to launch installer after download.", e);
                    throw new RuntimeException("Installer launch failed", e);
                }
            } else {
                log.info("No download occurred (up-to-date or check failed). Workflow finished.");
            }
        }, backgroundExecutor).exceptionally(ex -> {
            log.error("Update workflow failed.", ex);
            System.err.println("Update workflow failed: " + ex.getMessage());
            currentState = UpdateState.ERROR;
            lastResult.set(new UpdateResult.CheckFailed(ex));
            return null;
        });
    }

    private String determinePlatformAssetKey() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (os.contains("win")) {
            return "windows-x64";
        } else if (os.contains("mac")) {
            if (arch.contains("aarch64")) {
                return "macos-aarch64";
            } else {
                return "macos-x64";
            }
        } else if (os.contains("linux")) {
            if (arch.contains("amd64") || arch.contains("x86_64")) {
                return "linux-amd64";
            }
        }
        log.warn("Could not determine platform asset key for os={}, arch={}", os, arch);
        throw new IllegalStateException("Could not determine platform asset key");
    }
}