package hospital.view.controller;

import hospital.daomodel.Doctor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocManControllerTest {
    @Test
    public void testCreateDoctor_InvalidFirstName_ThrowsException() {
        Doctor doctor = new Doctor();
        assertThrows(IllegalArgumentException.class, () -> {
            doctor.setFirstName("");   // пустое имя
        });
    }

    @Test
    public void testCreateDoctor_InvalidEmail_ThrowsException() {
        Doctor doctor = new Doctor();
        assertThrows(IllegalArgumentException.class, () -> {
            doctor.setEmail("not-an-email");
        });
    }

    @Test
    public void testCreateDoctor_ValidData_Success() {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Иван");
        doctor.setLastName("Петров");
        doctor.setSpecialization("Терапевт");
        doctor.setRoomNumber("101");
        doctor.setSchedule("Пн-Пт 9-18");
        doctor.setEmail("ivan@hospital.ru");
        assertNotNull(doctor.getFirstName());
        assertEquals("ivan@hospital.ru", doctor.getEmail());
    }
}