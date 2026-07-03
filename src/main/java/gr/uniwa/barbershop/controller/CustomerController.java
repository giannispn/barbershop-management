package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.service.CustomerService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the customers screen. Loads customers into a table, lets the
 * user search, and add / update / delete via the {@link CustomerService}
 * (which enforces validation and the delete rules).
 */
public class CustomerController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String>  colName;
    @FXML private TableColumn<Customer, String>  colPhone;
    @FXML private TableColumn<Customer, String>  colEmail;
    @FXML private TableColumn<Customer, Integer> colLate;

    @FXML private TextField searchField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField lateField;
    @FXML private TextArea  preferencesField;
    @FXML private Label     formTitle;

    private final CustomerService service = new CustomerService();
    private final ObservableList<Customer> data = FXCollections.observableArrayList();

    /** The customer currently being edited (null = creating a new one). */
    private Customer editing;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLate.setCellValueFactory(new PropertyValueFactory<>("lateArrivalCount"));

        customerTable.setItems(data);

        // when a row is selected, fill the form for editing
        customerTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, selected) -> {
                if (selected != null) {
                    populateForm(selected);
                }
            });

        loadAll();
    }

    @FXML
    private void loadAll() {
        searchField.clear();
        refresh(null);
    }

    @FXML
    private void searchCustomers() {
        refresh(searchField.getText());
    }

    private void refresh(String term) {
        try {
            data.setAll(service.search(term));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void saveCustomer() {
        try {
            Customer c = (editing != null) ? editing : new Customer();
            c.setFirstName(text(firstNameField));
            c.setLastName(text(lastNameField));
            c.setPhone(text(phoneField));
            c.setEmail(text(emailField));
            c.setPreferences(text(preferencesField));
            c.setLateArrivalCount(parseIntSafe(lateField.getText()));

            if (editing != null) {
                service.update(c);
                AlertUtil.info("Ο πελάτης ενημερώθηκε.");
            } else {
                service.add(c);
                AlertUtil.info("Ο πελάτης καταχωρήθηκε.");
            }
            clearForm();
            refresh(null);
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void deleteCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.error("Επιλέξτε πρώτα έναν πελάτη από τη λίστα.");
            return;
        }
        if (!AlertUtil.confirm("Διαγραφή του πελάτη \"" + selected.getFullName() + "\";")) {
            return;
        }
        try {
            service.delete(selected.getId());
            AlertUtil.info("Ο πελάτης διαγράφηκε.");
            clearForm();
            refresh(null);
        } catch (BusinessException e) {
            // e.g. "requires admin approval" or "has active appointments"
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        editing = null;
        formTitle.setText("Νέος Πελάτης");
        customerTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
        lateField.clear();
        preferencesField.clear();
    }

    private void populateForm(Customer c) {
        editing = c;
        formTitle.setText("Επεξεργασία: " + c.getFullName());
        firstNameField.setText(c.getFirstName());
        lastNameField.setText(c.getLastName());
        phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail());
        lateField.setText(String.valueOf(c.getLateArrivalCount()));
        preferencesField.setText(c.getPreferences());
    }

    private String text(TextInputControl field) {
        String t = field.getText();
        return (t == null) ? null : t.trim();
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Math.max(0, Integer.parseInt(s.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
