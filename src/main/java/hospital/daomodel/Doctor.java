package hospital.daomodel;
import java.util.regex.Pattern;
public class Doctor {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private int doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String roomNumber;
    private String schedule;
    private String email;
    // Геттеры
    public int getDoctorId() { return doctorId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public String getRoomNumber() { return roomNumber; }
    public String getSchedule() { return schedule; }
    public String getEmail() { return email; }
    // Сеттеры с валидацией
    public void setDoctorId(int doctorId) {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("ID врача должно быть положительным числом");
        }
        this.doctorId = doctorId;
    }
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя врача не может быть пустым");
        }
        this.firstName = firstName.trim();
    }
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия врача не может быть пустой");
        }
        this.lastName = lastName.trim();
    }
    public void setSpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            throw new IllegalArgumentException("Специализация не может быть пустой");
        }
        this.specialization = specialization.trim();
    }
    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер кабинета не может быть пустым");
        }
        this.roomNumber = roomNumber.trim();
    }
    public void setSchedule(String schedule) {
        if (schedule == null || schedule.trim().isEmpty()) {
            throw new IllegalArgumentException("График работы не может быть пустым");
        }
        this.schedule = schedule.trim();
    }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Неверный формат email");
        }
        this.email = email.trim().toLowerCase();
    }
}
