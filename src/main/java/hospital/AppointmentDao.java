package hospital;
import hospital.daomodel.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {
    public List<Appointment> getAppointmentsByRole(String role, int roleId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql;
        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
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
                    appointment.setDateTime(rs.getTimestamp("AppointmentDateTime").toLocalDateTime());
                    appointment.setDoctorName(rs.getString("DoctorName"));
                    appointment.setPatientName(rs.getString("PatientName"));
                    appointment.setStatus(rs.getString("Status"));
                    appointment.setNote(rs.getString("MedicalRecordNote"));
                    appointment.setRoomNumber(rs.getString("RoomNumber")); // Устанавливаем номер кабинета
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

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

    public List<Appointment> searchAppointments(String searchTerm, String role, int roleId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql;

        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
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
}