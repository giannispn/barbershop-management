package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.App;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.service.AuthService;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Controller for the main dashboard. Reads the logged-in {@link User} from the
 * session, shows a greeting, and hides the employee-management button for any
 * role other than Admin. (The {@code EmployeeService} also enforces this on the
 * server side as a second line of defense.)
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label contentLabel;
    @FXML private Button employeesButton;
    @FXML private StackPane contentArea;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        User current = SessionManager.getCurrentUser();
        if (current == null) {
            // should not happen if login flow is respected
            return;
        }

        welcomeLabel.setText("Καλώς ήρθατε,\n" + current.getUsername());
        roleLabel.setText(current.getRole().getDisplayName());

        // Role-based UI: only Admin sees employee management.
        boolean isAdmin = current.isAdmin();
        employeesButton.setVisible(isAdmin);
        employeesButton.setManaged(isAdmin); // also reclaim the layout space
    }

    // --- navigation handlers ---

    @FXML private void openCustomers()    { loadView("/gr/uniwa/barbershop/view/customers.fxml"); }
    @FXML private void openServices()     { loadView("/gr/uniwa/barbershop/view/services.fxml"); }
    @FXML private void openAppointments() { loadView("/gr/uniwa/barbershop/view/appointments.fxml"); }
    @FXML private void openSchedule()     { loadView("/gr/uniwa/barbershop/view/schedule.fxml"); }
    @FXML private void openPayments()     { loadView("/gr/uniwa/barbershop/view/payments.fxml"); }

    @FXML
    private void openEmployees() {
        // Guard again here in case the button is reached programmatically.
        User current = SessionManager.getCurrentUser();
        if (current == null || !current.isAdmin()) {
            AlertUtil.error("Δεν έχετε δικαίωμα πρόσβασης σε αυτή τη λειτουργία.");
            return;
        }
        loadView("/gr/uniwa/barbershop/view/employees.fxml");
    }

    @FXML
    private void handleLogout() {
        if (!AlertUtil.confirm("Θέλετε σίγουρα να αποσυνδεθείτε;")) {
            return;
        }
        authService.logout();
        try {
            App.showScene("/gr/uniwa/barbershop/view/login.fxml",
                    "Σύνδεση", 360, 340);
        } catch (Exception e) {
            AlertUtil.error("Σφάλμα κατά την αποσύνδεση.");
            e.printStackTrace();
        }
    }

    /**
     * Loads a feature screen's FXML into the content area, replacing whatever
     * was there before. This is how each menu button shows its screen.
     */
    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            AlertUtil.error("Αποτυχία φόρτωσης της οθόνης.");
            e.printStackTrace();
        }
    }

    /** Placeholder for feature screens not yet built. */
    private void setContent(String featureName) {
        contentLabel.setText("Ενότητα: " + featureName + " (υπό κατασκευή)");
        contentArea.getChildren().setAll(contentLabel);
    }
}