package com.clinic.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database configuration and connections.
 * Reads settings from {@code db.properties} on the classpath.
 * <p>
 * هذه أداة تهيئة فقط في النسخة الحالية. Production repositories do not currently
 * call this class, so schema/configuration exists without a wired persistence layer.
 */
public class DatabaseConfig {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load " + PROPERTIES_FILE + ": " + e.getMessage());
        }
    }

    private DatabaseConfig() {
        // Utility class — not instantiable
    }

    /**
     * Returns the configured JDBC URL.
     *
     * @return the JDBC URL
     */
    public static String getUrl() {
        return properties.getProperty("db.url", "jdbc:mysql://localhost:3306/clinic_db");
    }

    /**
     * Returns the configured database username.
     *
     * @return the username
     */
    public static String getUsername() {
        return properties.getProperty("db.username", "");
    }

    /**
     * Returns the configured database password.
     *
     * @return the password
     */
    public static String getPassword() {
        return properties.getProperty("db.password", "");
    }

    /**
     * Creates a new database connection using the configured properties.
     *
     * @return a new {@link Connection}
     * @throws SQLException if a connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
    }
}
