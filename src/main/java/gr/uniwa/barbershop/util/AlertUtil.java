package gr.uniwa.barbershop.util;

import javafx.scene.control.Alert;

/**
 * Small helper for showing standard JavaFX dialogs from controllers.
 */
public final class AlertUtil {

    private AlertUtil() {
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Σφάλμα", message);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Ενημέρωση", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                    .filter(b -> b == javafx.scene.control.ButtonType.OK)
                    .isPresent();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
