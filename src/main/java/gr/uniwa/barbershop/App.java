package gr.uniwa.barbershop;

import gr.uniwa.barbershop.config.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Verifies the database is reachable, then
 * shows the login screen. Note: a separate {@link Launcher} class holds the
 * real {@code main} so the app launches correctly when packaged as a fat jar.
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        if (!DatabaseConnection.getInstance().testConnection()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Σφάλμα Βάσης Δεδομένων");
            alert.setHeaderText(null);
            alert.setContentText(
                "Δεν είναι δυνατή η σύνδεση με τη βάση δεδομένων.\n"
                + "Ελέγξτε το αρχείο db.properties και ότι ο PostgreSQL εκτελείται.");
            alert.showAndWait();
            return;
        }

        showScene("/gr/uniwa/barbershop/view/login.fxml", "Σύνδεση", 360, 340);
    }

    /** Swaps the scene on the primary stage (used to move login -> dashboard). */
    public static void showScene(String fxmlPath, String title,
                                 double width, double height) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);

        // optional global stylesheet
        var css = App.class.getResource("/gr/uniwa/barbershop/css/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
