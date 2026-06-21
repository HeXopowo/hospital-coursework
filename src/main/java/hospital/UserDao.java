package hospital;
import hospital.daomodel.User;
import java.sql.*;

public class UserDao {
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT u.UserID, u.Username, u.Password, u.Role, " +
                "COALESCE(u.DoctorID, u.PatientID, u.AdminID) as RoleID " +
                "FROM Users u " +
                "WHERE u.Username = ? AND u.Password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setPassword(rs.getString("Password"));
                    user.setRole(rs.getString("Role"));
                    user.setRoleId(rs.getInt("RoleID"));
                    return user;
                }
            }
        }
        return null;
    }

    // Дополнительные методы для работы с пользователями

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT u.UserID, u.Username, u.Password, u.Role, " +
                "COALESCE(u.DoctorID, u.PatientID, u.AdminID) as RoleID " +
                "FROM Users u " +
                "WHERE u.UserID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setPassword(rs.getString("Password"));
                    user.setRole(rs.getString("Role"));
                    user.setRoleId(rs.getInt("RoleID"));
                    return user;
                }
            }
        }
        return null;
    }

    public String getFullNameByRole(User user) throws SQLException {
        String sql = "";
        int roleId = user.getRoleId();

        switch (user.getRole()) {
            case "DOCTOR":
                sql = "SELECT FirstName, LastName FROM Doctors WHERE DoctorID = ?";
                break;
            case "PATIENT":
                sql = "SELECT FirstName, LastName FROM Patients WHERE PatientID = ?";
                break;
            case "ADMIN":
                sql = "SELECT FirstName, LastName FROM Admins WHERE AdminID = ?";
                break;
            default:
                return "Unknown Role";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FirstName") + " " + rs.getString("LastName");
                }
            }
        }
        return "Name Not Found";
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}