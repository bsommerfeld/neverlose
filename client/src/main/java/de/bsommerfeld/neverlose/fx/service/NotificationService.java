package de.bsommerfeld.neverlose.fx.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.fx.components.NotificationController;
import de.bsommerfeld.neverlose.fx.components.NotificationController.NotificationType;
import de.bsommerfeld.neverlose.fx.messages.MessagesResourceBundle;
import de.bsommerfeld.neverlose.fx.view.ViewProvider;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/** Service for managing notifications across the application. */
@Singleton
public class NotificationService {

  private final ViewProvider viewProvider;
  private VBox notificationContainer;

  @Inject
  public NotificationService(ViewProvider viewProvider) {
    this.viewProvider = viewProvider;
  }

  /**
   * Initializes the notification service with the notification container.
   *
   * @param notificationContainer the container for notifications
   */
  public void init(VBox notificationContainer) {
    this.notificationContainer = notificationContainer;
  }

  /**
   * Shows an information notification.
   *
   * @param message the message to display
   */
  public void showInfo(String message) {
    showNotification("Information", message, NotificationType.INFO, true, 5);
  }

  /**
   * Shows an information notification with a custom title.
   *
   * @param title the title of the notification
   * @param message the message to display
   */
  public void showInfo(String title, String message) {
    showNotification(title, message, NotificationType.INFO, true, 5);
  }

  /**
   * Shows a success notification.
   *
   * @param message the message to display
   */
  public void showSuccess(String message) {
    showNotification("Success", message, NotificationType.SUCCESS, true, 5);
  }

  /**
   * Shows a success notification with a custom title.
   *
   * @param title the title of the notification
   * @param message the message to display
   */
  public void showSuccess(String title, String message) {
    showNotification(title, message, NotificationType.SUCCESS, true, 5);
  }

  /**
   * Shows a warning notification.
   *
   * @param message the message to display
   */
  public void showWarning(String message) {
    showNotification("Warning", message, NotificationType.WARNING, true, 7);
  }

  /**
   * Shows a warning notification with a custom title.
   *
   * @param title the title of the notification
   * @param message the message to display
   */
  public void showWarning(String title, String message) {
    showNotification(title, message, NotificationType.WARNING, true, 7);
  }

  /**
   * Shows an error notification.
   *
   * @param message the message to display
   */
  public void showError(String message) {
    showNotification("Error", message, NotificationType.ERROR, true, 10);
  }

  /**
   * Shows an error notification with a custom title.
   *
   * @param title the title of the notification
   * @param message the message to display
   */
  public void showError(String title, String message) {
    showNotification(title, message, NotificationType.ERROR, true, 10);
  }

  /**
   * Shows a confirmation notification with confirm and cancel buttons.
   *
   * @param title the title of the notification
   * @param message the message to display
   * @param onConfirm the action to perform when the user confirms
   * @param onCancel the action to perform when the user cancels
   */
  public void showConfirmation(
      String title, String message, Runnable onConfirm, Runnable onCancel) {
    showConfirmation(title, message, "Confirm", "Cancel", onConfirm, onCancel);
  }

  /**
   * Shows a confirmation notification with custom button texts.
   *
   * @param title the title of the notification
   * @param message the message to display
   * @param confirmText the text for the confirm button
   * @param cancelText the text for the cancel button
   * @param onConfirm the action to perform when the user confirms
   * @param onCancel the action to perform when the user cancels
   */
  public void showConfirmation(
      String title,
      String message,
      String confirmText,
      String cancelText,
      Runnable onConfirm,
      Runnable onCancel) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              getClass().getResource("/de/bsommerfeld/neverlose/fx/components/Notification.fxml"));
      // Set the resource bundle for internationalization
      ResourceBundle resourceBundle = new MessagesResourceBundle();
      loader.setResources(resourceBundle);
      Node notification = loader.load();
      NotificationController controller = loader.getController();

      controller.setType(NotificationType.WARNING);
      controller.setTitle(title);
      controller.setMessage(message);
      controller.setAutoHide(false);

      controller.setConfirmation(
          confirmText,
          cancelText,
          confirmed -> {
            if (confirmed && onConfirm != null) {
              onConfirm.run();
            } else if (!confirmed && onCancel != null) {
              onCancel.run();
            }
          });

      controller.setOnClose(() -> removeNotification(notification));

      // Add notification to container and show it after it's been added
      if (notificationContainer == null) {
        throw new IllegalStateException(
            "Notification container not initialized. Call init() first.");
      }

      Platform.runLater(
          () -> {
            // Stelle sicher, dass nur die sichtbaren Elemente der Benachrichtigung klickbar sind
            notification.setMouseTransparent(false);
            notification.setPickOnBounds(
                false); // Nur die tatsächlich sichtbaren Bereiche fangen Mausevents ab
            // Add at the beginning (bottom) of the container
            notificationContainer.getChildren().add(0, notification);
            // Show the notification after it's been added to the container
            controller.show();
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shows a notification with the specified parameters.
   *
   * @param title the title of the notification
   * @param message the message to display
   * @param type the type of notification
   * @param autoHide whether the notification should automatically hide
   * @param durationSeconds the duration in seconds for which the notification should be shown
   */
  private void showNotification(
      String title,
      String message,
      NotificationType type,
      boolean autoHide,
      double durationSeconds) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              getClass().getResource("/de/bsommerfeld/neverlose/fx/components/Notification.fxml"));
      // Set the resource bundle for internationalization
      ResourceBundle resourceBundle = new MessagesResourceBundle();
      loader.setResources(resourceBundle);
      Node notification = loader.load();
      NotificationController controller = loader.getController();

      controller.setType(type);
      controller.setTitle(title);
      controller.setMessage(message);
      controller.setAutoHide(autoHide);
      controller.setShowDuration(durationSeconds);

      controller.setOnClose(() -> removeNotification(notification));

      // Add notification to container and show it after it's been added
      if (notificationContainer == null) {
        throw new IllegalStateException(
            "Notification container not initialized. Call init() first.");
      }

      Platform.runLater(
          () -> {
            // Make the notification clickable (not mouse transparent)
            notification.setMouseTransparent(false);
            // Add at the beginning (bottom) of the container
            notificationContainer.getChildren().add(0, notification);
            // Show the notification after it's been added to the container
            controller.show();
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Removes a notification from the container.
   *
   * @param notification the notification node to remove
   */
  private void removeNotification(Node notification) {
    if (notificationContainer == null) {
      return;
    }

    Platform.runLater(
        () -> {
          notificationContainer.getChildren().remove(notification);
        });
  }
}
