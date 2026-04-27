package hospital;

import hospital.daomodel.PatientRegistration;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientRegistrationDao {

    // Получить список активных учётов для конкретного врача (с именем пациента)
    public List<PatientRegistration> getActiveRegistrationsByDoctor(int doctorId) throws SQLException {
        List<PatientRegistration> list = new ArrayList<>();
        String sql = """
            SELECT r.*, p.FirstName || ' ' || p.LastName AS PatientName
            FROM PatientRegistrations r
            JOIN Patients p ON r.PatientID = p.PatientID
            WHERE r.DoctorID = ? AND r.IsActive = TRUE
            ORDER BY r.RegistrationDate DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Получить список активных учётов для конкретного пациента (с именем врача)
    public List<PatientRegistration> getActiveRegistrationsByPatient(int patientId) throws SQLException {
        List<PatientRegistration> list = new ArrayList<>();
        String sql = """
            SELECT r.*, d.FirstName || ' ' || d.LastName AS DoctorName
            FROM PatientRegistrations r
            JOIN Doctors d ON r.DoctorID = d.DoctorID
            WHERE r.PatientID = ? AND r.IsActive = TRUE
            ORDER BY r.RegistrationDate DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Проверить, состоит ли пациент на активном учёте у данного врача
    public boolean isPatientRegistered(int patientId, int doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PatientRegistrations WHERE PatientID = ? AND DoctorID = ? AND IsActive = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Добавить новую запись учёта
    public void addRegistration(PatientRegistration reg) throws SQLException {
        String sql = """
            INSERT INTO PatientRegistrations (PatientID, DoctorID, RegistrationDate, Diagnosis, IsActive, Notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reg.getPatientId());
            stmt.setInt(2, reg.getDoctorId());
            stmt.setDate(3, Date.valueOf(reg.getRegistrationDate() != null ? reg.getRegistrationDate() : LocalDate.now()));
            stmt.setString(4, reg.getDiagnosis());
            stmt.setBoolean(5, reg.isActive());
            stmt.setString(6, reg.getNotes());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    reg.setRegistrationId(keys.getInt(1));
                }
            }
        }
    }

    // Обновить запись (например, снять с учёта или изменить диагноз)
    public void updateRegistration(PatientRegistration reg) throws SQLException {
        String sql = """
            UPDATE PatientRegistrations
            SET Diagnosis = ?, IsActive = ?, Notes = ?, UpdatedAt = CURRENT_TIMESTAMP
            WHERE RegistrationID = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reg.getDiagnosis());
            stmt.setBoolean(2, reg.isActive());
            stmt.setString(3, reg.getNotes());
            stmt.setInt(4, reg.getRegistrationId());
            stmt.executeUpdate();
        }
    }

    // Снять с учёта
    public void deactivateRegistration(int registrationId) throws SQLException {
        String sql = "UPDATE PatientRegistrations SET IsActive = FALSE, UpdatedAt = CURRENT_TIMESTAMP WHERE RegistrationID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, registrationId);
            stmt.executeUpdate();
        }
    }

    // Удалить запись
    public void deleteRegistration(int registrationId) throws SQLException {
        String sql = "DELETE FROM PatientRegistrations WHERE RegistrationID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, registrationId);
            stmt.executeUpdate();
        }
    }

    private PatientRegistration mapRow(ResultSet rs) throws SQLException {
        PatientRegistration reg = new PatientRegistration();
        reg.setRegistrationId(rs.getInt("RegistrationID"));
        reg.setPatientId(rs.getInt("PatientID"));
        reg.setDoctorId(rs.getInt("DoctorID"));
        reg.setRegistrationDate(rs.getDate("RegistrationDate").toLocalDate());
        reg.setDiagnosis(rs.getString("Diagnosis"));
        reg.setActive(rs.getBoolean("IsActive"));
        reg.setNotes(rs.getString("Notes"));
        reg.setCreatedAt(rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null);
        reg.setUpdatedAt(rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null);
        // Если есть дополнительные поля
        try {
            reg.setPatientName(rs.getString("PatientName"));
        } catch (SQLException ignored) {}
        try {
            reg.setDoctorName(rs.getString("DoctorName"));
        } catch (SQLException ignored) {}
        return reg;
    }

    // Получить все активные учётные записи для администратора
    public List<PatientRegistration> getAllActiveRegistrations() throws SQLException {
        List<PatientRegistration> list = new ArrayList<>();
        String sql = """
        SELECT r.*, 
               p.FirstName || ' ' || p.LastName AS PatientName,
               d.FirstName || ' ' || d.LastName AS DoctorName
        FROM PatientRegistrations r
        JOIN Patients p ON r.PatientID = p.PatientID
        JOIN Doctors d ON r.DoctorID = d.DoctorID
        WHERE r.IsActive = TRUE
        ORDER BY r.RegistrationDate DESC
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
}