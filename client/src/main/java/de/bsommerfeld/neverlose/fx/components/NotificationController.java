package de.bsommerfeld.neverlose.fx.components;

import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Controller for the notification component. */
public class NotificationController {

  @FXML private StackPane iconContainer;

  @FXML private SVGPath iconPath;

  @FXML private Label titleLabel;

  @FXML private Label messageLabel;

  @FXML private Button closeButton;

  @FXML private HBox actionButtonsContainer;

  @FXML private Button cancelButton;

  @FXML private Button confirmButton;

  @FXML private StackPane notificationRoot;

  @FXML private HBox notificationCard;

  private Runnable onClose;
  private Consumer<Boolean> onConfirmation;
  private boolean autoHide = true;
  private Duration showDuration = Duration.seconds(5);
  // The auto-dismiss timer
  private PauseTransition autoHideTimer;

  /** Initializes the controller. */
  @FXML
  private void initialize() {
    notificationRoot.setMouseTransparent(false);
    notificationCard.setMouseTransparent(false);

    notificationRoot.setPickOnBounds(false); // Only the visible components can catch mouse events

    // Set hand cursor for the notification root to indicate it's clickable
    notificationRoot.setCursor(Cursor.HAND);

    // Add click handler to the notification root
    notificationRoot.setOnMouseClicked(
        event -> {
          // Don't handle clicks on the close button (let the close button handle those)
          if (event.getTarget() != closeButton) {
            handleClose();
          }
        });

    // Set hand cursor for the close button
    closeButton.setCursor(Cursor.HAND);
  }

  /**
   * Sets the notification type and updates the icon accordingly.
   *
   * @param type the notification type
   */
  public void setType(NotificationType type) {
    // Set icon and styles based on type
    iconContainer.getStyleClass().clear();
    iconContainer.getStyleClass().addAll("notification-icon-container", type.getStyleClass());

    iconPath.setContent(type.getIconPath());
  }

  /**
   * Sets the title of the notification.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  /**
   * Sets the message of the notification.
   *
   * @param message the message
   */
  public void setMessage(String message) {
    messageLabel.setText(message);
  }

  /**
   * Sets whether the notification should automatically hide after a delay.
   *
   * @param autoHide true if the notification should auto-hide, false otherwise
   */
  public void setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
  }

  /**
   * Sets the duration for which the notification should be shown before auto-hiding.
   *
   * @param duration the duration in seconds
   */
  public void setShowDuration(double duration) {
    this.showDuration = Duration.seconds(duration);
  }

  /**
   * Sets the callback to be invoked when the notification is closed.
   *
   * @param onClose the callback
   */
  public void setOnClose(Runnable onClose) {
    this.onClose = onClose;
  }

  /**
   * Configures the notification for confirmation with custom button texts.
   *
   * @param confirmText the text for the confirm button
   * @param cancelText the text for the cancel button
   * @param onConfirmation the callback to be invoked when the user confirms or cancels
   */
  public void setConfirmation(
      String confirmText, String cancelText, Consumer<Boolean> onConfirmation) {
    this.onConfirmation = onConfirmation;
    this.autoHide = false;

    confirmButton.setText(confirmText);
    cancelButton.setText(cancelText);

    confirmButton.setVisible(true);
    confirmButton.setManaged(true);
    cancelButton.setVisible(true);
    cancelButton.setManaged(true);
  }

  /** Shows the notification with a fade-in animation and sets up auto-hide if enabled. */
  public void show() {
    // Set initial opacity to 0
    notificationRoot.setOpacity(0);

    // Create fade-in animation
    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationRoot);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);

    // Play the fade-in animation
    fadeIn.play();

    // Set up auto-hide timer if enabled
    if (autoHide) {
      // Create pause for auto-hide
      autoHideTimer = new PauseTransition(showDuration);
      autoHideTimer.setOnFinished(event -> dismiss());

      // Start the timer
      autoHideTimer.play();
    }
  }

  /**
   * Dismisses the notification with a fade-out animation. The onClose callback will handle removing
   * the notification from its parent container.
   */
  public void dismiss() {
    // Stop any running auto-hide timer
    if (autoHideTimer != null) {
      autoHideTimer.stop();
    }

    // Create fade-out animation
    FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationRoot);
    fadeOut.setFromValue(notificationRoot.getOpacity());
    fadeOut.setToValue(0);
    fadeOut.setOnFinished(
        event -> {
          // Call close to invoke any registered callbacks
          // The onClose callback will handle removing the notification from its parent
          close();
        });

    // Play the fade-out animation
    fadeOut.play();
  }

  /** Handles the close button action. */
  @FXML
  private void handleClose() {
    dismiss();
  }

  /** Handles the confirm button action. */
  @FXML
  private void handleConfirm() {
    if (onConfirmation != null) {
      onConfirmation.accept(true);
    }
    dismiss();
  }

  /** Handles the cancel button action. */
  @FXML
  private void handleCancel() {
    if (onConfirmation != null) {
      onConfirmation.accept(false);
    }
    dismiss();
  }

  /** Closes the notification and invokes the onClose callback if set. */
  private void close() {
    if (onClose != null) {
      onClose.run();
    }
  }

  /** Enum representing the different types of notifications. */
  public enum NotificationType {
    INFO(
        "notification-info",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"),
    SUCCESS(
        "notification-success",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"),
    WARNING("notification-warning", "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"),
    ERROR(
        "notification-error",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");

    private final String styleClass;
    private final String iconPath;

    NotificationType(String styleClass, String iconPath) {
      this.styleClass = styleClass;
      this.iconPath = iconPath;
    }

    public String getStyleClass() {
      return styleClass;
    }

    public String getIconPath() {
      return iconPath;
    }
  }
}
