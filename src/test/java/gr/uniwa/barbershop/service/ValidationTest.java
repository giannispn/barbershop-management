package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.Payment;
import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.model.enums.PaymentMethod;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for validation rules in the service layer. These exercise the
 * checks that run BEFORE any database access, so no database is required:
 * an invalid object causes a {@link BusinessException} before a DAO is ever
 * called.
 */
class ValidationTest {

    @AfterEach
    void cleanup() {
        SessionManager.logout();   // reset static session between tests
    }

    // ---------- CustomerService ----------

    @Test
    void customerRequiresFirstAndLastName() {
        CustomerService service = new CustomerService();
        Customer c = new Customer();         // empty
        c.setPhone("6900000000");
        assertThrows(BusinessException.class, () -> service.add(c));
    }

    @Test
    void customerRequiresPhone() {
        CustomerService service = new CustomerService();
        Customer c = new Customer("Γιώργος", "Παπαδόπουλος", null, null);
        assertThrows(BusinessException.class, () -> service.add(c));
    }

    @Test
    void customerDeleteRequiresAdmin() {
        // nobody logged in -> not an admin -> must be blocked before touching DB
        SessionManager.logout();
        CustomerService service = new CustomerService();
        assertThrows(BusinessException.class, () -> service.delete(1));
    }

    // ---------- ServiceService ----------

    @Test
    void serviceRequiresName() {
        ServiceService service = new ServiceService();
        Service s = new Service(null, "desc", new BigDecimal("10"), 30);
        assertThrows(BusinessException.class, () -> service.save(s));
    }

    @Test
    void serviceRequiresPositivePrice() {
        ServiceService service = new ServiceService();
        Service s = new Service("Κούρεμα", "desc", BigDecimal.ZERO, 30);
        assertThrows(BusinessException.class, () -> service.save(s));
    }

    @Test
    void serviceRequiresPositiveDuration() {
        ServiceService service = new ServiceService();
        Service s = new Service("Κούρεμα", "desc", new BigDecimal("10"), 0);
        assertThrows(BusinessException.class, () -> service.save(s));
    }

    // ---------- PaymentService ----------

    @Test
    void paymentRequiresPositiveAmount() {
        PaymentService service = new PaymentService();
        Payment p = new Payment(1, null, BigDecimal.ZERO, PaymentMethod.CASH);
        assertThrows(BusinessException.class, () -> service.register(p));
    }

    @Test
    void paymentRequiresMethod() {
        PaymentService service = new PaymentService();
        Payment p = new Payment(1, null, new BigDecimal("10"), null);
        assertThrows(BusinessException.class, () -> service.register(p));
    }

    @Test
    void paymentRequiresCustomer() {
        PaymentService service = new PaymentService();
        Payment p = new Payment(0, null, new BigDecimal("10"), PaymentMethod.CARD);
        assertThrows(BusinessException.class, () -> service.register(p));
    }

    // ---------- EmployeeService access control ----------

    @Test
    void employeeManagementBlockedForNonAdmin() {
        SessionManager.logout();             // not logged in -> not admin
        EmployeeService service = new EmployeeService();
        assertThrows(BusinessException.class, service::getAll);
    }
}
