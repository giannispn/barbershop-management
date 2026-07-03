package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.PaymentDAO;
import gr.uniwa.barbershop.model.Payment;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for payments. A payment must link to a customer (and may also
 * link to a specific appointment), and the amount must be a positive number.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    public void register(Payment p) throws BusinessException {
        validate(p);
        try {
            p.setRegisteredBy(SessionManager.currentUserId());
            paymentDAO.insert(p);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία καταχώρησης πληρωμής.", e);
        }
    }

    public List<Payment> getByCustomer(int customerId) throws BusinessException {
        try {
            return paymentDAO.findByCustomer(customerId);
        } catch (SQLException e) {
            throw new BusinessException("Αποτυχία ανάκτησης πληρωμών.", e);
        }
    }

    private void validate(Payment p) throws BusinessException {
        if (p == null) {
            throw new BusinessException("Δεν υπάρχουν στοιχεία πληρωμής.");
        }
        if (p.getCustomerId() <= 0) {
            throw new BusinessException(
                "Η πληρωμή πρέπει να συνδέεται με συγκεκριμένο πελάτη ή ραντεβού.");
        }
        if (p.getMethod() == null) {
            throw new BusinessException("Επιλέξτε τρόπο πληρωμής.");
        }
        if (p.getAmount() == null || p.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Το ποσό πληρωμής πρέπει να είναι θετικό.");
        }
    }
}
