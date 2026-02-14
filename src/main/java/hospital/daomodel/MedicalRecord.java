package hospital.daomodel;

import java.time.LocalDate;

public class MedicalRecord {
    private int recordId;
    private int patientId;
    private int doctorId;
    private Integer appointmentId;
    private LocalDate creationDate;
    private LocalDate lastUpdateDate;

    // Поля из базы данных
    private String diseaseHistory;
    private String lifeHistory;
    private String objectiveStatus;
    private String diagnosis;
    private String recommendations;

    // Дополнительные поля для отображения ФИО
    private String patientName;
    private String doctorName;

    // Конструкторы
    public MedicalRecord() {}

    public MedicalRecord(int patientId, int doctorId, String diseaseHistory,
                         String lifeHistory, String objectiveStatus,
                         String diagnosis, String recommendations) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diseaseHistory = diseaseHistory;
        this.lifeHistory = lifeHistory;
        this.objectiveStatus = objectiveStatus;
        this.diagnosis = diagnosis;
        this.recommendations = recommendations;
        this.creationDate = LocalDate.now();
        this.lastUpdateDate = LocalDate.now();
    }

    // Геттеры и сеттеры для всех полей
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public LocalDate getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(LocalDate lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public String getDiseaseHistory() { return diseaseHistory; }
    public void setDiseaseHistory(String diseaseHistory) { this.diseaseHistory = diseaseHistory; }

    public String getLifeHistory() { return lifeHistory; }
    public void setLifeHistory(String lifeHistory) { this.lifeHistory = lifeHistory; }

    public String getObjectiveStatus() { return objectiveStatus; }
    public void setObjectiveStatus(String objectiveStatus) { this.objectiveStatus = objectiveStatus; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    // Методы для обратной совместимости (если используются в UI)
    public String getHistory() {
        StringBuilder history = new StringBuilder();
        if (diseaseHistory != null && !diseaseHistory.isEmpty()) {
            history.append("Анамнез заболевания: ").append(diseaseHistory).append("\n");
        }
        if (lifeHistory != null && !lifeHistory.isEmpty()) {
            history.append("История жизни: ").append(lifeHistory).append("\n");
        }
        if (objectiveStatus != null && !objectiveStatus.isEmpty()) {
            history.append("Объективный статус: ").append(objectiveStatus);
        }
        return history.toString();
    }

    public String getTreatment() {
        return recommendations != null ? recommendations : "";
    }
}