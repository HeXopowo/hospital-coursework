package hospital;

import hospital.daomodel.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoctorDao {
    private static final Logger logger = Logger.getLogger(DoctorDao.class.getName());

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM Doctors";
        logger.log(Level.FINE, "Executing getAllDoctors: {0}", sql);
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка в getAllDoctors", e);
            throw e;
        }
        return doctors;
    }

    public Doctor getDoctorById(int doctorId) throws SQLException {
        Doctor doctor = null;
        String sql = "SELECT * FROM Doctors WHERE DoctorID = ?";
        logger.log(Level.FINE, "Executing getDoctorById for ID: {0}", doctorId);
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка в getDoctorById для ID " + doctorId, e);
            throw e;
        }
        return doctor;
    }

    public void addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO Doctors (FirstName, LastName, Specialization, RoomNumber, Schedule, Email) VALUES (?, ?, ?, ?, ?, ?)";
        logger.log(Level.FINE, "Adding doctor: {0} {1}", new Object[]{doctor.getFirstName(), doctor.getLastName()});
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getFirstName());
            stmt.setString(2, doctor.getLastName());
            stmt.setString(3, doctor.getSpecialization());
            stmt.setString(4, doctor.getRoomNumber());
            stmt.setString(5, doctor.getSchedule());
            stmt.setString(6, doctor.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении врача", e);
            throw e;
        }
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE Doctors SET FirstName = ?, LastName = ?, Specialization = ?, RoomNumber = ?, Schedule = ?, Email = ? WHERE DoctorID = ?";
        logger.log(Level.FINE, "Updating doctor ID: {0}", doctor.getDoctorId());
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении врача ID " + doctor.getDoctorId(), e);
            throw e;
        }
    }

    public void deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM Doctors WHERE DoctorID = ?";
        logger.log(Level.FINE, "Deleting doctor ID: {0}", doctorId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении врача ID " + doctorId, e);
            throw e;
        }
    }

    public List<Doctor> searchDoctors(String searchTerm) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        // Исправленный SQL – 5 плейсхолдеров
        String sql = """
            SELECT * FROM Doctors 
            WHERE LOWER(FirstName) LIKE LOWER(?) 
               OR LOWER(LastName) LIKE LOWER(?)
               OR LOWER(FirstName || ' ' || LastName) LIKE LOWER(?)
               OR LOWER(LastName || ' ' || FirstName) LIKE LOWER(?)
               OR LOWER(Specialization) LIKE LOWER(?)
            ORDER BY LastName, FirstName
        """;
        logger.log(Level.FINE, "Searching doctors with term: {0}", searchTerm);
        logger.log(Level.FINE, "SQL: {0}", sql);

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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при поиске врачей по запросу '" + searchTerm + "'", e);
            throw e;
        }
        return doctors;
    }

    public void archiveDoctor(int doctorId, String archivedBy) throws SQLException {
        logger.log(Level.INFO, "Archiving doctor ID: {0} by {1}", new Object[]{doctorId, archivedBy});
        Doctor doctor = getDoctorById(doctorId);
        if (doctor == null) {
            logger.log(Level.WARNING, "Doctor with ID {0} not found for archiving", doctorId);
            return;
        }

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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при архивации врача ID " + doctorId, e);
            throw e;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Users WHERE DoctorID = ?")) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ошибка при удалении записи пользователя для врача ID " + doctorId, e);
        }

        String deleteSql = "DELETE FROM Doctors WHERE DoctorID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, doctorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении врача ID " + doctorId, e);
            throw e;
        }
    }

    public List<Doctor> getArchivedDoctors() throws SQLException {
        List<Doctor> archived = new ArrayList<>();
        String sql = """
            SELECT ArchivedID, DoctorID, FirstName, LastName, Specialization, RoomNumber, Schedule, Email, ArchivedDate
            FROM ArchivedDoctors
            ORDER BY ArchivedDate DESC
        """;
        logger.log(Level.FINE, "Loading archived doctors");
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке архивированных врачей", e);
            throw e;
        }
        return archived;
    }

    public void restoreDoctor(int archivedId) throws SQLException {
        logger.log(Level.INFO, "Restoring doctor from archive, archivedId={0}", archivedId);
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
                } else {
                    logger.log(Level.WARNING, "Archived doctor with id {0} not found", archivedId);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при восстановлении врача archivedId " + archivedId, e);
            throw e;
        }
    }
}