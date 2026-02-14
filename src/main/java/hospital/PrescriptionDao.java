package hospital;
import hospital.daomodel.Prescription;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDao {
    public List<Prescription> getPrescriptionsByRole(String role, int roleId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql;

        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    p.PrescriptionID, 
                    p.Medicine, 
                    p.Dosage,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    pt.FirstName || ' ' || pt.LastName AS PatientName
                FROM Prescriptions p
                JOIN Patients pt ON p.PatientID = pt.PatientID
                JOIN Doctors d ON p.DoctorID = d.DoctorID
                WHERE p.PatientID = ?
            """;
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    p.PrescriptionID, 
                    p.Medicine, 
                    p.Dosage,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    pt.FirstName || ' ' || pt.LastName AS PatientName
                FROM Prescriptions p
                JOIN Patients pt ON p.PatientID = pt.PatientID
                JOIN Doctors d ON p.DoctorID = d.DoctorID
                WHERE p.DoctorID = ?
            """;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            sql = """
                SELECT 
                    p.PrescriptionID, 
                    p.Medicine, 
                    p.Dosage,
                    d.FirstName || ' ' || d.LastName AS DoctorName,
                    pt.FirstName || ' ' || pt.LastName AS PatientName
                FROM Prescriptions p
                JOIN Patients pt ON p.PatientID = pt.PatientID
                JOIN Doctors d ON p.DoctorID = d.DoctorID
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
                    Prescription prescription = new Prescription();
                    prescription.setPrescriptionId(rs.getInt("PrescriptionID"));
                    prescription.setMedicine(rs.getString("Medicine"));
                    prescription.setDosage(rs.getString("Dosage"));
                    prescription.setDoctorName(rs.getString("DoctorName"));
                    prescription.setPatientName(rs.getString("PatientName"));
                    prescriptions.add(prescription);
                }
            }
        }
        return prescriptions;
    }

    public List<String> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<String> patients = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.FirstName || ' ' || p.LastName AS FullName
            FROM Patients p
            JOIN Appointments a ON p.PatientID = a.PatientID
            WHERE a.DoctorID = ?
            ORDER BY FullName
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(rs.getString("FullName"));
                }
            }
        }
        return patients;
    }

    public void addPrescription(int doctorId, String patientFullName, String medicine, String dosage) throws SQLException {
        String[] parts = patientFullName.split(" ");
        if (parts.length < 2) throw new SQLException("Некорректное имя пациента");

        String firstName = parts[0];
        String lastName = parts[1];

        String getPatientIdSql = "SELECT PatientID FROM Patients WHERE FirstName = ? AND LastName = ?";
        String insertSql = "INSERT INTO Prescriptions (PatientID, DoctorID, Medicine, Dosage) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psGetPatient = conn.prepareStatement(getPatientIdSql);
             PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

            psGetPatient.setString(1, firstName);
            psGetPatient.setString(2, lastName);

            try (ResultSet rs = psGetPatient.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Пациент не найден");
                }
                int patientId = rs.getInt("PatientID");

                psInsert.setInt(1, patientId);
                psInsert.setInt(2, doctorId);
                psInsert.setString(3, medicine);
                psInsert.setString(4, dosage);
                psInsert.executeUpdate();
            }
        }
    }

    public void deletePrescription(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM Prescriptions WHERE PrescriptionID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescriptionId);
            stmt.executeUpdate();
        }
    }

    public void updatePrescription(int prescriptionId, String newMedicine, String newDosage) throws SQLException {
        String sql = "UPDATE Prescriptions SET Medicine = ?, Dosage = ? WHERE PrescriptionID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newMedicine);
            stmt.setString(2, newDosage);
            stmt.setInt(3, prescriptionId);
            stmt.executeUpdate();
        }
    }

    public List<Prescription> searchPrescriptions(String searchTerm, String role, int roleId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql;

        if ("PATIENT".equalsIgnoreCase(role)) {
            sql = """
            SELECT 
                p.PrescriptionID, 
                p.Medicine, 
                p.Dosage,
                d.FirstName || ' ' || d.LastName AS DoctorName,
                pt.FirstName || ' ' || pt.LastName AS PatientName
            FROM Prescriptions p
            JOIN Patients pt ON p.PatientID = pt.PatientID
            JOIN Doctors d ON p.DoctorID = d.DoctorID
            WHERE p.PatientID = ?
              AND (LOWER(d.FirstName) LIKE LOWER(?) 
               OR LOWER(d.LastName) LIKE LOWER(?)
               OR LOWER(d.FirstName || ' ' || d.LastName) LIKE LOWER(?))
        """;
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            sql = """
            SELECT 
                p.PrescriptionID, 
                p.Medicine, 
                p.Dosage,
                d.FirstName || ' ' || d.LastName AS DoctorName,
                pt.FirstName || ' ' || pt.LastName AS PatientName
            FROM Prescriptions p
            JOIN Patients pt ON p.PatientID = pt.PatientID
            JOIN Doctors d ON p.DoctorID = d.DoctorID
            WHERE p.DoctorID = ?
              AND (LOWER(pt.FirstName) LIKE LOWER(?) 
               OR LOWER(pt.LastName) LIKE LOWER(?)
               OR LOWER(pt.FirstName || ' ' || pt.LastName) LIKE LOWER(?))
        """;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            sql = """
            SELECT 
                p.PrescriptionID, 
                p.Medicine, 
                p.Dosage,
                d.FirstName || ' ' || d.LastName AS DoctorName,
                pt.FirstName || ' ' || pt.LastName AS PatientName
            FROM Prescriptions p
            JOIN Patients pt ON p.PatientID = pt.PatientID
            JOIN Doctors d ON p.DoctorID = d.DoctorID
            WHERE LOWER(d.FirstName) LIKE LOWER(?) 
               OR LOWER(d.LastName) LIKE LOWER(?)
               OR LOWER(d.FirstName || ' ' || d.LastName) LIKE LOWER(?)
               OR LOWER(pt.FirstName) LIKE LOWER(?)
               OR LOWER(pt.LastName) LIKE LOWER(?)
               OR LOWER(pt.FirstName || ' ' || pt.LastName) LIKE LOWER(?)
        """;
        } else {
            throw new IllegalArgumentException("Неподдерживаемая роль: " + role);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";

            if ("ADMIN".equalsIgnoreCase(role)) {
                for (int i = 1; i <= 6; i++) {
                    stmt.setString(i, likeTerm);
                }
            } else {
                stmt.setInt(1, roleId);
                for (int i = 2; i <= 4; i++) {
                    stmt.setString(i, likeTerm);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prescription prescription = new Prescription();
                    prescription.setPrescriptionId(rs.getInt("PrescriptionID"));
                    prescription.setMedicine(rs.getString("Medicine"));
                    prescription.setDosage(rs.getString("Dosage"));
                    prescription.setDoctorName(rs.getString("DoctorName"));
                    prescription.setPatientName(rs.getString("PatientName"));
                    prescriptions.add(prescription);
                }
            }
        }
        return prescriptions;
    }
}