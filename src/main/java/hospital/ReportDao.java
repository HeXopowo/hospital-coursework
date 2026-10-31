package hospital;

import hospital.daomodel.Report;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<Report> generateReport(String reportType,
                                          LocalDate from, LocalDate to) throws SQLException {
        return switch (reportType) {
            case "Приёмы за период"        -> appointmentsDetailed(from, to);
            case "Загрузка врачей"         -> doctorWorkload(from, to);
            case "Пациенты по участкам"    -> patientsByDistrict();
            case "Статистика по статусам"  -> statusStatistics(from, to);
            case "Общая сводка"            -> summary(from, to);
            default -> throw new IllegalArgumentException(
                    "Неизвестный тип отчёта: " + reportType);
        };
    }

    // Приёмы за период дата | врач | пациент | статус
    private List<Report> appointmentsDetailed(LocalDate from, LocalDate to) throws SQLException {
        List<Report> result = new ArrayList<>();
        String sql = """
            SELECT a.AppointmentDateTime,
                   d.FirstName || ' ' || d.LastName AS DoctorName,
                   p.FirstName || ' ' || p.LastName AS PatientName,
                   a.Status
            FROM Appointments a
            JOIN Doctors d  ON a.DoctorID  = d.DoctorID
            JOIN Patients p ON a.PatientID = p.PatientID
            WHERE DATE(a.AppointmentDateTime) BETWEEN ? AND ?
            ORDER BY a.AppointmentDateTime
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime dt = rs.getTimestamp("AppointmentDateTime").toLocalDateTime();
                    result.add(new Report(
                            dt.format(DT_FORMAT),
                            1,
                            rs.getString("DoctorName"),    // 3-й столбец — Врач
                            rs.getString("PatientName"),   // 4-й — Пациент
                            rs.getString("Status")         // 5-й — Статус
                    ));
                }
            }
        }
        return result;
    }

    // Загрузка врачей
    private List<Report> doctorWorkload(LocalDate from, LocalDate to) throws SQLException {
        List<Report> result = new ArrayList<>();
        String sql = """
            SELECT d.FirstName || ' ' || d.LastName AS DoctorName,
                   d.Specialization,
                   COUNT(a.AppointmentID)           AS Total
            FROM Doctors d
            LEFT JOIN Appointments a
                   ON d.DoctorID = a.DoctorID
                  AND DATE(a.AppointmentDateTime) BETWEEN ? AND ?
            GROUP BY d.DoctorID, d.FirstName, d.LastName, d.Specialization
            ORDER BY Total DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new Report(
                            rs.getString("DoctorName"),
                            rs.getInt("Total"),
                            rs.getString("Specialization")
                    ));
                }
            }
        }
        return result;
    }

    // Пациенты по участкам
    private List<Report> patientsByDistrict() throws SQLException {
        List<Report> result = new ArrayList<>();
        String sql = """
            SELECT District, COUNT(*) AS Total
            FROM Patients
            GROUP BY District
            ORDER BY District
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new Report(
                        "Участок " + rs.getInt("District"),
                        rs.getInt("Total"),
                        null    // ← было "пациентов", теперь пусто
                ));
            }
        }
        return result;
    }

    // Статистика по статусам
    private List<Report> statusStatistics(LocalDate from, LocalDate to) throws SQLException {
        List<Report> result = new ArrayList<>();
        String sql = """
            SELECT Status, COUNT(*) AS Total
            FROM Appointments
            WHERE DATE(AppointmentDateTime) BETWEEN ? AND ?
            GROUP BY Status
            ORDER BY Total DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            int totalAll = 0;
            List<Object[]> rows = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int cnt = rs.getInt("Total");
                    totalAll += cnt;
                    rows.add(new Object[]{rs.getString("Status"), cnt});
                }
            }
            for (Object[] row : rows) {
                int cnt = (Integer) row[1];
                double pct = totalAll > 0 ? cnt * 100.0 / totalAll : 0;
                result.add(new Report((String) row[0], cnt,
                        String.format("%.1f%% от общего числа", pct)));
            }
        }
        return result;
    }

    // Общая сводка
    private List<Report> summary(LocalDate from, LocalDate to) throws SQLException {
        List<Report> result = new ArrayList<>();
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM Appointments WHERE DATE(AppointmentDateTime) BETWEEN ? AND ?) AS TotalAppointments,
              (SELECT COUNT(DISTINCT DoctorID)  FROM Appointments WHERE DATE(AppointmentDateTime) BETWEEN ? AND ?) AS UniqueDoctors,
              (SELECT COUNT(DISTINCT PatientID) FROM Appointments WHERE DATE(AppointmentDateTime) BETWEEN ? AND ?) AS UniquePatients,
              (SELECT COUNT(*) FROM Patients) AS TotalPatients,
              (SELECT COUNT(*) FROM Doctors)  AS TotalDoctors
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                stmt.setDate(i, Date.valueOf(i % 2 == 1 ? from : to));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.add(new Report("Всего приёмов за период",   rs.getInt("TotalAppointments"), ""));
                    result.add(new Report("Задействовано врачей",     rs.getInt("UniqueDoctors"),     "уникальных"));
                    result.add(new Report("Обслужено пациентов",      rs.getInt("UniquePatients"),    "уникальных"));
                    result.add(new Report("Всего пациентов в БД",     rs.getInt("TotalPatients"),     ""));
                    result.add(new Report("Всего врачей в БД",        rs.getInt("TotalDoctors"),      ""));
                }
            }
        }
        return result;
    }
}