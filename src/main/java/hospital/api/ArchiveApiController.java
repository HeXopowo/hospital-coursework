package hospital.api;

import hospital.DatabaseConnection;
import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ArchiveApiController {
    public void registerRoutes(Javalin app) {
        app.post("/api/archive/cleanup", ctx -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT cleanup_old_archives()");
                ctx.json(Map.of("message", "Архив очищен от записей старше 50 лет"));
            } catch (SQLException e) {
                ctx.status(500).json(Map.of("error", "Ошибка очистки архива: " + e.getMessage()));
            }
        });
    }
}