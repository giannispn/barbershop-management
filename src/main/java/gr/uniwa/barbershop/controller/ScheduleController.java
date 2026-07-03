package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Appointment;
import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.Employee;
import gr.uniwa.barbershop.service.AppointmentService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Read-only view of a day's appointments, sorted by time. No editing here —
 * bookings are created/changed in the Appointments screen.
 */
public class ScheduleController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private DatePicker scheduleDate;
    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colCustomer;
    @FXML private TableColumn<Appointment, String> colEmployee;
    @FXML private TableColumn<Appointment, String> colService;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private Label countLabel;

    private final AppointmentService service = new AppointmentService();
    private final ObservableList<Appointment> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTime.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getStartTime().format(TIME_FMT)));
        colCustomer.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(fullName(cd.getValue().getCustomer())));
        colEmployee.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(fullName(cd.getValue().getEmployee())));
        colService.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getService() == null ? ""
                : cd.getValue().getService().getName()));
        colStatus.setCellValueFactory(cd ->
            new ReadOnlyStringWrapper(cd.getValue().getStatus().getDisplayName()));

        table.setItems(data);
        table.setPlaceholder(new Label("Δεν υπάρχουν ραντεβού για αυτή την ημέρα."));

        scheduleDate.setValue(LocalDate.now());
        load();
    }

    @FXML
    private void load() {
        LocalDate day = scheduleDate.getValue();
        if (day == null) { day = LocalDate.now(); scheduleDate.setValue(day); }
        try {
            data.setAll(service.getDailySchedule(day));
            countLabel.setText(data.size() + " ραντεβού");
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void today() {
        scheduleDate.setValue(LocalDate.now());
        load();
    }

    private String fullName(Object person) {
        if (person instanceof Customer c) return c.getFullName();
        if (person instanceof Employee e) return e.getFullName();
        return "";
    }
}
