package hospital.daomodel;
public class Prescription {
    private int prescriptionId;
    private String medicine;
    private String dosage;
    private String doctorName;
    private String patientName; // новое поле
    public Prescription() {}
    public Prescription(int prescriptionId, String medicine, String dosage, String doctorName) {
        this.prescriptionId = prescriptionId;
        this.medicine = medicine;
        this.dosage = dosage;
        this.doctorName = doctorName;
    }
    public int getPrescriptionId() {
        return prescriptionId;
    }
    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    public String getMedicine() {
        return medicine;
    }
    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }
    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId=" + prescriptionId +
                ", medicine='" + medicine + '\'' +
                ", dosage='" + dosage + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", patientName='" + patientName + '\'' +
                '}';
    }
}
