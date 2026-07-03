package gr.uniwa.barbershop.model;

import gr.uniwa.barbershop.model.enums.AppointmentStatus;
import gr.uniwa.barbershop.model.enums.PaymentMethod;
import gr.uniwa.barbershop.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for enums and simple model helpers. No database required.
 */
class ModelTest {

    @Test
    void appointmentStatus_activeFlag() {
        assertTrue(AppointmentStatus.SCHEDULED.isActive());
        assertTrue(AppointmentStatus.COMPLETED.isActive());
        assertFalse(AppointmentStatus.CANCELLED.isActive());
        assertFalse(AppointmentStatus.NO_SHOW.isActive());
    }

    @Test
    void enumNamesMatchDatabaseValues() {
        // the enum constant names must match the PostgreSQL enum values exactly
        assertEquals("ADMIN", UserRole.ADMIN.name());
        assertEquals("CASH", PaymentMethod.CASH.name());
        assertEquals("SCHEDULED", AppointmentStatus.SCHEDULED.name());
    }

    @Test
    void enumsHaveGreekDisplayNames() {
        assertEquals("Διαχειριστής", UserRole.ADMIN.getDisplayName());
        assertEquals("Μετρητά", PaymentMethod.CASH.getDisplayName());
    }

    @Test
    void customerFullName() {
        Customer c = new Customer("Μαρία", "Ιωάννου", "69", "a@b.gr");
        assertEquals("Μαρία Ιωάννου", c.getFullName());
    }

    @Test
    void userRoleConvenienceChecks() {
        User u = new User("admin", "hash", UserRole.ADMIN);
        assertTrue(u.isAdmin());
        assertFalse(u.isEmployee());
        assertFalse(u.isSecretary());
    }
}
