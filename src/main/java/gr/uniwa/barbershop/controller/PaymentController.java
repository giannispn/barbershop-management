package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Appointment;
import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.Payment;
import gr.uniwa.barbershop.model.enums.PaymentMethod;
import gr.uniwa.barbershop.service.AppointmentService;
import gr.uniwa.barbershop.service.CustomerService;
import gr.uniwa.barbershop.service.PaymentService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the payments screen. The user picks a customer, sees that
 * customer's payments, and registers a new one (optionally linked to a specific
 * appointment). Validation/linking rules live in {@link PaymentService}.
 */
public class PaymentController {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> colDate;
    @FXML private TableColumn<Payment, String> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, String> colAppt;

    @FXML private ComboBox<Appointment>   appointmentCombo;
    @FXML private ComboBox<PaymentMethod> methodCombo;
    @FXML private TextField               amountField;

    private final PaymentService paymentService = new PaymentService();
    private final CustomerService customerService = new CustomerService();
    private final AppointmentService appointmentService = new AppointmentService();

    private final ObservableList<Payment> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
            cd.getValue().getPaidAt() == null ? "" : cd.getValue().getPaidAt().format(DT_FMT)));
        colAmount.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
            String.valueOf(cd.getValue().getAmount())));
        colMethod.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
            cd.getValue().getMethod().getDisplayName()));
        colAppt.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
            cd.getValue().getAppointmentId() == null ? "—"
                : "#" + cd.getValue().getAppointmentId()));

        paymentTable.setItems(data);

        methodCombo.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        try {
            customerCombo.setItems(
                FXCollections.observableArrayList(customerService.search(null)));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }

        // when a customer is picked, load their payments and appointments
        customerCombo.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, c) -> {
                if (c != null) {
                    loadPayments(c.getId());
                    loadAppointments(c.getId());
                }
            });
    }

    private void loadPayments(int customerId) {
        try {
            data.setAll(paymentService.getByCustomer(customerId));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    private void loadAppointments(int customerId) {
        try {
            appointmentCombo.setItems(
                FXCollections.observableArrayList(appointmentService.getByCustomer(customerId)));
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void registerPayment() {
        Customer c = customerCombo.getValue();
        if (c == null) {
            AlertUtil.error("Επιλέξτε πρώτα πελάτη.");
            return;
        }
        if (methodCombo.getValue() == null) {
            AlertUtil.error("Επιλέξτε τρόπο πληρωμής.");
            return;
        }
        BigDecimal amount = parseAmount(amountField.getText());

        try {
            Payment p = new Payment();
            p.setCustomerId(c.getId());
            Appointment appt = appointmentCombo.getValue();
            p.setAppointmentId(appt == null ? null : appt.getId());
            p.setAmount(amount);                 // may be null -> service reports it
            p.setMethod(methodCombo.getValue());

            paymentService.register(p);
            AlertUtil.info("Η πληρωμή καταχωρήθηκε.");
            clearForm();
            loadPayments(c.getId());
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        appointmentCombo.getSelectionModel().clearSelection();
        methodCombo.getSelectionModel().clearSelection();
        amountField.clear();
    }

    private BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
