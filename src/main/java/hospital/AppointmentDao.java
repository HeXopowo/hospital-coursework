package hospital;

import hospital.daomodel.Appointment;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {

    private final PatientRegistrationDao registrationDao = new PatientRegistrationDao();

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

    // Обновление приёма
    public void updateAppointment(Appointment appointment) throws SQLException {
        // Получаем ID врача и пациента по их именам
        int doctorId = getDoctorIdByName(appointment.getDoctorName());
        int patientId = getPatientIdByName(appointment.getPatientName());

        String sql = """
        UPDATE Appointments
        SET DoctorID = ?, 
            PatientID = ?, 
            AppointmentDateTime = ?, 
            Status = ?, 
            MedicalRecordNote = ?
        WHERE AppointmentID = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getDateTime()));
            stmt.setString(4, appointment.getStatus());
            stmt.setString(5, appointment.getNote());
            stmt.setInt(6, appointment.getAppointmentId());

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

    // Удаление приёма
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

    // Поиск приёмов по поисковому запросу
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

    // Проверяет, есть ли у данной пары врач-пациент другие приёмы в заданном диапазоне дней
    // Если пациент состоит на учёте у врача, ограничение не применяется
    public boolean hasAppointmentWithinDays(String doctorName, String patientName, LocalDate date, int days, Integer excludeAppointmentId) throws SQLException {
        // Получаем ID врача и пациента
        int doctorId = getDoctorIdByName(doctorName);
        int patientId = getPatientIdByName(patientName);

        // Проверяем, состоит ли пациент на активном учёте у этого врача
        if (registrationDao.isPatientRegistered(patientId, doctorId)) {
            return false; // учёт есть — ограничение не применяется
        }

        // Иначе проверяем наличие записей в указанном диапазоне
        LocalDate startDate = date.minusDays(days);
        LocalDate endDate = date.plusDays(days);

        String sql;
        if (excludeAppointmentId != null) {
            sql = """
                SELECT COUNT(*) FROM Appointments
                WHERE DoctorID = ? AND PatientID = ?
                  AND DATE(AppointmentDateTime) BETWEEN ? AND ?
                  AND AppointmentID != ?
            """;
        } else {
            sql = """
                SELECT COUNT(*) FROM Appointments
                WHERE DoctorID = ? AND PatientID = ?
                  AND DATE(AppointmentDateTime) BETWEEN ? AND ?
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            stmt.setInt(2, patientId);
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

    // Вспомогательный метод для получения ID врача по полному имени
    public int getDoctorIdByName(String fullName) throws SQLException {
        String sql = "SELECT DoctorID FROM Doctors WHERE FirstName || ' ' || LastName = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DoctorID");
                } else {
                    throw new SQLException("Врач с именем '" + fullName + "' не найден");
                }
            }
        }
    }

    // Вспомогательный метод для получения ID пациента по полному имени
    public int getPatientIdByName(String fullName) throws SQLException {
        String sql = "SELECT PatientID FROM Patients WHERE FirstName || ' ' || LastName = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("PatientID");
                } else {
                    throw new SQLException("Пациент с именем '" + fullName + "' не найден");
                }
            }
        }
    }

    // Проверяет, есть ли у пациента приём в указанное время (с возможностью исключить приём по ID)
    public boolean hasPatientAppointmentAtTime(int patientId, LocalDateTime dateTime, Integer excludeAppointmentId) throws SQLException {
        String sql;
        if (excludeAppointmentId != null) {
            sql = "SELECT COUNT(*) FROM Appointments WHERE PatientID = ? AND AppointmentDateTime = ? AND AppointmentID != ?";
        } else {
            sql = "SELECT COUNT(*) FROM Appointments WHERE PatientID = ? AND AppointmentDateTime = ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setTimestamp(2, Timestamp.valueOf(dateTime));
            if (excludeAppointmentId != null) {
                stmt.setInt(3, excludeAppointmentId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Проверяет, есть ли у врача приём в указанное время (с возможностью исключить приём по ID)
    public boolean hasDoctorAppointmentAtTime(int doctorId, LocalDateTime dateTime, Integer excludeAppointmentId) throws SQLException {
        String sql;
        if (excludeAppointmentId != null) {
            sql = "SELECT COUNT(*) FROM Appointments WHERE DoctorID = ? AND AppointmentDateTime = ? AND AppointmentID != ?";
        } else {
            sql = "SELECT COUNT(*) FROM Appointments WHERE DoctorID = ? AND AppointmentDateTime = ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setTimestamp(2, Timestamp.valueOf(dateTime));
            if (excludeAppointmentId != null) {
                stmt.setInt(3, excludeAppointmentId);
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