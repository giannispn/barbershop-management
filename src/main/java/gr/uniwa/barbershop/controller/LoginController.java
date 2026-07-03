package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.App;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.service.AuthService;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login screen. Delegates authentication to
 * {@link AuthService} and, on success, swaps to the dashboard.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        loginButton.setDisable(true);
        try {
            User user = authService.login(
                usernameField.getText(), passwordField.getText());

            // success -> open the dashboard (DashboardController reads the session)
            App.showScene("/gr/uniwa/barbershop/view/dashboard.fxml",
                          "Διαχείριση Κουρείου", 900, 600);

        } catch (BusinessException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Απρόσμενο σφάλμα κατά τη σύνδεση.");
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
        }
    }
}
