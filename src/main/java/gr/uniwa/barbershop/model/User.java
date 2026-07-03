package gr.uniwa.barbershop.model;

import gr.uniwa.barbershop.model.enums.UserRole;

import java.time.LocalDateTime;

/**
 * A system user with login credentials and a role. Maps to the {@code users}
 * table. {@code employeeId} is nullable: a barber-user links to their
 * {@link Employee} record, while an admin/secretary may have none.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;     // BCrypt hash, never plaintext
    private UserRole role;
    private Integer employeeId;       // nullable
    private boolean active = true;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String passwordHash, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ---- role convenience checks (used by the service layer for access control) ----
    public boolean isAdmin()     { return role == UserRole.ADMIN; }
    public boolean isEmployee()  { return role == UserRole.EMPLOYEE; }
    public boolean isSecretary() { return role == UserRole.SECRETARY; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
