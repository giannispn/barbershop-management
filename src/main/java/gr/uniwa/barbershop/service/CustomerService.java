package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.AppointmentDAO;
import gr.uniwa.barbershop.dao.CustomerDAO;
import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for customers: input validation, audit (who edited),
 * and the delete rules (no active appointments + admin approval).
 */
public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public List<Customer> getAll() throws BusinessException {
        try {
            return customerDAO.findAll();
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης πελατών.", e);
        }
    }

    public List<Customer> search(String term) throws BusinessException {
        try {
            return (term == null || term.isBlank())
                 ? customerDAO.findAll()
                 : customerDAO.search(term.trim());
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία αναζήτησης πελατών.", e);
        }
    }

    public void add(Customer c) throws BusinessException {
        validate(c);
        try {
            customerDAO.insert(c, SessionManager.currentUserId());
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία καταχώρησης πελάτη.", e);
        }
    }

    public void update(Customer c) throws BusinessException {
        validate(c);
        try {
            customerDAO.update(c, SessionManager.currentUserId());
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ενημέρωσης πελάτη.", e);
        }
    }

    /**
     * Deletes a customer, enforcing two rules from the spec:
     *   1. The acting user must be an Admin (deletion requires admin approval).
     *   2. The customer must have no active (SCHEDULED) appointments.
     */
    public void delete(int customerId) throws BusinessException {
        User current = SessionManager.getCurrentUser();
        if (current == null || !current.isAdmin()) {
            throw new BusinessException(
                "Η διαγραφή πελάτη απαιτεί έγκριση διαχειριστή.");
        }
        try {
            int active = appointmentDAO.countActiveForCustomer(customerId);
            if (active > 0) {
                throw new BusinessException(
                    "Δεν επιτρέπεται η διαγραφή: ο πελάτης έχει "
                    + active + " ενεργό/ά ραντεβού.");
            }
            customerDAO.delete(customerId);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία διαγραφής πελάτη.", e);
        }
    }

    /** Validates mandatory fields (prevents empty required input). */
    private void validate(Customer c) throws BusinessException {
        if (c == null) {
            throw new BusinessException("Δεν υπάρχουν στοιχεία πελάτη.");
        }
        if (isBlank(c.getFirstName()) || isBlank(c.getLastName())) {
            throw new BusinessException("Το όνομα και το επώνυμο είναι υποχρεωτικά.");
        }
        if (isBlank(c.getPhone())) {
            throw new BusinessException("Το τηλέφωνο είναι υποχρεωτικό.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
