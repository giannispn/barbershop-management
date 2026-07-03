package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code customers} table. Writes set last_modified_by so
 * the audit trail records who edited the record.
 */
public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Customer> search(String term) throws SQLException {
        String sql = "SELECT * FROM customers "
                   + "WHERE first_name ILIKE ? OR last_name ILIKE ? OR phone ILIKE ? "
                   + "ORDER BY last_name, first_name";
        List<Customer> list = new ArrayList<>();
        String like = "%" + term + "%";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Customer> findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
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

    public void insert(Customer c, Integer editorUserId) throws SQLException {
        String sql = "INSERT INTO customers "
                   + "(first_name, last_name, phone, email, preferences, "
                   + " late_arrival_count, no_show_count, reliability_notes, last_modified_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING customer_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEditable(ps, c);
            setNullableInt(ps, 9, editorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Customer c, Integer editorUserId) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, phone = ?, "
                   + "email = ?, preferences = ?, late_arrival_count = ?, no_show_count = ?, "
                   + "reliability_notes = ?, last_modified_by = ? "
                   + "WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEditable(ps, c);
            setNullableInt(ps, 9, editorUserId);
            ps.setInt(10, c.getId());
            ps.executeUpdate();
        }
    }

    /** Hard delete. The service layer guards against deleting active customers. */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bindEditable(PreparedStatement ps, Customer c) throws SQLException {
        ps.setString(1, c.getFirstName());
        ps.setString(2, c.getLastName());
        ps.setString(3, c.getPhone());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getPreferences());
        ps.setInt(6, c.getLateArrivalCount());
        ps.setInt(7, c.getNoShowCount());
        ps.setString(8, c.getReliabilityNotes());
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val != null) {
            ps.setInt(idx, val);
        } else {
            ps.setNull(idx, Types.INTEGER);
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("customer_id"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setPreferences(rs.getString("preferences"));
        c.setLateArrivalCount(rs.getInt("late_arrival_count"));
        c.setNoShowCount(rs.getInt("no_show_count"));
        c.setReliabilityNotes(rs.getString("reliability_notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        c.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
        Timestamp lm = rs.getTimestamp("last_modified_at");
        c.setLastModifiedAt(lm == null ? null : lm.toLocalDateTime());
        int mb = rs.getInt("last_modified_by");
        c.setLastModifiedBy(rs.wasNull() ? null : mb);
        return c;
    }
}
