package de.sommerfeld.topspin.fx.controller.component;

import com.google.inject.Inject;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.updater.model.UpdateResult;
import de.sommerfeld.topspin.updater.model.UpdateState;
import de.sommerfeld.topspin.updater.service.UpdateService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public class UpdateIndicatorController {

    private static final LogFacade log = LogFacadeFactory.getLogger();

    private final Image iconCheck = IconLoader.loadIcon("/icons/indicator/check.png");
    private final Image iconDownload = IconLoader.loadIcon("/icons/indicator/download.png");
    private final Image iconError = IconLoader.loadIcon("/icons/indicator/error.png");
    private final Image iconInfo = IconLoader.loadIcon("/icons/indicator/info.png");

    private final Tooltip statusTooltip = new Tooltip();

    private final UpdateService updateService;

    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView statusIcon;
    @FXML
    private ProgressIndicator progressIndicator;
    private File downloadedInstallerFile = null;

    @Inject
    public UpdateIndicatorController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @FXML
    private void initialize() {
        log.debug("Initializing UpdateIndicatorController...");
        Tooltip.install(rootPane, statusTooltip);
        rootPane.setCursor(Cursor.HAND);
        rootPane.setOnMouseClicked(this::handleIndicatorClick);
        updateVisuals(UpdateState.CHECKING, null);
        performCheck(false);
    }

    private void performCheck(boolean showErrorOnUpToDate) {
        if (updateService.getUpdateState() == UpdateState.CHECKING || updateService.getUpdateState() == UpdateState.DOWNLOADING) {
            log.debug("Check/Download already in progress.");
            return;
        }
        updateVisuals(UpdateState.CHECKING, null);
        updateService.checkAsync().whenCompleteAsync((result, error) -> {
            if (error != null) {
                updateVisuals(UpdateState.ERROR, new UpdateResult.CheckFailed(error));
            } else {
                updateVisuals(updateService.getUpdateState(), result);
                if (result instanceof UpdateResult.UpToDate && showErrorOnUpToDate) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Updater");
                    alert.setHeaderText("No new updates available.");
                    alert.setContentText("Your application version " + updateService.getCurrentVersion().value() + " is up-to-date.");
                    alert.showAndWait();
                }
            }
        }, Platform::runLater);
    }

    private void performDownloadAndInstall() {
        if (updateService.getUpdateState() == UpdateState.DOWNLOADING) {
            log.warn("Download already in progress.");
            return;
        }
        UpdateResult lastResult = updateService.getUpdateResult();
        if (!(lastResult instanceof UpdateResult.UpdateAvailable)) {
            log.warn("Tried to download, but no update available or last check failed.");
            updateVisuals(UpdateState.IDLE, lastResult);
            return;
        }

        updateVisuals(UpdateState.DOWNLOADING, lastResult);

        Consumer<Double> progressCallback = progress ->
                Platform.runLater(() -> {
                    progressIndicator.setProgress(progress);
                    statusTooltip.setText(String.format("Downloading update... (%.0f%%)", progress * 100.0));
                });

        updateService.downloadAsync(progressCallback)
                .thenAcceptAsync(downloadedFile -> {
                    this.downloadedInstallerFile = downloadedFile;
                    updateVisuals(UpdateState.IDLE, new UpdateResult.DownloadOk(downloadedFile));
                    statusTooltip.setText("Download complete. Click here to install.");
                }, Platform::runLater)
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        updateVisuals(UpdateState.ERROR, new UpdateResult.DownloadFailed(error));
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Updater Error");
                        alert.setHeaderText("Download failed.");
                        alert.setContentText("Could not download update: " + error.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    @FXML
    private void handleIndicatorClick(MouseEvent event) {
        UpdateState currentState = updateService.getUpdateState();
        UpdateResult lastResult = updateService.getUpdateResult();
        log.debug("Update indicator clicked. State: {}, Result: {}", currentState, lastResult);

        if (currentState == UpdateState.CHECKING || currentState == UpdateState.DOWNLOADING) {
            return;
        }

        if (lastResult instanceof UpdateResult.UpdateAvailable) {
            performDownloadAndInstall();
        } else if (lastResult instanceof UpdateResult.DownloadOk(File downloadedFile)) {
            try {
                updateService.launchInstaller(downloadedFile);
                log.info("Exiting application after launching installer...");
                Platform.exit();
            } catch (Exception e) {
                log.error("Failed to launch installer after click.", e);
                updateVisuals(UpdateState.ERROR, new UpdateResult.DownloadFailed(e));
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Updater Error");
                alert.setHeaderText("Launch Installer Failed");
                alert.setContentText("Could not launch installer: " + e.getMessage());
                alert.showAndWait();
            }
        } else {
            performCheck(true);
        }
    }

    private void updateVisuals(UpdateState state, UpdateResult result) {
        progressIndicator.setVisible(false);
        statusIcon.setVisible(true);

        String tooltipText = "";

        switch (state) {
            case IDLE:
                if (result instanceof UpdateResult.UpdateAvailable available) {
                    statusIcon.setImage(iconDownload);
                    tooltipText = "Update available (v" + available.latestVersion().value() + "). Click to download.";
                } else if (result instanceof UpdateResult.UpToDate) {
                    statusIcon.setImage(iconCheck);
                    tooltipText = "Application is up-to-date (v" + updateService.getCurrentVersion().value() + ")";
                } else if (result instanceof UpdateResult.DownloadOk(File downloadedFile)) {
                    statusIcon.setImage(iconInfo);
                    tooltipText = "Update downloaded. Click here to install.";
                    this.downloadedInstallerFile = downloadedFile;
                } else {
                    statusIcon.setImage(iconError);
                    String errorMsg = "... Click to retry check.";
                    if (result instanceof UpdateResult.CheckFailed(Throwable cause))
                        errorMsg = "Check failed: " + getShortErrorMessage(cause) + " Click to retry.";
                    if (result instanceof UpdateResult.DownloadFailed(Throwable cause))
                        errorMsg = "Download failed: " + getShortErrorMessage(cause) + " Click to retry check.";
                    tooltipText = errorMsg;
                }
                break;
            case CHECKING:
                statusIcon.setVisible(false);
                progressIndicator.setProgress(-1.0);
                progressIndicator.setVisible(true);
                tooltipText = "Checking for updates...";
                break;
            case DOWNLOADING:
                statusIcon.setVisible(false);
                progressIndicator.setVisible(true);
                tooltipText = "Downloading update...";
                break;
            case ERROR:
                statusIcon.setImage(iconError);
                tooltipText = "An error occurred. Click to retry check.";
                if (result instanceof UpdateResult.CheckFailed(Throwable cause))
                    tooltipText = "Check failed: " + getShortErrorMessage(cause) + " Click to retry.";
                if (result instanceof UpdateResult.DownloadFailed(Throwable cause))
                    tooltipText = "Download failed: " + getShortErrorMessage(cause) + " Click to retry check.";
                break;
        }
        statusTooltip.setText(tooltipText);
    }

    private String getShortErrorMessage(Throwable throwable) {
        if (throwable == null) return "Unknown error";
        Throwable cause = throwable;
        if (cause instanceof RuntimeException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
    }

    static class IconLoader {
        public static Image loadIcon(String resourcePath) {
            try {
                return new Image(Objects.requireNonNull(IconLoader.class.getResourceAsStream(resourcePath)));
            } catch (Exception e) {
                System.err.println("Failed to load icon resource: " + resourcePath + " - " + e.getMessage());
                return null;
            }
        }
    }
}
