package de.bsommerfeld.neverlose.fx.util;

import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for creating and showing dialog windows.
 * Centralizes dialog creation and styling to avoid code duplication.
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
     */
    public static void showAlert(Alert.AlertType alertType, String title, String header, 
                                String content, Parent styleSource) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        applyStylesheets(alert.getDialogPane(), styleSource);

        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns the user's response.
     *
     * @param title the title of the dialog
     * @param header the header text of the dialog
     * @param content the content text of the dialog
     * @param styleSource the parent node to get stylesheets from
     * @return an Optional containing the user's response (ButtonType.OK if confirmed)
     */
    public static Optional<ButtonType> showConfirmationDialog(String title, String header, 
                                                            String content, Parent styleSource) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(title);
        confirmDialog.setHeaderText(header);
        confirmDialog.setContentText(content);

        applyStylesheets(confirmDialog.getDialogPane(), styleSource);

        return confirmDialog.showAndWait();
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