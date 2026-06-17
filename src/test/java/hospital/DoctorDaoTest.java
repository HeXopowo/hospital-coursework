package hospital;

import hospital.daomodel.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DoctorDaoTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;
    private DoctorDao doctorDao;
    private MockedStatic<DatabaseConnection> mockedStatic;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        // Мокаем статический метод DatabaseConnection.getConnection()
        mockedStatic = mockStatic(DatabaseConnection.class);
        mockedStatic.when(DatabaseConnection::getConnection).thenReturn(connection);

        // Настройка для методов, использующих createStatement() (например, getAllDoctors)
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Настройка для методов, использующих prepareStatement()
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        doctorDao = new DoctorDao();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void testGetAllDoctors_ExecutesCorrectQuery() throws SQLException {
        // Мокаем ResultSet: сначала одна запись, потом пусто
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Мокаем ВСЕ поля, которые читаются в extractDoctorFromResultSet
        when(resultSet.getInt("DoctorID")).thenReturn(1);
        when(resultSet.getString("FirstName")).thenReturn("Иван");
        when(resultSet.getString("LastName")).thenReturn("Петров");
        when(resultSet.getString("Specialization")).thenReturn("Терапевт");
        when(resultSet.getString("RoomNumber")).thenReturn("101");
        when(resultSet.getString("Schedule")).thenReturn("Пн-Пт 9:00-18:00");
        when(resultSet.getString("Email")).thenReturn("ivan@hospital.ru");

        List<Doctor> doctors = doctorDao.getAllDoctors();

        assertNotNull(doctors);
        assertEquals(1, doctors.size());
        Doctor d = doctors.get(0);
        assertEquals("Иван", d.getFirstName());
        assertEquals("Петров", d.getLastName());
        assertEquals("ivan@hospital.ru", d.getEmail());

        verify(connection).createStatement();
        verify(statement).executeQuery(anyString());
        verify(resultSet, atLeastOnce()).next();
    }

    @Test
    void testGetDoctorById_ValidId_ReturnsDoctor() throws SQLException {
        int doctorId = 10;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("DoctorID")).thenReturn(doctorId);
        when(resultSet.getString("FirstName")).thenReturn("Анна");
        when(resultSet.getString("LastName")).thenReturn("Сидорова");
        when(resultSet.getString("Specialization")).thenReturn("Хирург");
        when(resultSet.getString("RoomNumber")).thenReturn("202");
        when(resultSet.getString("Schedule")).thenReturn("Вт-Сб 10:00-16:00");
        when(resultSet.getString("Email")).thenReturn("anna@hospital.ru");

        Doctor doctor = doctorDao.getDoctorById(doctorId);

        assertNotNull(doctor);
        assertEquals(doctorId, doctor.getDoctorId());
        assertEquals("Анна", doctor.getFirstName());
        assertEquals("Сидорова", doctor.getLastName());

        verify(preparedStatement).setInt(1, doctorId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetDoctorById_NotFound_ReturnsNull() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Doctor doctor = doctorDao.getDoctorById(999);
        assertNull(doctor);
    }

    @Test
    void testAddDoctor_ExecutesInsert() throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Петр");
        doctor.setLastName("Сидоров");
        doctor.setSpecialization("Окулист");
        doctor.setRoomNumber("305");
        doctor.setSchedule("Пн-Ср 8:00-14:00");
        doctor.setEmail("petr@hospital.ru");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        doctorDao.addDoctor(doctor);

        verify(preparedStatement).setString(1, "Петр");
        verify(preparedStatement).setString(2, "Сидоров");
        verify(preparedStatement).setString(3, "Окулист");
        verify(preparedStatement).setString(4, "305");
        verify(preparedStatement).setString(5, "Пн-Ср 8:00-14:00");
        verify(preparedStatement).setString(6, "petr@hospital.ru");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateDoctor_ValidDoctor_ExecutesUpdate() throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(100);
        doctor.setFirstName("Иван");
        doctor.setLastName("Иванов");
        doctor.setSpecialization("Невролог");
        doctor.setRoomNumber("404");
        doctor.setSchedule("Чт-Вс 12:00-18:00");
        doctor.setEmail("ivan.updated@hospital.ru");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        doctorDao.updateDoctor(doctor);

        verify(preparedStatement).setString(1, "Иван");
        verify(preparedStatement).setString(2, "Иванов");
        verify(preparedStatement).setString(3, "Невролог");
        verify(preparedStatement).setString(4, "404");
        verify(preparedStatement).setString(5, "Чт-Вс 12:00-18:00");
        verify(preparedStatement).setString(6, "ivan.updated@hospital.ru");
        verify(preparedStatement).setInt(7, 100);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDeleteDoctor_ValidId_ExecutesDelete() throws SQLException {
        int doctorId = 100;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        doctorDao.deleteDoctor(doctorId);

        verify(preparedStatement).setInt(1, doctorId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSearchDoctors_WithSearchTerm_ReturnsList() throws SQLException {
        String searchTerm = "Анна";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Мокаем ВСЕ поля
        when(resultSet.getInt("DoctorID")).thenReturn(5);
        when(resultSet.getString("FirstName")).thenReturn("Анна");
        when(resultSet.getString("LastName")).thenReturn("Сидорова");
        when(resultSet.getString("Specialization")).thenReturn("Терапевт");
        when(resultSet.getString("RoomNumber")).thenReturn("101");
        when(resultSet.getString("Schedule")).thenReturn("Пн-Пт 9-18");
        when(resultSet.getString("Email")).thenReturn("anna@hospital.ru");

        List<Doctor> found = doctorDao.searchDoctors(searchTerm);

        assertFalse(found.isEmpty());
        verify(preparedStatement).setString(1, "%" + searchTerm + "%");
        verify(preparedStatement).setString(2, "%" + searchTerm + "%");
        verify(preparedStatement).setString(3, "%" + searchTerm + "%");
        verify(preparedStatement).setString(4, "%" + searchTerm + "%");
        verify(preparedStatement).executeQuery();
    }
}