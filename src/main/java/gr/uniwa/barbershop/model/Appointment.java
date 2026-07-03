package gr.uniwa.barbershop.model;

import gr.uniwa.barbershop.model.enums.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * An appointment linking a {@link Customer}, an {@link Employee}, and a
 * {@link Service} to a time slot. Maps to the {@code appointments} table.
 *
 * <p>The FK ids are stored directly; the optional object references
 * ({@code customer}, {@code employee}, {@code service}) can be populated by a
 * DAO join so the UI can show names/prices without extra lookups.</p>
 */
public class Appointment {

    private int id;
    private int customerId;
    private int employeeId;
    private int serviceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    private String notes;
    private Integer createdBy;        // nullable FK to users.user_id
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Optional joined references (not persisted directly).
    private Customer customer;
    private Employee employee;
    private Service service;

    public Appointment() {
    }

    public Appointment(int customerId, int employeeId, int serviceId,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.serviceId = serviceId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    @Override
    public String toString() {
        return "Appointment#" + id + " @" + startTime + " [" + status + "]";
    }
}
