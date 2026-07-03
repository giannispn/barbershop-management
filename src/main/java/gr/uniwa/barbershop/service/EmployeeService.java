package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.AppointmentDAO;
import gr.uniwa.barbershop.dao.EmployeeDAO;
import gr.uniwa.barbershop.model.Employee;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for employee management (Admin only) and earnings calculation.
 *
 * <p>Total earnings = base salary + commission, where commission is
 * {@code commission_rate%} of the total price of the employee's COMPLETED
 * services in the period.</p>
 */
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public List<Employee> getAll() throws BusinessException {
        requireAdmin();
        try {
            return employeeDAO.findAll();
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης εργαζομένων.", e);
        }
    }

    /**
     * Returns the active employees for selection in dropdowns (e.g. when
     * booking an appointment). Available to all roles, since booking is not an
     * admin-only operation.
     */
    public List<Employee> getSelectable() throws BusinessException {
        try {
            return employeeDAO.findAll().stream()
                    .filter(Employee::isActive)
                    .toList();
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης εργαζομένων.", e);
        }
    }

    public void save(Employee e) throws BusinessException {
        requireAdmin();
        validate(e);
        try {
            if (e.getId() > 0) {
                employeeDAO.update(e);
            } else {
                employeeDAO.insert(e);
            }
        } catch (SQLException ex) {
            throw new BusinessException("Αποτυχία αποθήκευσης εργαζομένου.", ex);
        }
    }

    public void deactivate(int employeeId) throws BusinessException {
        requireAdmin();
        try {
            employeeDAO.deactivate(employeeId);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία απενεργοποίησης εργαζομένου.", e);
        }
    }

    /**
     * Computes total earnings for an employee over a date range:
     * base salary + (completed-service total * commission_rate / 100).
     */
    public BigDecimal calculateEarnings(int employeeId, LocalDate from, LocalDate to)
            throws BusinessException {
        requireAdmin();
        try {
            Employee e = employeeDAO.findById(employeeId)
                    .orElseThrow(() -> new BusinessException("Ο εργαζόμενος δεν βρέθηκε."));

            BigDecimal completedTotal =
                    appointmentDAO.completedServiceTotal(employeeId, from, to);

            BigDecimal commission = completedTotal
                    .multiply(e.getCommissionRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            return e.getBaseSalary().add(commission).setScale(2, RoundingMode.HALF_UP);

        } catch (SQLException ex) {
            throw new BusinessException("Αποτυχία υπολογισμού απολαβών.", ex);
        }
    }

    private void requireAdmin() throws BusinessException {
        User current = SessionManager.getCurrentUser();
        if (current == null || !current.isAdmin()) {
            throw new BusinessException(
                    "Μόνο ο διαχειριστής έχει πρόσβαση στη διαχείριση εργαζομένων.");
        }
    }

    private void validate(Employee e) throws BusinessException {
        if (e == null) {
            throw new BusinessException("Δεν υπάρχουν στοιχεία εργαζομένου.");
        }
        if (isBlank(e.getFirstName()) || isBlank(e.getLastName())) {
            throw new BusinessException("Το όνομα και το επώνυμο είναι υποχρεωτικά.");
        }
        if (e.getBaseSalary() == null || e.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Ο βασικός μισθός δεν μπορεί να είναι αρνητικός.");
        }
        if (e.getCommissionRate() == null
                || e.getCommissionRate().compareTo(BigDecimal.ZERO) < 0
                || e.getCommissionRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Το ποσοστό προμήθειας πρέπει να είναι 0–100.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}