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
        ORDER BY LastName, FirstName
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);

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
}
