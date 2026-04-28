package hospital.daomodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientTest {

    @Spy
    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = spy(new Patient());
    }

    @Test
    void testSetValidFirstName_Success() {
        patient.setFirstName("Иван");
        assertEquals("Иван", patient.getFirstName());
        verify(patient, times(1)).setFirstName("Иван");
    }

    @Test
    void testSetInvalidFirstName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> patient.setFirstName(null));
        assertNull(patient.getFirstName());
        // Сеттер был вызван (с null) и выбросил исключение – проверяем только вызов
        verify(patient, times(1)).setFirstName(null);
    }

    @Test
    void testSetValidEmail_Success() {
        patient.setEmail("test@hospital.ru");
        assertEquals("test@hospital.ru", patient.getEmail());
        verify(patient, times(1)).setEmail("test@hospital.ru");
    }

    @Test
    void testSetInvalidEmail_ThrowsException_AndStateNotChanged() {
        // Ожидаем исключение
        assertThrows(IllegalArgumentException.class, () -> patient.setEmail("invalid"));
        // Поле email осталось null (не изменилось)
        assertNull(patient.getEmail());
        // Сеттер был вызван один раз – проверим это, если нужно
        verify(patient, times(1)).setEmail("invalid");
    }
}