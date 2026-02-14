package hospital;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital-management";
    private static final String USER = "hospital_user";
    private static final String PASSWORD = "StrongPassword123";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Successfully connected to PostgreSQL database!");
            return connection;
        } catch (SQLException e) {
            System.err.println("Connection to PostgreSQL failed!");
            System.err.println("URL: " + URL);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }

    // Дополнительный метод для тестирования подключения
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Test connection failed: " + e.getMessage());
        }
    }
}