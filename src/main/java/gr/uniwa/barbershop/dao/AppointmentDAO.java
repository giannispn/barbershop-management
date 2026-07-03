package gr.uniwa.barbershop.dao;

import gr.uniwa.barbershop.config.DatabaseConnection;
import gr.uniwa.barbershop.model.Appointment;
import gr.uniwa.barbershop.model.Customer;
import gr.uniwa.barbershop.model.Employee;
import gr.uniwa.barbershop.model.Service;
import gr.uniwa.barbershop.model.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code appointments} table. Several queries join the
 * customer/employee/service so the UI can render names and prices in one pass.
 */
public class AppointmentDAO {

    // Joined SELECT used by list/detail queries.
    private static final String SELECT_JOINED =
            "SELECT a.*, "
                    + "  c.first_name AS c_first, c.last_name AS c_last, "
                    + "  e.first_name AS e_first, e.last_name AS e_last, "
                    + "  s.name AS s_name, s.price AS s_price "
                    + "FROM appointments a "
                    + "JOIN customers c ON c.customer_id = a.customer_id "
                    + "JOIN employees e ON e.employee_id = a.employee_id "
                    + "JOIN services  s ON s.service_id  = a.service_id ";

    /**
     * Returns true if the employee already has an overlapping, non-cancelled
     * appointment in the given window. When updating an existing appointment,
     * pass its id as {@code excludeId} so it doesn't conflict with itself.
     */
    public boolean hasConflict(int employeeId, LocalDateTime start,
                               LocalDateTime end, Integer excludeId) throws SQLException {
        String sql = "SELECT 1 FROM appointments "
                + "WHERE employee_id = ? "
                + "  AND status NOT IN ('CANCELLED','NO_SHOW') "
                + "  AND tsrange(start_time, end_time) && tsrange(?, ?) "
                + "  AND (?::int IS NULL OR appointment_id <> ?) "
                + "LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            if (excludeId != null) {
                ps.setInt(4, excludeId);
                ps.setInt(5, excludeId);
            } else {
                ps.setNull(4, Types.INTEGER);
                ps.setInt(5, 0); // unused when excludeId IS NULL
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Appointment> findByDate(LocalDate date) throws SQLException {
        String sql = SELECT_JOINED
                + "WHERE a.start_time::date = ? ORDER BY a.start_time";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoined(rs));
                }
            }
        }
        return list;
    }

    public Optional<Appointment> findById(int id) throws SQLException {
        String sql = SELECT_JOINED + "WHERE a.appointment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapJoined(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** All appointments for a customer (most recent first). */
    public List<Appointment> findByCustomer(int customerId) throws SQLException {
        String sql = SELECT_JOINED
                + "WHERE a.customer_id = ? ORDER BY a.start_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoined(rs));
                }
            }
        }
        return list;
    }

    /** Count of still-active (SCHEDULED) appointments for a customer. */
    public int countActiveForCustomer(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments "
                + "WHERE customer_id = ? AND status = 'SCHEDULED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Sum of service prices for an employee's COMPLETED appointments in a date
     * range. Used by PayrollService to compute commission.
     */
    public BigDecimal completedServiceTotal(int employeeId,
                                            LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(s.price), 0) "
                + "FROM appointments a JOIN services s ON s.service_id = a.service_id "
                + "WHERE a.employee_id = ? AND a.status = 'COMPLETED' "
                + "  AND a.start_time::date BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public void insert(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments "
                + "(customer_id, employee_id, service_id, start_time, end_time, "
                + " status, notes, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?::appointment_status, ?, ?) "
                + "RETURNING appointment_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCustomerId());
            ps.setInt(2, a.getEmployeeId());
            ps.setInt(3, a.getServiceId());
            ps.setTimestamp(4, Timestamp.valueOf(a.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(a.getEndTime()));
            ps.setString(6, a.getStatus().name());
            ps.setString(7, a.getNotes());
            if (a.getCreatedBy() != null) ps.setInt(8, a.getCreatedBy());
            else ps.setNull(8, Types.INTEGER);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Appointment a) throws SQLException {
        String sql = "UPDATE appointments SET customer_id = ?, employee_id = ?, "
                + "service_id = ?, start_time = ?, end_time = ?, "
                + "status = ?::appointment_status, notes = ? "
                + "WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCustomerId());
            ps.setInt(2, a.getEmployeeId());
            ps.setInt(3, a.getServiceId());
            ps.setTimestamp(4, Timestamp.valueOf(a.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(a.getEndTime()));
            ps.setString(6, a.getStatus().name());
            ps.setString(7, a.getNotes());
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        }
    }

    public void cancel(int id, String reason) throws SQLException {
        String sql = "UPDATE appointments "
                + "SET status = 'CANCELLED', cancelled_at = now(), cancellation_reason = ? "
                + "WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Appointment mapJoined(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("appointment_id"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setServiceId(rs.getInt("service_id"));
        a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setNotes(rs.getString("notes"));
        int cb = rs.getInt("created_by");
        a.setCreatedBy(rs.wasNull() ? null : cb);

        // lightweight joined references for display
        Customer c = new Customer();
        c.setId(a.getCustomerId());
        c.setFirstName(rs.getString("c_first"));
        c.setLastName(rs.getString("c_last"));
        a.setCustomer(c);

        Employee e = new Employee();
        e.setId(a.getEmployeeId());
        e.setFirstName(rs.getString("e_first"));
        e.setLastName(rs.getString("e_last"));
        a.setEmployee(e);

        Service s = new Service();
        s.setId(a.getServiceId());
        s.setName(rs.getString("s_name"));
        s.setPrice(rs.getBigDecimal("s_price"));
        a.setService(s);

        return a;
    }
}