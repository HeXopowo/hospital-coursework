package hospital;

import hospital.daomodel.User;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

// Примечание: для работы теста нужна реальная БД с тестовыми данными.
// Можно использовать in-memory H2 или мокировать Connection.
public class UserDaoTest {
    @Test
    public void testAuthenticate_ValidUser_ReturnsUser() throws SQLException {
        UserDao userDao = new UserDao();
        // Замените логин/пароль на существующие в вашей БД
        User user = userDao.authenticate("neadmin", "tochnoadmin123");
        assertNotNull(user);
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    public void testAuthenticate_InvalidPassword_ReturnsNull() throws SQLException {
        UserDao userDao = new UserDao();
        User user = userDao.authenticate("admin", "wrong");
        assertNull(user);
    }
}