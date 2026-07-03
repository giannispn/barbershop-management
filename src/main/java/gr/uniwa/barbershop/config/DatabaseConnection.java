package gr.uniwa.barbershop.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton that owns the database configuration and hands out JDBC
 * connections to PostgreSQL.
 *
 * <p>Design note: this singleton does NOT hold one long-lived shared
 * {@link Connection}. A single shared connection is fragile in a JavaFX app
 * (background tasks, dropped sockets, no thread-safety on a Statement). Instead
 * it centralises the credentials/URL and returns a fresh connection per call.
 * Each DAO is expected to use try-with-resources so the connection is closed
 * promptly:</p>
 *
 * <pre>{@code
 * try (Connection conn = DatabaseConnection.getInstance().getConnection();
 *      PreparedStatement ps = conn.prepareStatement(sql)) {
 *     ...
 * }
 * }</pre>
 *
 * <p>If real connection pooling is needed later, swap the body of
 * {@link #getConnection()} for a HikariCP {@code DataSource.getConnection()}
 * without changing any caller.</p>
 */
public final class DatabaseConnection {

    /** Eagerly-created, thread-safe singleton instance. */
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private final String url;
    private final String user;
    private final String password;

    /** Private constructor: loads config from db.properties on the classpath. */
    private DatabaseConnection() {
        Properties props = new Properties();

        try (InputStream in =
                 getClass().getResourceAsStream("/db.properties")) {

            if (in == null) {
                throw new IllegalStateException(
                    "db.properties not found on the classpath "
                    + "(expected at src/main/resources/db.properties)");
            }
            props.load(in);

        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read db.properties", e);
        }

        this.url      = props.getProperty("db.url");
        this.user     = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        if (url == null || user == null) {
            throw new IllegalStateException(
                "db.url and db.user must be defined in db.properties");
        }

        // Ensure the PostgreSQL driver is registered. With modern JDBC the
        // driver auto-registers via SPI, but this makes the dependency explicit
        // and fails fast with a clear message if the jar is missing.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "PostgreSQL JDBC driver not found on the classpath", e);
        }
    }

    /** @return the single instance of this class. */
    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Opens and returns a new database connection. The caller is responsible
     * for closing it (use try-with-resources).
     *
     * @return a live {@link Connection} to the PostgreSQL database
     * @throws SQLException if the connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Convenience health-check used at startup to verify the database is
     * reachable before showing the login screen.
     *
     * @return {@code true} if a connection can be opened, {@code false} otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
