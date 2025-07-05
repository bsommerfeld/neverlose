package de.bsommerfeld.neverlose.fx.util;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import de.bsommerfeld.neverlose.fx.service.NotificationService;
import de.bsommerfeld.neverlose.fx.components.NotificationController.NotificationType;

/**
 * Utility class for creating and showing dialog windows. Centralizes dialog creation and styling to
 * avoid code duplication.
 */
public final class DialogUtils {

  private DialogUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated.");
  }

  /**
   * Shows an alert dialog with the given parameters.
   *
   * @param alertType the type of alert
   * @param title the title of the alert
   * @param header the header text of the alert (can be null)
   * @param content the content text of the alert
   * @param styleSource the parent node to get stylesheets from
   * @param notificationService the notification service to use
   */
  public static void showAlert(
      Alert.AlertType alertType, String title, String header, String content, Parent styleSource,
      NotificationService notificationService) {

    // Use the notification service instead of Alert
    String displayTitle = title;
    String displayContent = header != null && !header.isEmpty() ? header + "\n" + content : content;

    switch (alertType) {
      case INFORMATION:
        notificationService.showInfo(displayTitle, displayContent);
        break;
      case WARNING:
        notificationService.showWarning(displayTitle, displayContent);
        break;
      case ERROR:
        notificationService.showError(displayTitle, displayContent);
        break;
      case CONFIRMATION:
        // For confirmation, we should use showConfirmationDialog instead
        notificationService.showInfo(displayTitle, displayContent);
        break;
      default:
        notificationService.showInfo(displayTitle, displayContent);
        break;
    }
  }

  /**
   * Shows a confirmation dialog and returns the user's response.
   *
   * @param title the title of the dialog
   * @param header the header text of the dialog
   * @param content the content text of the dialog
   * @param styleSource the parent node to get stylesheets from
   * @param notificationService the notification service to use
   * @return an Optional containing the user's response (ButtonType.OK if confirmed)
   */
  public static Optional<ButtonType> showConfirmationDialog(
      String title, String header, String content, Parent styleSource,
      NotificationService notificationService) {

    // Create a CompletableFuture to handle the asynchronous response
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    String displayContent = header != null && !header.isEmpty() ? header + "\n" + content : content;

    notificationService.showConfirmation(
        title,
        displayContent,
        "OK",
        "Cancel",
        () -> future.complete(true),
        () -> future.complete(false));

    // Try to get the result synchronously to maintain compatibility with the old API
    try {
      boolean result = future.get();
      return result ? Optional.of(ButtonType.OK) : Optional.of(ButtonType.CANCEL);
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Applies the stylesheets from the source node to the dialog pane.
   *
   * @param dialogPane the dialog pane to apply stylesheets to
   * @param styleSource the parent node to get stylesheets from
   */
  private static void applyStylesheets(DialogPane dialogPane, Parent styleSource) {
    if (styleSource != null && styleSource.getScene() != null) {
      dialogPane.getStylesheets().addAll(styleSource.getScene().getStylesheets());
    }
  }

  /**
   * Closes the dialog window containing the given node.
   *
   * @param node a node in the dialog to close
   */
  public static void closeDialog(Parent node) {
    if (node != null && node.getScene() != null && node.getScene().getWindow() != null) {
      Stage stage = (Stage) node.getScene().getWindow();
      stage.close();
    }
  }
}
