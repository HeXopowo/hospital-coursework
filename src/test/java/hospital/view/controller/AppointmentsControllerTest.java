package hospital.view.controller;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class AppointmentsControllerTest {
    @Test
    public void testIsPastDateForbidden_PastDateWithForbiddenStatus_ReturnsTrue() throws Exception {
        AppointmentsController controller = new AppointmentsController();
        java.lang.reflect.Method method = AppointmentsController.class.getDeclaredMethod("isPastDateForbidden", LocalDateTime.class, String.class);
        method.setAccessible(true);
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        boolean result = (boolean) method.invoke(controller, past, "Запланирован");
        assertTrue(result);
    }

    @Test
    public void testIsPastDateForbidden_FutureDate_ReturnsFalse() throws Exception {
        AppointmentsController controller = new AppointmentsController();
        java.lang.reflect.Method method = AppointmentsController.class.getDeclaredMethod("isPastDateForbidden", LocalDateTime.class, String.class);
        method.setAccessible(true);
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        boolean result = (boolean) method.invoke(controller, future, "Запланирован");
        assertFalse(result);
    }
}