package hospital.api;

import hospital.AppointmentDao;
import io.javalin.Javalin;
import java.util.Map;

public class AppointmentApiController {
    private final AppointmentDao appointmentDao = new AppointmentDao();

    public void registerRoutes(Javalin app) {
        app.get("/api/appointments/search", ctx -> {
            String term = ctx.queryParam("term");
            String role = ctx.queryParam("role");
            String roleIdStr = ctx.queryParam("roleId");

            if (term == null || term.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Параметр 'term' обязателен"));
                return;
            }
            if (role == null || roleIdStr == null) {
                ctx.status(400).json(Map.of("error", "Параметры 'role' и 'roleId' обязательны"));
                return;
            }

            try {
                int roleId = Integer.parseInt(roleIdStr);
                var result = appointmentDao.searchAppointments(term.trim(), role, roleId);
                ctx.json(result);
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "roleId должен быть числом"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
            }
        });
    }
}