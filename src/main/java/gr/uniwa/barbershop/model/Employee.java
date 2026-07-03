package gr.uniwa.barbershop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A staff member who performs services and earns base salary + commission.
 * Maps to the {@code employees} table.
 */
public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
    private BigDecimal baseSalary;       // monthly/period base, >= 0
    private BigDecimal commissionRate;   // percentage per service, 0..100
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Employee() {
    }

    public Employee(String firstName, String lastName, String phone,
                    String specialty, BigDecimal baseSalary,
                    BigDecimal commissionRate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.specialty = specialty;
        this.baseSalary = baseSalary;
        this.commissionRate = commissionRate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** Convenience accessor for UI tables. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
