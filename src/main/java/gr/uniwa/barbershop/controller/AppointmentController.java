package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Appointment;
import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.Employee;
import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.service.AppointmentService;
import gr.uniwa.barbershop.service.CustomerService;
import gr.uniwa.barbershop.service.EmployeeService;
import gr.uniwa.barbershop.service.ServiceService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller for the appointments screen. Shows a daily schedule sorted by time
 * and lets the user create / update / cancel bookings. The double-booking rule
 * lives in {@link AppointmentService}; this controller just reports its result.
 */
public class AppointmentController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private DatePicker scheduleDate;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colCustomer;
    @FXML private TableColumn<Appointment, String> colEmployee;
    @FXML private TableColumn<Appointment, String> colService;
    @FXML private TableColumn<Appointment, String> colStatus;

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<Service>  serviceCombo;
    @FXML private DatePicker apptDate;
    @FXML private ComboBox<String>   timeCombo;
    @FXML private TextField          notesField;
    @FXML private Label              formTitle;

    private final AppointmentService appointmentService = new AppointmentService();
    private final CustomerService customerService = new CustomerService();
    private final EmployeeService employeeService = new EmployeeService();
    private final ServiceService serviceService = new ServiceService();

    private final ObservableList<Appointment> data = FXCollections.observableArrayList();

    private Appointment editing;

    @FXML
    public void initialize() {
        // table columns (derived/formatted values)
        colTime.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getStartTime().format(TIME_FMT)));
        colCustomer.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(name(cd.getValue().getCustomer())));
        colEmployee.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(name(cd.getValue().getEmployee())));
        colService.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getService() == null ? ""
                : cd.getValue().getService().getName()));
        colStatus.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getStatus().getDisplayName()));

        appointmentTable.setItems(data);
        appointmentTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> { if (sel != null) populateForm(sel); });

        // time slots 08:00 - 20:00 every 30'
        for (int h = 8; h <= 20; h++) {
            timeCombo.getItems().add(String.format("%02d:00", h));
            if (h < 20) timeCombo.getItems().add(String.format("%02d:30", h));
        }

        scheduleDate.setValue(LocalDate.now());
        apptDate.setValue(LocalDate.now());

        loadDropdowns();
        loadSchedule();
    }

    private void loadDropdowns() {
        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerService.search(null)));
            employeeCombo.setItems(FXCollections.observableArrayList(employeeService.getSelectable()));
            serviceCombo.setItems(FXCollections.observableArrayList(serviceService.getActive()));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void loadSchedule() {
        LocalDate day = scheduleDate.getValue();
        if (day == null) { day = LocalDate.now(); scheduleDate.setValue(day); }
        try {
            data.setAll(appointmentService.getDailySchedule(day));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void saveAppointment() {
        Customer c = customerCombo.getValue();
        Employee e = employeeCombo.getValue();
        Service  s = serviceCombo.getValue();
        if (c == null || e == null || s == null) {
            AlertUtil.error("Επιλέξτε πελάτη, εργαζόμενο και υπηρεσία.");
            return;
        }
        LocalDateTime start = buildStart();
        if (start == null) return;   // error already shown

        try {
            Appointment a = (editing != null) ? editing : new Appointment();
            a.setCustomerId(c.getId());
            a.setEmployeeId(e.getId());
            a.setServiceId(s.getId());
            a.setStartTime(start);
            a.setNotes(text(notesField));

            if (editing != null) {
                appointmentService.update(a);
                AlertUtil.info("Το ραντεβού ενημερώθηκε.");
            } else {
                appointmentService.create(a);
                AlertUtil.info("Το ραντεβού καταχωρήθηκε.");
            }
            clearForm();
            loadSchedule();
        } catch (BusinessException ex) {
            // includes the double-booking message
            AlertUtil.error(ex.getMessage());
        }
    }

    @FXML
    private void cancelAppointment() {
        Appointment sel = appointmentTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.error("Επιλέξτε πρώτα ένα ραντεβού από τη λίστα.");
            return;
        }
        if (!AlertUtil.confirm("Ακύρωση του ραντεβού στις "
                               + sel.getStartTime().format(TIME_FMT) + ";")) {
            return;
        }
        try {
            appointmentService.cancel(sel.getId(), "Ακύρωση από χρήστη");
            AlertUtil.info("Το ραντεβού ακυρώθηκε.");
            clearForm();
            loadSchedule();
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        editing = null;
        formTitle.setText("Νέο Ραντεβού");
        appointmentTable.getSelectionModel().clearSelection();
        customerCombo.getSelectionModel().clearSelection();
        employeeCombo.getSelectionModel().clearSelection();
        serviceCombo.getSelectionModel().clearSelection();
        apptDate.setValue(scheduleDate.getValue());
        timeCombo.getEditor().clear();
        timeCombo.setValue(null);
        notesField.clear();
    }

    private void populateForm(Appointment a) {
        editing = a;
        formTitle.setText("Επεξεργασία ραντεβού");
        selectById(customerCombo, a.getCustomerId());
        selectById(employeeCombo, a.getEmployeeId());
        selectServiceById(a.getServiceId());
        apptDate.setValue(a.getStartTime().toLocalDate());
        timeCombo.setValue(a.getStartTime().format(TIME_FMT));
        notesField.setText(a.getNotes());
    }

    /** Builds the start datetime from the form's date + time, or null on error. */
    private LocalDateTime buildStart() {
        LocalDate date = apptDate.getValue();
        if (date == null) {
            AlertUtil.error("Επιλέξτε ημερομηνία.");
            return null;
        }
        String t = timeCombo.getValue();
        if (t == null || t.isBlank()) {
            AlertUtil.error("Επιλέξτε ή πληκτρολογήστε ώρα (π.χ. 10:30).");
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(t.trim(), TIME_FMT);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException ex) {
            AlertUtil.error("Μη έγκυρη ώρα. Χρησιμοποιήστε τη μορφή ΩΩ:ΛΛ (π.χ. 09:00).");
            return null;
        }
    }

    // --- helpers ---

    private void selectById(ComboBox<? extends Object> combo, int id) {
        for (Object o : combo.getItems()) {
            int oid = (o instanceof Customer cu) ? cu.getId()
                    : (o instanceof Employee em) ? em.getId() : -1;
            if (oid == id) {
                @SuppressWarnings("unchecked")
                ComboBox<Object> c = (ComboBox<Object>) combo;
                c.getSelectionModel().select(o);
                return;
            }
        }
    }

    private void selectServiceById(int id) {
        for (Service s : serviceCombo.getItems()) {
            if (s.getId() == id) {
                serviceCombo.getSelectionModel().select(s);
                return;
            }
        }
    }

    private String name(Object person) {
        if (person instanceof Customer c) return c.getFullName();
        if (person instanceof Employee e) return e.getFullName();
        return "";
    }

    private String text(TextInputControl f) {
        String t = f.getText();
        return (t == null) ? null : t.trim();
    }
}
