package hospital.api;

import hospital.DoctorDao;
import io.javalin.Javalin;
import java.util.Map;

public class DoctorApiController {
    private final DoctorDao doctorDao = new DoctorDao();

    public void registerRoutes(Javalin app) {
        app.get("/api/doctors/search", ctx -> {
            String term = ctx.queryParam("term");
            if (term == null || term.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Параметр 'term' обязателен"));
                return;
            }
            ctx.json(doctorDao.searchDoctors(term.trim()));
        });
    }
}