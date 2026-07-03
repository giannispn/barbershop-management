package gr.uniwa.barbershop.model;

import gr.uniwa.barbershop.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A payment, always linked to a {@link Customer} and optionally to a specific
 * {@link Appointment}. Maps to the {@code payments} table.
 */
public class Payment {

    private int id;
    private int customerId;
    private Integer appointmentId;    // nullable
    private BigDecimal amount;        // > 0
    private PaymentMethod method;
    private LocalDateTime paidAt;
    private Integer registeredBy;     // nullable FK to users.user_id

    public Payment() {
    }

    public Payment(int customerId, Integer appointmentId,
                   BigDecimal amount, PaymentMethod method) {
        this.customerId = customerId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.method = method;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public Integer getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(Integer registeredBy) { this.registeredBy = registeredBy; }

    @Override
    public String toString() {
        return "Payment#" + id + " " + amount + " (" + method + ")";
    }
}
