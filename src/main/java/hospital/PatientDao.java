package hospital;

import hospital.daomodel.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDao {

    // Получение всех пациентов
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients ORDER BY LastName, FirstName";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }
        }
        return patients;
    }

    // Получение пациента по ID
    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM Patients WHERE PatientID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPatientFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Добавление нового пациента
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (FirstName, LastName, BirthDate, PhoneNumber, Email, " +
                "SNILS, PolicyOMS, District, Address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPatientParameters(stmt, patient);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setPatientId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Обновление данных пациента
    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET FirstName = ?, LastName = ?, BirthDate = ?, PhoneNumber = ?, " +
                "Email = ?, SNILS = ?, PolicyOMS = ?, District = ?, Address = ? " +
                "WHERE PatientID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setPatientParameters(stmt, patient);
            stmt.setInt(10, patient.getPatientId());
            stmt.executeUpdate();
        }
    }

    // Удаление пациента
    public void deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM Patients WHERE PatientID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    // Поиск пациентов по ФИО (глобальный)
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = """
            SELECT * FROM Patients 
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
                    patients.add(extractPatientFromResultSet(rs));
                }
            }
        }
        return patients;
    }

    // Получение всех пациентов, которые были на приёме у данного врача
    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.* 
            FROM Patients p
            JOIN Appointments a ON p.PatientID = a.PatientID
            WHERE a.DoctorID = ?
            ORDER BY p.LastName, p.FirstName
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(extractPatientFromResultSet(rs));
                }
            }
        }
        return patients;
    }

    // Поиск пациентов (только среди тех, кто был на приёме у врача) по ФИО
    public List<Patient> searchPatientsByDoctor(int doctorId, String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.* 
            FROM Patients p
            JOIN Appointments a ON p.PatientID = a.PatientID
            WHERE a.DoctorID = ?
              AND (LOWER(p.FirstName) LIKE LOWER(?) 
                   OR LOWER(p.LastName) LIKE LOWER(?)
                   OR LOWER(p.FirstName || ' ' || p.LastName) LIKE LOWER(?))
            ORDER BY p.LastName, p.FirstName
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeTerm = "%" + searchTerm + "%";
            stmt.setInt(1, doctorId);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(extractPatientFromResultSet(rs));
                }
            }
        }
        return patients;
    }

    // Извлечение объекта Patient из ResultSet
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("PatientID"));
        patient.setFirstName(rs.getString("FirstName"));
        patient.setLastName(rs.getString("LastName"));
        Date birthDate = rs.getDate("BirthDate");
        patient.setBirthDate(birthDate != null ? birthDate.toLocalDate() : null);
        patient.setPhoneNumber(rs.getString("PhoneNumber"));
        patient.setEmail(rs.getString("Email"));
        patient.setSnils(rs.getString("SNILS"));
        patient.setPolicyOMS(rs.getString("PolicyOMS"));
        patient.setDistrict(rs.getInt("District"));
        patient.setAddress(rs.getString("Address"));
        return patient;
    }

    // Установка параметров для PreparedStatement (в порядке столбцов)
    private void setPatientParameters(PreparedStatement stmt, Patient patient) throws SQLException {
        stmt.setString(1, patient.getFirstName());
        stmt.setString(2, patient.getLastName());
        if (patient.getBirthDate() != null) {
            stmt.setDate(3, Date.valueOf(patient.getBirthDate()));
        } else {
            stmt.setNull(3, Types.DATE);
        }
        stmt.setString(4, patient.getPhoneNumber());
        stmt.setString(5, patient.getEmail());
        stmt.setString(6, patient.getSnils());
        stmt.setString(7, patient.getPolicyOMS());
        stmt.setInt(8, patient.getDistrict());
        stmt.setString(9, patient.getAddress());
    }
}