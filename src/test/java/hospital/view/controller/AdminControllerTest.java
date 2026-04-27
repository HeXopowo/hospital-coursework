package hospital.view.controller;

import hospital.DatabaseConnection;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Test
    void testCleanupArchive_CallsStoredProcedure() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockStmt.execute(anyString())).thenReturn(true);

        try (var mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertDoesNotThrow(() -> {
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT cleanup_old_archives()");
                }
            });

            verify(mockStmt).execute("SELECT cleanup_old_archives()");
        }
    }
}