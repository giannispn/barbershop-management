package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code services} table.
 */
public class ServiceDAO {

    public List<Service> findAll() throws SQLException {
        return query("SELECT * FROM services ORDER BY name");
    }

    public List<Service> findActive() throws SQLException {
        return query("SELECT * FROM services WHERE is_active = TRUE ORDER BY name");
    }

    public Optional<Service> findById(int id) throws SQLException {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void insert(Service s) throws SQLException {
        String sql = "INSERT INTO services (name, description, price, duration_minutes) "
                   + "VALUES (?, ?, ?, ?) RETURNING service_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setBigDecimal(3, s.getPrice());
            ps.setInt(4, s.getDurationMinutes());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Service s) throws SQLException {
        String sql = "UPDATE services SET name = ?, description = ?, price = ?, "
                   + "duration_minutes = ?, is_active = ? WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setBigDecimal(3, s.getPrice());
            ps.setInt(4, s.getDurationMinutes());
            ps.setBoolean(5, s.isActive());
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        }
    }

    private List<Service> query(String sql) throws SQLException {
        List<Service> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private Service map(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("service_id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setPrice(rs.getBigDecimal("price"));
        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}
