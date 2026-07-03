package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.Payment;
import gr.uniwa.barbershop.model.enums.PaymentMethod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the {@code payments} table.
 */
public class PaymentDAO {

    public void insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments "
                   + "(customer_id, appointment_id, amount, method, registered_by) "
                   + "VALUES (?, ?, ?, ?::payment_method, ?) RETURNING payment_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCustomerId());
            if (p.getAppointmentId() != null) ps.setInt(2, p.getAppointmentId());
            else ps.setNull(2, Types.INTEGER);
            ps.setBigDecimal(3, p.getAmount());
            ps.setString(4, p.getMethod().name());
            if (p.getRegisteredBy() != null) ps.setInt(5, p.getRegisteredBy());
            else ps.setNull(5, Types.INTEGER);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Payment> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE customer_id = ? ORDER BY paid_at DESC";
        return query(sql, customerId);
    }

    public List<Payment> findByAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE appointment_id = ? ORDER BY paid_at DESC";
        return query(sql, appointmentId);
    }

    private List<Payment> query(String sql, int idParam) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("payment_id"));
        p.setCustomerId(rs.getInt("customer_id"));
        int ap = rs.getInt("appointment_id");
        p.setAppointmentId(rs.wasNull() ? null : ap);
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
        Timestamp paid = rs.getTimestamp("paid_at");
        p.setPaidAt(paid == null ? null : paid.toLocalDateTime());
        int rb = rs.getInt("registered_by");
        p.setRegisteredBy(rs.wasNull() ? null : rb);
        return p;
    }
}
