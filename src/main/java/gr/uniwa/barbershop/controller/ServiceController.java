package gr.uniwa.barbershop.controller;

import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.service.ServiceService;
import gr.uniwa.barbershop.util.AlertUtil;
import gr.uniwa.barbershop.util.BusinessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

/**
 * Controller for the services screen. Lists services and lets the user add or
 * edit them via {@link ServiceService}. Services are deactivated (Ενεργή
 * unchecked) rather than deleted, because appointments reference them.
 */
public class ServiceController {

    @FXML private TableView<Service> serviceTable;
    @FXML private TableColumn<Service, String>  colName;
    @FXML private TableColumn<Service, BigDecimal> colPrice;
    @FXML private TableColumn<Service, Integer> colDuration;
    @FXML private TableColumn<Service, Boolean> colActive;

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private CheckBox  activeCheck;
    @FXML private TextArea  descriptionField;
    @FXML private Label     formTitle;

    private final ServiceService service = new ServiceService();
    private final ObservableList<Service> data = FXCollections.observableArrayList();

    private Service editing;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        serviceTable.setItems(data);
        serviceTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, selected) -> {
                if (selected != null) {
                    populateForm(selected);
                }
            });

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
    private void saveService() {
        try {
            Service s = (editing != null) ? editing : new Service();
            s.setName(text(nameField));
            s.setDescription(text(descriptionField));
            s.setPrice(parsePrice(priceField.getText()));
            s.setDurationMinutes(parseInt(durationField.getText()));
            s.setActive(activeCheck.isSelected());

            service.save(s);
            AlertUtil.info(editing != null ? "Η υπηρεσία ενημερώθηκε."
                                           : "Η υπηρεσία καταχωρήθηκε.");
            clearForm();
            refresh();
        } catch (BusinessException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        editing = null;
        formTitle.setText("Νέα Υπηρεσία");
        serviceTable.getSelectionModel().clearSelection();
        nameField.clear();
        priceField.clear();
        durationField.clear();
        descriptionField.clear();
        activeCheck.setSelected(true);
    }

    private void populateForm(Service s) {
        editing = s;
        formTitle.setText("Επεξεργασία: " + s.getName());
        nameField.setText(s.getName());
        priceField.setText(s.getPrice() == null ? "" : s.getPrice().toPlainString());
        durationField.setText(String.valueOf(s.getDurationMinutes()));
        descriptionField.setText(s.getDescription());
        activeCheck.setSelected(s.isActive());
    }

    private String text(TextInputControl f) {
        String t = f.getText();
        return (t == null) ? null : t.trim();
    }

    /** Returns null on invalid input so the service-layer validation reports it. */
    private BigDecimal parsePrice(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
