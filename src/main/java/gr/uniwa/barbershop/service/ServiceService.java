package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.ServiceDAO;
import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.util.BusinessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for the barbershop's offered services. Services are not hard
 * deleted (appointments reference them); instead they are deactivated via the
 * {@code active} flag.
 */
public class ServiceService {

    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<Service> getAll() throws BusinessException {
        try {
            return serviceDAO.findAll();
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης υπηρεσιών.", e);
        }
    }

    public List<Service> getActive() throws BusinessException {
        try {
            return serviceDAO.findActive();
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης υπηρεσιών.", e);
        }
    }

    /** Inserts a new service or updates an existing one (id &gt; 0). */
    public void save(Service s) throws BusinessException {
        validate(s);
        try {
            if (s.getId() > 0) {
                serviceDAO.update(s);
            } else {
                serviceDAO.insert(s);
            }
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία αποθήκευσης υπηρεσίας.", e);
        }
    }

    private void validate(Service s) throws BusinessException {
        if (s == null) {
            throw new BusinessException("Δεν υπάρχουν στοιχεία υπηρεσίας.");
        }
        if (s.getName() == null || s.getName().isBlank()) {
            throw new BusinessException("Το όνομα της υπηρεσίας είναι υποχρεωτικό.");
        }
        if (s.getPrice() == null || s.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Η τιμή πρέπει να είναι θετικός αριθμός.");
        }
        if (s.getDurationMinutes() <= 0) {
            throw new BusinessException("Η διάρκεια (λεπτά) πρέπει να είναι θετικός αριθμός.");
        }
    }
}
