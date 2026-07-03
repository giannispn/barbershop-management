package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code employees} table.
 */
public class EmployeeDAO {

    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT * FROM employees ORDER BY last_name, first_name";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
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

    public void insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employees "
                   + "(first_name, last_name, phone, specialty, base_salary, commission_rate) "
                   + "VALUES (?, ?, ?, ?, ?, ?) RETURNING employee_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEditable(ps, e);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, phone = ?, "
                   + "specialty = ?, base_salary = ?, commission_rate = ?, is_active = ? "
                   + "WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEditable(ps, e);
            ps.setBoolean(7, e.isActive());
            ps.setInt(8, e.getId());
            ps.executeUpdate();
        }
    }

    /** Soft-delete by deactivating; preserves historical appointment links. */
    public void deactivate(int id) throws SQLException {
        String sql = "UPDATE employees SET is_active = FALSE WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bindEditable(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getFirstName());
        ps.setString(2, e.getLastName());
        ps.setString(3, e.getPhone());
        ps.setString(4, e.getSpecialty());
        ps.setBigDecimal(5, e.getBaseSalary());
        ps.setBigDecimal(6, e.getCommissionRate());
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("employee_id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setPhone(rs.getString("phone"));
        e.setSpecialty(rs.getString("specialty"));
        e.setBaseSalary(rs.getBigDecimal("base_salary"));
        e.setCommissionRate(rs.getBigDecimal("commission_rate"));
        e.setActive(rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        e.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        e.setUpdatedAt(ua == null ? null : ua.toLocalDateTime());
        return e;
    }
}
