package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Employee;
import gr.uniwa.barbershop.service.EmployeeService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controller for the employees screen (Admin only). Manages employee records
 * and computes earnings (base salary + commission) via {@link EmployeeService}.
 */
public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String>     colName;
    @FXML private TableColumn<Employee, String>     colSpecialty;
    @FXML private TableColumn<Employee, BigDecimal> colSalary;
    @FXML private TableColumn<Employee, BigDecimal> colCommission;
    @FXML private TableColumn<Employee, Boolean>    colActive;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField specialtyField;
    @FXML private TextField salaryField;
    @FXML private TextField commissionField;
    @FXML private CheckBox  activeCheck;
    @FXML private Label     formTitle;

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label      earningsLabel;

    private final EmployeeService service = new EmployeeService();
    private final ObservableList<Employee> data = FXCollections.observableArrayList();

    private Employee editing;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        colCommission.setCellValueFactory(new PropertyValueFactory<>("commissionRate"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        employeeTable.setItems(data);
        employeeTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, selected) -> {
                if (selected != null) {
                    populateForm(selected);
                }
            });

        // default earnings range: first day of current month -> today
        fromDate.setValue(LocalDate.now().withDayOfMonth(1));
        toDate.setValue(LocalDate.now());

        refresh();
    }

    private void refresh() {
        try {
            data.setAll(service.getAll());
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void saveEmployee() {
        try {
            Employee e = (editing != null) ? editing : new Employee();
            e.setFirstName(text(firstNameField));
            e.setLastName(text(lastNameField));
            e.setPhone(text(phoneField));
            e.setSpecialty(text(specialtyField));
            e.setBaseSalary(parseDecimal(salaryField.getText()));
            e.setCommissionRate(parseDecimal(commissionField.getText()));
            e.setActive(activeCheck.isSelected());

            service.save(e);
            AlertUtil.info(editing != null ? "Ο εργαζόμενος ενημερώθηκε."
                                           : "Ο εργαζόμενος καταχωρήθηκε.");
            clearForm();
            refresh();
        } catch (BusinessException ex) {
            AlertUtil.error(ex.getMessage());
        }
    }

    @FXML
    private void deactivateEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.error("Επιλέξτε πρώτα έναν εργαζόμενο.");
            return;
        }
        if (!AlertUtil.confirm("Απενεργοποίηση του εργαζομένου \""
                               + selected.getFullName() + "\";")) {
            return;
        }
        try {
            service.deactivate(selected.getId());
            AlertUtil.info("Ο εργαζόμενος απενεργοποιήθηκε.");
            clearForm();
            refresh();
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void calculateEarnings() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.error("Επιλέξτε πρώτα έναν εργαζόμενο από τη λίστα.");
            return;
        }
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (from == null || to == null || to.isBefore(from)) {
            AlertUtil.error("Επιλέξτε έγκυρο εύρος ημερομηνιών (Από ≤ Έως).");
            return;
        }
        try {
            BigDecimal total = service.calculateEarnings(selected.getId(), from, to);
            earningsLabel.setText("Σύνολο: " + total + " €");
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        editing = null;
        formTitle.setText("Νέος Εργαζόμενος");
        employeeTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        specialtyField.clear();
        salaryField.clear();
        commissionField.clear();
        activeCheck.setSelected(true);
        earningsLabel.setText("");
    }

    private void populateForm(Employee e) {
        editing = e;
        formTitle.setText("Επεξεργασία: " + e.getFullName());
        firstNameField.setText(e.getFirstName());
        lastNameField.setText(e.getLastName());
        phoneField.setText(e.getPhone());
        specialtyField.setText(e.getSpecialty());
        salaryField.setText(e.getBaseSalary() == null ? "" : e.getBaseSalary().toPlainString());
        commissionField.setText(e.getCommissionRate() == null ? "" : e.getCommissionRate().toPlainString());
        activeCheck.setSelected(e.isActive());
        earningsLabel.setText("");
    }

    private String text(TextInputControl f) {
        String t = f.getText();
        return (t == null) ? null : t.trim();
    }

    /** Returns null on invalid input so service-layer validation reports it. */
    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
