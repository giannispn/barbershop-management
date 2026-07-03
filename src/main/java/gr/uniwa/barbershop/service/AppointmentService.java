package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.AppointmentDAO;
import gr.uniwa.barbershop.dao.ServiceDAO;
import gr.uniwa.barbershop.model.Appointment;
import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.model.enums.AppointmentStatus;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for appointments. Enforces the core double-booking rule and
 * computes the end time from the chosen service's duration.
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<Appointment> getDailySchedule(LocalDate date) throws BusinessException {
        try {
            return appointmentDAO.findByDate(date);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης ημερήσιου προγράμματος.", e);
        }
    }

    public List<Appointment> getByCustomer(int customerId) throws BusinessException {
        try {
            return appointmentDAO.findByCustomer(customerId);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης ραντεβού πελάτη.", e);
        }
    }

    /**
     * Creates a new appointment. Validates required references, derives the
     * end time from the service duration, and blocks double-booking.
     */
    public void create(Appointment a) throws BusinessException {
        prepareAndValidate(a, null);
        try {
            a.setStatus(AppointmentStatus.SCHEDULED);
            a.setCreatedBy(SessionManager.currentUserId());
            appointmentDAO.insert(a);
        } catch (SQLException e) {
            throw translate(e, "Αποτυχία δημιουργίας ραντεβού.");
        }
    }

    /** Updates an existing appointment, re-checking availability. */
    public void update(Appointment a) throws BusinessException {
        if (a.getId() <= 0) {
            throw new BusinessException("Μη έγκυρο ραντεβού προς ενημέρωση.");
        }
        prepareAndValidate(a, a.getId());
        try {
            appointmentDAO.update(a);
        } catch (SQLException e) {
            throw translate(e, "Αποτυχία ενημέρωσης ραντεβού.");
        }
    }

    public void cancel(int appointmentId, String reason) throws BusinessException {
        try {
            appointmentDAO.cancel(appointmentId, reason);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ακύρωσης ραντεβού.", e);
        }
    }

    // -----------------------------------------------------------------
    //  Shared validation + double-booking guard
    // -----------------------------------------------------------------
    private void prepareAndValidate(Appointment a, Integer excludeId)
            throws BusinessException {
        if (a == null) {
            throw new BusinessException("Δεν υπάρχουν στοιχεία ραντεβού.");
        }
        if (a.getCustomerId() <= 0 || a.getEmployeeId() <= 0 || a.getServiceId() <= 0) {
            throw new BusinessException(
                    "Το ραντεβού πρέπει να συνδέεται με πελάτη, εργαζόμενο και υπηρεσία.");
        }
        if (a.getStartTime() == null) {
            throw new BusinessException("Η ημερομηνία και ώρα είναι υποχρεωτικές.");
        }

        try {
            // derive end time from the service's duration
            Optional<Service> svc = serviceDAO.findById(a.getServiceId());
            if (svc.isEmpty()) {
                throw new BusinessException("Η επιλεγμένη υπηρεσία δεν υπάρχει.");
            }
            LocalDateTime end =
                    a.getStartTime().plusMinutes(svc.get().getDurationMinutes());
            a.setEndTime(end);

            // application-level double-booking check (clear message); the DB
            // EXCLUDE constraint is the final backstop against race conditions.
            if (appointmentDAO.hasConflict(
                    a.getEmployeeId(), a.getStartTime(), end, excludeId)) {
                throw new BusinessException(
                        "Ο εργαζόμενος έχει ήδη ραντεβού σε αυτό το χρονικό διάστημα.");
            }
        } catch (SQLException e) {
            throw new BusinessException("Σφάλμα ελέγχου διαθεσιμότητας.", e);
        }
    }

    /**
     * Maps the PostgreSQL EXCLUDE-constraint violation (SQLState 23P01) to a
     * friendly message, in case two users save at the exact same moment.
     */
    private BusinessException translate(SQLException e, String fallback) {
        if ("23P01".equals(e.getSQLState())) {
            return new BusinessException(
                    "Ο εργαζόμενος έχει ήδη ραντεβού σε αυτό το χρονικό διάστημα.", e);
        }
        return new BusinessException(fallback, e);
    }
}