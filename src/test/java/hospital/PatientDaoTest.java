package hospital;

import hospital.daomodel.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PatientDaoTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private PatientDao patientDao;
    private MockedStatic<DatabaseConnection> mockedStatic;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        // Мокаем статический метод DatabaseConnection.getConnection()
        mockedStatic = mockStatic(DatabaseConnection.class);
        mockedStatic.when(DatabaseConnection::getConnection).thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        patientDao = new PatientDao();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void testGetPatientById_ValidId_ReturnsPatient() throws SQLException {
        int patientId = 100;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Мокаем ВСЕ поля, которые читаются в extractPatientFromResultSet
        when(resultSet.getInt("PatientID")).thenReturn(patientId);
        when(resultSet.getString("FirstName")).thenReturn("Иван");
        when(resultSet.getString("LastName")).thenReturn("Петров");
        when(resultSet.getDate("BirthDate")).thenReturn(Date.valueOf(LocalDate.of(1980, 1, 1)));
        when(resultSet.getString("PhoneNumber")).thenReturn("+79161234567");
        when(resultSet.getString("Email")).thenReturn("ivan@test.ru");
        when(resultSet.getString("SNILS")).thenReturn("123-456-789 00");
        when(resultSet.getString("PolicyOMS")).thenReturn("1234567890123456");
        when(resultSet.getInt("District")).thenReturn(5);
        when(resultSet.getString("Address")).thenReturn("ул. Ленина 10");

        Patient patient = patientDao.getPatientById(patientId);

        assertNotNull(patient);
        assertEquals(patientId, patient.getPatientId());
        assertEquals("Иван", patient.getFirstName());
        assertEquals("Петров", patient.getLastName());
        assertEquals("ivan@test.ru", patient.getEmail());

        verify(preparedStatement).setInt(1, patientId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetPatientById_NotFound_ReturnsNull() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Patient patient = patientDao.getPatientById(999);
        assertNull(patient);
    }

    @Test
    void testAddPatient_ValidPatient_ExecutesInsertAndSetsId() throws SQLException {
        Patient patient = new Patient();
        patient.setFirstName("Анна");
        patient.setLastName("Сидорова");
        patient.setBirthDate(LocalDate.of(1995, 6, 20));
        patient.setPhoneNumber("+79169998877");
        patient.setEmail("anna@test.ru");
        patient.setSnils("987-654-321 00");
        patient.setPolicyOMS("9876543210987654");
        patient.setDistrict(3);
        patient.setAddress("ул. Пушкина 5");

        ResultSet generatedKeys = mock(ResultSet.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(200);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        patientDao.addPatient(patient);

        verify(preparedStatement).setString(1, "Анна");
        verify(preparedStatement).setString(2, "Сидорова");
        verify(preparedStatement).setDate(eq(3), any(Date.class));
        verify(preparedStatement).setString(4, "+79169998877");
        verify(preparedStatement).setString(5, "anna@test.ru");
        verify(preparedStatement).setString(6, "987-654-321 00");
        verify(preparedStatement).setString(7, "9876543210987654");
        verify(preparedStatement).setInt(8, 3);
        verify(preparedStatement).setString(9, "ул. Пушкина 5");
        verify(preparedStatement).executeUpdate();
        assertEquals(200, patient.getPatientId());
    }

    @Test
    void testUpdatePatient_ValidPatient_ExecutesUpdate() throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(100);
        patient.setFirstName("Иван");
        patient.setLastName("Иванов");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setPhoneNumber("+79161112233");
        patient.setEmail("ivan_new@test.ru");
        patient.setSnils("123-456-789 00");
        patient.setPolicyOMS("1234567890123456");
        patient.setDistrict(2);
        patient.setAddress("ул. Ленина");

        when(preparedStatement.executeUpdate()).thenReturn(1);

        patientDao.updatePatient(patient);

        verify(preparedStatement).setString(1, "Иван");
        verify(preparedStatement).setString(2, "Иванов");
        verify(preparedStatement).setInt(10, 100);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDeletePatient_ValidId_ExecutesDelete() throws SQLException {
        int patientId = 100;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        patientDao.deletePatient(patientId);

        verify(preparedStatement).setInt(1, patientId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSearchPatients_WithSearchTerm_ReturnsList() throws SQLException {
        String searchTerm = "Анна";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Мокаем ВСЕ поля – это критически важно, иначе валидация модели выбросит исключение
        when(resultSet.getInt("PatientID")).thenReturn(5);
        when(resultSet.getString("FirstName")).thenReturn("Анна");
        when(resultSet.getString("LastName")).thenReturn("Сидорова");
        when(resultSet.getDate("BirthDate")).thenReturn(Date.valueOf(LocalDate.of(1990, 1, 1)));
        when(resultSet.getString("PhoneNumber")).thenReturn("+79161234567");
        when(resultSet.getString("Email")).thenReturn("anna@test.ru");
        when(resultSet.getString("SNILS")).thenReturn("123-456-789 00");
        when(resultSet.getString("PolicyOMS")).thenReturn("1234567890123456");
        when(resultSet.getInt("District")).thenReturn(1);
        when(resultSet.getString("Address")).thenReturn("ул. Тестовая");

        List<Patient> found = patientDao.searchPatients(searchTerm);

        assertFalse(found.isEmpty());
        verify(preparedStatement).setString(1, "%" + searchTerm + "%");
        verify(preparedStatement).setString(2, "%" + searchTerm + "%");
        verify(preparedStatement).setString(3, "%" + searchTerm + "%");
        verify(preparedStatement).setString(4, "%" + searchTerm + "%");
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testGetPatientsByDoctorId_ValidDoctorId_ReturnsList() throws SQLException {
        int doctorId = 10;
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Полный мок ResultSet – аналогично
        when(resultSet.getInt("PatientID")).thenReturn(7);
        when(resultSet.getString("FirstName")).thenReturn("Ольга");
        when(resultSet.getString("LastName")).thenReturn("Кузнецова");
        when(resultSet.getDate("BirthDate")).thenReturn(Date.valueOf(LocalDate.of(1985, 5, 10)));
        when(resultSet.getString("PhoneNumber")).thenReturn("+79169998877");
        when(resultSet.getString("Email")).thenReturn("olga@test.ru");
        when(resultSet.getString("SNILS")).thenReturn("987-654-321 00");
        when(resultSet.getString("PolicyOMS")).thenReturn("9876543210987654");
        when(resultSet.getInt("District")).thenReturn(2);
        when(resultSet.getString("Address")).thenReturn("ул. Дружбы");

        List<Patient> patients = patientDao.getPatientsByDoctorId(doctorId);

        assertNotNull(patients);
        assertFalse(patients.isEmpty());
        verify(preparedStatement).setInt(1, doctorId);
        verify(preparedStatement).executeQuery();
    }
}