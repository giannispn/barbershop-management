package gr.uniwa.barbershop.model.enums;

/**
 * Lifecycle status of an appointment. Constant names map 1:1 to the PostgreSQL
 * {@code appointment_status} enum values. Only SCHEDULED/COMPLETED count as
 * "active" for the double-booking and customer-delete rules.
 */
public enum AppointmentStatus {
    SCHEDULED("Προγραμματισμένο"),
    COMPLETED("Ολοκληρωμένο"),
    CANCELLED("Ακυρωμένο"),
    NO_SHOW("Μη εμφάνιση");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** @return true if this status occupies the employee's time slot. */
    public boolean isActive() {
        return this == SCHEDULED || this == COMPLETED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
