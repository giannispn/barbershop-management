package gr.uniwa.barbershop.model;

import java.time.LocalDateTime;

/**
 * A customer of the barbershop. Maps to the {@code customers} table.
 * Includes reliability tracking (late arrivals / no-shows) and audit fields
 * recording who last edited the record and when.
 */
public class Customer {

    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String preferences;
    private int lateArrivalCount;
    private int noShowCount;
    private String reliabilityNotes;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private Integer lastModifiedBy;     // nullable FK to users.user_id

    public Customer() {
    }

    public Customer(String firstName, String lastName, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public int getLateArrivalCount() { return lateArrivalCount; }
    public void setLateArrivalCount(int lateArrivalCount) { this.lateArrivalCount = lateArrivalCount; }

    public int getNoShowCount() { return noShowCount; }
    public void setNoShowCount(int noShowCount) { this.noShowCount = noShowCount; }

    public String getReliabilityNotes() { return reliabilityNotes; }
    public void setReliabilityNotes(String reliabilityNotes) { this.reliabilityNotes = reliabilityNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

    public Integer getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(Integer lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
