package gr.uniwa.barbershop.model;

import java.math.BigDecimal;

/**
 * A service offered by the barbershop (e.g. haircut, beard trim).
 * Maps to the {@code services} table.
 */
public class Service {

    private int id;
    private String name;
    private String description;
    private BigDecimal price;         // > 0
    private int durationMinutes;      // > 0, used to compute appointment end time
    private boolean active = true;

    public Service() {
    }

    public Service(String name, String description, BigDecimal price, int durationMinutes) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;
    }
}
