package hospital;

import hospital.daomodel.MedicalRecord;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDao {

    public List<MedicalRecord> getMedicalRecordsByDoctor(int doctorId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.doctorid = ?
            ORDER BY mr.creationdate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        }
        return records;
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(int patientId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.patientid = ?
            ORDER BY mr.creationdate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        }
        return records;
    }

    public MedicalRecord getMedicalRecordById(int recordId) throws SQLException {
        MedicalRecord record = null;
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.recordid = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    record = mapResultSetToMedicalRecord(rs);
                }
            }
        }
        return record;
    }

    public void addMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = """
            INSERT INTO medicalrecords 
            (patientid, doctorid, appointmentid, disease_history, life_history, 
             objective_status, diagnosis, recommendations, creationdate, lastupdatedate)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, record.getPatientId());
            stmt.setInt(2, record.getDoctorId());

            if (record.getAppointmentId() != null) {
                stmt.setInt(3, record.getAppointmentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, record.getDiseaseHistory());
            stmt.setString(5, record.getLifeHistory());
            stmt.setString(6, record.getObjectiveStatus());
            stmt.setString(7, record.getDiagnosis());
            stmt.setString(8, record.getRecommendations());

            LocalDate now = LocalDate.now();
            stmt.setDate(9, Date.valueOf(now));
            stmt.setDate(10, Date.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setRecordId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Не удалось получить ID созданной записи");
                    }
                }
            }
        }
    }

    public void updateMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = """
            UPDATE medicalrecords 
            SET disease_history = ?, life_history = ?, objective_status = ?, 
                diagnosis = ?, recommendations = ?, lastupdatedate = ?
            WHERE recordid = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, record.getDiseaseHistory());
            stmt.setString(2, record.getLifeHistory());
            stmt.setString(3, record.getObjectiveStatus());
            stmt.setString(4, record.getDiagnosis());
            stmt.setString(5, record.getRecommendations());
            stmt.setDate(6, Date.valueOf(LocalDate.now()));
            stmt.setInt(7, record.getRecordId());

            stmt.executeUpdate();
        }
    }

    public void deleteMedicalRecord(int recordId) throws SQLException {
        String sql = "DELETE FROM medicalrecords WHERE recordid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            stmt.executeUpdate();
        }
    }

    public List<MedicalRecord> searchMedicalRecordsByDoctor(int doctorId, String searchTerm) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.doctorid = ? 
              AND (LOWER(p.firstname || ' ' || p.lastname) LIKE LOWER(?)
                   OR LOWER(mr.diagnosis) LIKE LOWER(?)
                   OR LOWER(mr.disease_history) LIKE LOWER(?))
            ORDER BY mr.creationdate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        }
        return records;
    }

    public List<MedicalRecord> searchMedicalRecordsByPatient(int patientId, String searchTerm) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.patientid = ? 
              AND (LOWER(d.firstname || ' ' || d.lastname) LIKE LOWER(?)
                   OR LOWER(mr.diagnosis) LIKE LOWER(?)
                   OR LOWER(mr.disease_history) LIKE LOWER(?))
            ORDER BY mr.creationdate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        }
        return records;
    }

    public List<MedicalRecord> getMedicalRecordsByAppointment(int appointmentId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.appointmentid = ?
            ORDER BY mr.creationdate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        }
        return records;
    }

    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();

        // Основные поля
        record.setRecordId(rs.getInt("recordid"));
        record.setPatientId(rs.getInt("patientid"));
        record.setDoctorId(rs.getInt("doctorid"));

        // AppointmentId может быть NULL
        int appointmentId = rs.getInt("appointmentid");
        if (!rs.wasNull()) {
            record.setAppointmentId(appointmentId);
        }

        // Медицинские данные
        record.setDiseaseHistory(rs.getString("disease_history"));
        record.setLifeHistory(rs.getString("life_history"));
        record.setObjectiveStatus(rs.getString("objective_status"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setRecommendations(rs.getString("recommendations"));

        // Даты
        Date creationDate = rs.getDate("creationdate");
        if (creationDate != null) {
            record.setCreationDate(creationDate.toLocalDate());
        }

        Date lastUpdateDate = rs.getDate("lastupdatedate");
        if (lastUpdateDate != null) {
            record.setLastUpdateDate(lastUpdateDate.toLocalDate());
        }

        // Имена для отображения
        record.setPatientName(rs.getString("patient_name"));
        record.setDoctorName(rs.getString("doctor_name"));

        return record;
    }

    /**
     * Проверяет существование медицинской записи для указанного приема
     */
    public boolean existsMedicalRecordForAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicalrecords WHERE appointmentid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Получает количество медицинских записей для пациента
     */
    public int getMedicalRecordsCountByPatient(int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicalrecords WHERE patientid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Получает количество медицинских записей для врача
     */
    public int getMedicalRecordsCountByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicalrecords WHERE doctorid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Получает последнюю медицинскую запись для пациента
     */
    public MedicalRecord getLastMedicalRecordByPatient(int patientId) throws SQLException {
        String sql = """
            SELECT mr.*, 
                   p.firstname || ' ' || p.lastname as patient_name, 
                   d.firstname || ' ' || d.lastname as doctor_name
            FROM medicalrecords mr
            LEFT JOIN patients p ON mr.patientid = p.patientid
            LEFT JOIN doctors d ON mr.doctorid = d.doctorid
            WHERE mr.patientid = ?
            ORDER BY mr.creationdate DESC
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicalRecord(rs);
                }
            }
        }
        return null;
    }
}