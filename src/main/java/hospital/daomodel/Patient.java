package hospital.daomodel;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class Patient {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\-\\s()]{7,20}$");
    private static final Pattern SNILS_PATTERN = Pattern.compile("^\\d{3}-\\d{3}-\\d{3} \\d{2}$");
    private static final Pattern POLICY_PATTERN = Pattern.compile("^\\d{16}$");
    private static final int MIN_AGE = 0;
    private static final int MAX_AGE = 120;

    private int patientId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private String snils;
    private String policyOMS;
    private int district; // Изменено с String на int
    private String address;

    // Геттеры
    public int getPatientId() { return patientId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getSnils() { return snils; }
    public String getPolicyOMS() { return policyOMS; }
    public int getDistrict() { return district; } // Изменено на int
    public String getAddress() { return address; }

    // Сеттеры с валидацией
    public void setPatientId(int patientId) {
        if (patientId <= 0) {
            throw new IllegalArgumentException("ID пациента должно быть положительным числом");
        }
        this.patientId = patientId;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пациента не может быть пустым");
        }
        this.firstName = firstName.trim();
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия пациента не может быть пустой");
        }
        this.lastName = lastName.trim();
    }

    public void setBirthDate(LocalDate birthDate) {
        if (birthDate != null) {
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < MIN_AGE || age > MAX_AGE) {
                throw new IllegalArgumentException("Недопустимый возраст пациента");
            }
        }
        this.birthDate = birthDate;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("Неверный формат телефона");
            }
            this.phoneNumber = phoneNumber.trim();
        } else {
            this.phoneNumber = null;
        }
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

    public void setSnils(String snils) {
        if (snils != null && !snils.trim().isEmpty()) {
            if (!SNILS_PATTERN.matcher(snils).matches()) {
                throw new IllegalArgumentException("Неверный формат СНИЛС. Используйте формат: XXX-XXX-XXX XX");
            }
            this.snils = snils.trim();
        } else {
            this.snils = null;
        }
    }

    public void setPolicyOMS(String policyOMS) {
        if (policyOMS != null && !policyOMS.trim().isEmpty()) {
            if (!POLICY_PATTERN.matcher(policyOMS).matches()) {
                throw new IllegalArgumentException("Неверный формат полиса ОМС. Должно быть 16 цифр");
            }
            this.policyOMS = policyOMS.trim();
        } else {
            this.policyOMS = null;
        }
    }

    public void setDistrict(int district) { // Изменено на int
        if (district <= 0) {
            throw new IllegalArgumentException("Номер участка должен быть положительным числом");
        }
        this.district = district;
    }

    public void setAddress(String address) {
        this.address = address != null ? address.trim() : null;
    }
}