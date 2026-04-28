package hospital.api;

import io.javalin.Javalin;

public class RestApiServer {
    public static void start() {
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false)
                .start(8080);
        new DoctorApiController().registerRoutes(app);
        new AppointmentApiController().registerRoutes(app);
        new ArchiveApiController().registerRoutes(app);
        System.out.println("REST API started on http://localhost:8080");
    }
}