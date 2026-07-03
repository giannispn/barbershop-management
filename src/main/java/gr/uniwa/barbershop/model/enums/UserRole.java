package gr.uniwa.barbershop.model.enums;

/**
 * User access roles. The constant names ({@code name()}) map 1:1 to the values
 * of the PostgreSQL {@code user_role} enum, so persistence is just
 * {@code UserRole.valueOf(rs.getString("role"))} and {@code role.name()}.
 * The {@code displayName} is the Greek label shown in the UI.
 */
public enum UserRole {
    ADMIN("Διαχειριστής"),
    EMPLOYEE("Υπάλληλος"),
    SECRETARY("Γραμματέας");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
