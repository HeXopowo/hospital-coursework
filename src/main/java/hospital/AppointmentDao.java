package hospital;

import hospital.daomodel.Appointment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {

    // Получение списка приёмов по роли пользователя
    public List<Appointment> getAppointmentsByRole(String role, int roleId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql;
        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE a.PatientID = ?
            """;
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE a.DoctorID = ?
            """;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
            """;
        } else {
            throw new IllegalArgumentException("Неподдерживаемая роль: " + role);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!"ADMIN".equalsIgnoreCase(role)) {
                stmt.setInt(1, roleId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("AppointmentID"));
                    appointment.setDateTime(rs.getTimestamp("AppointmentDateTime").toLocalDateTime());
                    appointment.setDoctorName(rs.getString("DoctorName"));
                    appointment.setPatientName(rs.getString("PatientName"));
                    appointment.setStatus(rs.getString("Status"));
                    appointment.setNote(rs.getString("MedicalRecordNote"));
                    appointment.setRoomNumber(rs.getString("RoomNumber"));
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    // Обновление приёма (по старым данным – дата, врач, пациент)
    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = """
            UPDATE Appointments
            SET Status = ?, MedicalRecordNote = ?
            WHERE AppointmentDateTime = ? 
              AND DoctorID = (SELECT DoctorID FROM Doctors WHERE FirstName || ' ' || LastName = ?) 
              AND PatientID = (SELECT PatientID FROM Patients WHERE FirstName || ' ' || LastName = ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointment.getStatus());
            stmt.setString(2, appointment.getNote());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getDateTime()));
            stmt.setString(4, appointment.getDoctorName());
            stmt.setString(5, appointment.getPatientName());
            stmt.executeUpdate();
        }
    }

    // Добавление нового приёма
    public void addAppointment(String doctorName, String patientName, String dateTime, String status, String note) throws SQLException {
        String sql = """
            INSERT INTO Appointments (DoctorID, PatientID, AppointmentDateTime, Status, MedicalRecordNote)
            VALUES (
                (SELECT DoctorID FROM Doctors WHERE FirstName || ' ' || LastName = ?),
                (SELECT PatientID FROM Patients WHERE FirstName || ' ' || LastName = ?),
                ?, ?, ?
            )
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorName);
            stmt.setString(2, patientName);
            stmt.setTimestamp(3, Timestamp.valueOf(dateTime));
            stmt.setString(4, status);
            stmt.setString(5, note);
            stmt.executeUpdate();
        }
    }

    // Удаление приёма (по старым данным)
    public void deleteAppointment(Appointment appointment) throws SQLException {
        String sql = """
            DELETE FROM Appointments
            WHERE AppointmentDateTime = ?
              AND DoctorID = (SELECT DoctorID FROM Doctors WHERE FirstName || ' ' || LastName = ?)
              AND PatientID = (SELECT PatientID FROM Patients WHERE FirstName || ' ' || LastName = ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(appointment.getDateTime()));
            stmt.setString(2, appointment.getDoctorName());
            stmt.setString(3, appointment.getPatientName());
            stmt.executeUpdate();
        }
    }

    // Поиск приёмов по поисковому запросу (с учётом роли)
    public List<Appointment> searchAppointments(String searchTerm, String role, int roleId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql;

        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE a.PatientID = ?
                  AND (LOWER(d.FirstName) LIKE LOWER(?) 
                   OR LOWER(d.LastName) LIKE LOWER(?)
                   OR LOWER(d.FirstName || ' ' || d.LastName) LIKE LOWER(?))
                ORDER BY a.AppointmentDateTime DESC
            """;
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE a.DoctorID = ?
                  AND (LOWER(p.FirstName) LIKE LOWER(?) 
                   OR LOWER(p.LastName) LIKE LOWER(?)
                   OR LOWER(p.FirstName || ' ' || p.LastName) LIKE LOWER(?))
                ORDER BY a.AppointmentDateTime DESC
            """;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    a.AppointmentID,
                    a.AppointmentDateTime,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    p.FirstName || ' ' || p.LastName AS PatientName,
                    a.Status,
                    a.MedicalRecordNote,
                    d.RoomNumber
                FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE LOWER(d.FirstName) LIKE LOWER(?) 
                   OR LOWER(d.LastName) LIKE LOWER(?)
                   OR LOWER(d.FirstName || ' ' || d.LastName) LIKE LOWER(?)
                   OR LOWER(p.FirstName) LIKE LOWER(?)
                   OR LOWER(p.LastName) LIKE LOWER(?)
                   OR LOWER(p.FirstName || ' ' || p.LastName) LIKE LOWER(?)
                ORDER BY a.AppointmentDateTime DESC
            """;
        } else {
            throw new IllegalArgumentException("Неподдерживаемая роль: " + role);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";

            if ("PATIENT".equalsIgnoreCase(role)) {
                stmt.setInt(1, roleId);
                stmt.setString(2, likeTerm);
                stmt.setString(3, likeTerm);
                stmt.setString(4, likeTerm);
            } else if ("DOCTOR".equalsIgnoreCase(role)) {
                stmt.setInt(1, roleId);
                stmt.setString(2, likeTerm);
                stmt.setString(3, likeTerm);
                stmt.setString(4, likeTerm);
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                for (int i = 1; i <= 6; i++) {
                    stmt.setString(i, likeTerm);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("AppointmentID"));
                    appointment.setDateTime(rs.getTimestamp("AppointmentDateTime").toLocalDateTime());
                    appointment.setDoctorName(rs.getString("DoctorName"));
                    appointment.setPatientName(rs.getString("PatientName"));
                    appointment.setStatus(rs.getString("Status"));
                    appointment.setNote(rs.getString("MedicalRecordNote"));
                    appointment.setRoomNumber(rs.getString("RoomNumber"));
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    // Получение списка всех врачей (ФИО)
    public List<String> getAllDoctors() throws SQLException {
        List<String> doctors = new ArrayList<>();
        String sql = "SELECT FirstName || ' ' || LastName AS FullName FROM Doctors";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                doctors.add(rs.getString("FullName"));
            }
        }
        return doctors;
    }

    // Получение списка всех пациентов (ФИО)
    public List<String> getAllPatients() throws SQLException {
        List<String> patients = new ArrayList<>();
        String sql = "SELECT FirstName || ' ' || LastName AS FullName FROM Patients";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(rs.getString("FullName"));
            }
        }
        return patients;
    }

    public boolean hasAppointmentWithinDays(String doctorName, String patientName, LocalDate date, int days, Integer excludeAppointmentId) throws SQLException {
        LocalDate startDate = date.minusDays(days);
        LocalDate endDate = date.plusDays(days);
        String sql;
        if (excludeAppointmentId != null) {
            sql = """
                SELECT COUNT(*) FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE d.FirstName || ' ' || d.LastName = ?
                  AND p.FirstName || ' ' || p.LastName = ?
                  AND DATE(a.AppointmentDateTime) BETWEEN ? AND ?
                  AND a.AppointmentID != ?
            """;
        } else {
            sql = """
                SELECT COUNT(*) FROM Appointments a
                JOIN Doctors d ON a.DoctorID = d.DoctorID
                JOIN Patients p ON a.PatientID = p.PatientID
                WHERE d.FirstName || ' ' || d.LastName = ?
                  AND p.FirstName || ' ' || p.LastName = ?
                  AND DATE(a.AppointmentDateTime) BETWEEN ? AND ?
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, doctorName);
            stmt.setString(2, patientName);
            stmt.setDate(3, Date.valueOf(startDate));
            stmt.setDate(4, Date.valueOf(endDate));
            if (excludeAppointmentId != null) {
                stmt.setInt(5, excludeAppointmentId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}