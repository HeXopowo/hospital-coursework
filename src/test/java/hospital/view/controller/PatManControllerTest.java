package hospital.view.controller;

import hospital.daomodel.Patient;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class PatManControllerTest {
    @Test
    public void testCreatePatient_InvalidEmail_ThrowsException() {
        Patient patient = new Patient();
        assertThrows(IllegalArgumentException.class, () -> {
            patient.setEmail("bad-email");
        });
    }

    @Test
    public void testCreatePatient_InvalidSnils_ThrowsException() {
        Patient patient = new Patient();
        assertThrows(IllegalArgumentException.class, () -> {
            patient.setSnils("123-456-789"); // не хватает двух цифр в конце
        });
    }

    @Test
    public void testCreatePatient_ValidData_Success() {
        Patient patient = new Patient();
        patient.setFirstName("Анна");
        patient.setLastName("Сидорова");
        patient.setBirthDate(LocalDate.of(1990, 5, 15));
        patient.setEmail("anna@mail.ru");
        patient.setSnils("123-456-789 01");
        patient.setPolicyOMS("1234567890123456");
        patient.setDistrict(2);
        assertDoesNotThrow(() -> patient.setEmail("anna@mail.ru"));
    }
}