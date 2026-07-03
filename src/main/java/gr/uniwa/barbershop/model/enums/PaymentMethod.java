package gr.uniwa.barbershop.model.enums;

/**
 * Accepted payment methods. Constant names map 1:1 to the PostgreSQL
 * {@code payment_method} enum values.
 */
public enum PaymentMethod {
    CASH("Μετρητά"),
    CARD("Κάρτα");

    private final String displayName;

    PaymentMethod(String displayName) {
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
