package hospital;

import hospital.daomodel.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDao {
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM Doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int doctorId = rs.getInt("DoctorID");
                if (rs.wasNull() || doctorId <= 0) {
                    throw new IllegalArgumentException("Некорректный DoctorID в базе: " + doctorId);
                }
                Doctor doctor = new Doctor();
                doctor.setDoctorId(doctorId);
                doctor.setFirstName(rs.getString("FirstName"));
                doctor.setLastName(rs.getString("LastName"));
                doctor.setSpecialization(rs.getString("Specialization"));
                doctor.setRoomNumber(rs.getString("RoomNumber"));
                doctor.setSchedule(rs.getString("Schedule"));
                doctor.setEmail(rs.getString("Email"));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public Doctor getDoctorById(int doctorId) throws SQLException {
        Doctor doctor = null;
        String sql = "SELECT * FROM Doctors WHERE DoctorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("DoctorID");
                    if (rs.wasNull() || id <= 0) {
                        throw new IllegalArgumentException("Некорректный DoctorID в базе: " + id);
                    }
                    doctor = new Doctor();
                    doctor.setDoctorId(id);
                    doctor.setFirstName(rs.getString("FirstName"));
                    doctor.setLastName(rs.getString("LastName"));
                    doctor.setSpecialization(rs.getString("Specialization"));
                    doctor.setRoomNumber(rs.getString("RoomNumber"));
                    doctor.setSchedule(rs.getString("Schedule"));
                    doctor.setEmail(rs.getString("Email"));
                }
            }
        }
        return doctor;
    }

    public void addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO Doctors (FirstName, LastName, Specialization, RoomNumber, Schedule, Email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, doctor.getRoomNumber());
            stmt.setString(5, doctor.getSchedule());
            stmt.setString(6, doctor.getEmail());
            stmt.executeUpdate();
        }
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE Doctors SET FirstName = ?, LastName = ?, Specialization = ?, RoomNumber = ?, Schedule = ?, Email = ? WHERE DoctorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, doctor.getRoomNumber());
            stmt.setString(5, doctor.getSchedule());
            stmt.setString(6, doctor.getEmail());
            stmt.setInt(7, doctor.getDoctorId());
            stmt.executeUpdate();
        }
    }

    public void deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM Doctors WHERE DoctorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        }
    }

    public List<Doctor> searchDoctors(String searchTerm) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = """
        SELECT * FROM Doctors 
        WHERE LOWER(FirstName) LIKE LOWER(?) 
           OR LOWER(LastName) LIKE LOWER(?)
           OR LOWER(FirstName || ' ' || LastName) LIKE LOWER(?)
           OR LOWER(LastName || ' ' || FirstName) LIKE LOWER(?)
           OR LOWER(Specialization) LIKE LOWER(?)      
        ORDER BY LastName, FirstName
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);
            stmt.setString(5, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Doctor doctor = new Doctor();
                    doctor.setDoctorId(rs.getInt("DoctorID"));
                    doctor.setFirstName(rs.getString("FirstName"));
                    doctor.setLastName(rs.getString("LastName"));
                    doctor.setSpecialization(rs.getString("Specialization"));
                    doctor.setRoomNumber(rs.getString("RoomNumber"));
                    doctor.setSchedule(rs.getString("Schedule"));
                    doctor.setEmail(rs.getString("Email"));
                    doctors.add(doctor);
                }
            }
        }
        return doctors;
    }

    public void archiveDoctor(int doctorId, String archivedBy) throws SQLException {
        // 1. Получаем данные врача
        Doctor doctor = getDoctorById(doctorId);
        if (doctor == null) return;

        // 2. Копируем в архивную таблицу
        String insertArchivedSql = """
        INSERT INTO ArchivedDoctors (DoctorID, FirstName, LastName, Specialization, RoomNumber, Schedule, Email, ArchivedBy)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertArchivedSql)) {
            stmt.setInt(1, doctor.getDoctorId());
            stmt.setString(2, doctor.getFirstName());
            stmt.setString(3, doctor.getLastName());
            stmt.setString(4, doctor.getSpecialization());
            stmt.setString(5, doctor.getRoomNumber());
            stmt.setString(6, doctor.getSchedule());
            stmt.setString(7, doctor.getEmail());
            stmt.setString(8, archivedBy);
            stmt.executeUpdate();
        }

        // 3. Удаляем запись из Users (если есть)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Users WHERE DoctorID = ?")) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        }

        // 4. Удаляем врача – связанные записи удалятся каскадно, триггеры их заархивируют
        String deleteSql = "DELETE FROM Doctors WHERE DoctorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        }
    }

    // Получает список архивированных врачей (с их archivedId)
    public List<Doctor> getArchivedDoctors() throws SQLException {
        List<Doctor> archived = new ArrayList<>();
        String sql = """
        SELECT ArchivedID, DoctorID, FirstName, LastName, Specialization, RoomNumber, Schedule, Email, ArchivedDate
        FROM ArchivedDoctors
        ORDER BY ArchivedDate DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setArchivedId(rs.getInt("ArchivedID"));
                doctor.setDoctorId(rs.getInt("DoctorID"));
                doctor.setFirstName(rs.getString("FirstName"));
                doctor.setLastName(rs.getString("LastName"));
                doctor.setSpecialization(rs.getString("Specialization"));
                doctor.setRoomNumber(rs.getString("RoomNumber"));
                doctor.setSchedule(rs.getString("Schedule"));
                doctor.setEmail(rs.getString("Email"));
                doctor.setArchivedDate(rs.getDate("ArchivedDate") != null ? rs.getDate("ArchivedDate").toLocalDate() : null);
                archived.add(doctor);
            }
        }
        return archived;
    }

    // Восстанавливает врача из архива по его archivedId
    public void restoreDoctor(int archivedId) throws SQLException {
        String selectSql = "SELECT * FROM ArchivedDoctors WHERE ArchivedID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, archivedId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String insertSql = """
                    INSERT INTO Doctors (DoctorID, FirstName, LastName, Specialization, RoomNumber, Schedule, Email)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, rs.getInt("DoctorID"));
                        insertStmt.setString(2, rs.getString("FirstName"));
                        insertStmt.setString(3, rs.getString("LastName"));
                        insertStmt.setString(4, rs.getString("Specialization"));
                        insertStmt.setString(5, rs.getString("RoomNumber"));
                        insertStmt.setString(6, rs.getString("Schedule"));
                        insertStmt.setString(7, rs.getString("Email"));
                        insertStmt.executeUpdate();
                    }
                    String deleteSql = "DELETE FROM ArchivedDoctors WHERE ArchivedID = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, archivedId);
                        deleteStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
