package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.model.enums.UserRole;

import java.sql.*;
import java.util.Optional;

/**
 * Data access for the {@code users} table.
 */
public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = now() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, employee_id) "
                   + "VALUES (?, ?, ?::user_role, ?) RETURNING user_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            if (user.getEmployeeId() != null) {
                ps.setInt(4, user.getEmployeeId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        int empId = rs.getInt("employee_id");
        u.setEmployeeId(rs.wasNull() ? null : empId);
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ll = rs.getTimestamp("last_login");
        u.setLastLogin(ll == null ? null : ll.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        u.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        return u;
    }
}
