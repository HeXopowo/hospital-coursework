package hospital;

import hospital.daomodel.Appointment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentDaoTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private AppointmentDao appointmentDao;
    private MockedStatic<DatabaseConnection> mockedStatic;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        mockedStatic = mockStatic(DatabaseConnection.class);
        mockedStatic.when(DatabaseConnection::getConnection).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        appointmentDao = new AppointmentDao();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void testSearchAppointments_ForDoctor_FiltersByPatientName() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("AppointmentID")).thenReturn(100);
        when(resultSet.getTimestamp("AppointmentDateTime")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("DoctorName")).thenReturn("Анна Сидорова");
        when(resultSet.getString("PatientName")).thenReturn("Иванов Петр");
        when(resultSet.getString("Status")).thenReturn("Запланирован");
        when(resultSet.getString("MedicalRecordNote")).thenReturn("");
        when(resultSet.getString("RoomNumber")).thenReturn("101");

        List<Appointment> result = appointmentDao.searchAppointments("Петр", "DOCTOR", 1);

        assertFalse(result.isEmpty());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "%Петр%");
        verify(preparedStatement).setString(3, "%Петр%");
        verify(preparedStatement).setString(4, "%Петр%");
    }

    @Test
    void testSearchAppointments_ForPatient_FiltersByDoctorName() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("AppointmentID")).thenReturn(101);
        when(resultSet.getTimestamp("AppointmentDateTime")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("DoctorName")).thenReturn("Иван Петров");
        when(resultSet.getString("PatientName")).thenReturn("Анна Сидорова");
        when(resultSet.getString("Status")).thenReturn("Завершён");
        when(resultSet.getString("MedicalRecordNote")).thenReturn("Осмотр");
        when(resultSet.getString("RoomNumber")).thenReturn("202");

        List<Appointment> result = appointmentDao.searchAppointments("Иван", "PATIENT", 1);

        assertFalse(result.isEmpty());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "%Иван%");
        verify(preparedStatement).setString(3, "%Иван%");
        verify(preparedStatement).setString(4, "%Иван%");
    }

    @Test
    void testSearchAppointments_EmptyTerm_ReturnsFullList() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // две записи
        when(resultSet.getInt("AppointmentID")).thenReturn(1, 2);
        when(resultSet.getTimestamp("AppointmentDateTime")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("DoctorName")).thenReturn("Доктор А", "Доктор Б");
        when(resultSet.getString("PatientName")).thenReturn("Пациент А", "Пациент Б");
        when(resultSet.getString("Status")).thenReturn("Запланирован", "Завершён");
        when(resultSet.getString("MedicalRecordNote")).thenReturn("", "");
        when(resultSet.getString("RoomNumber")).thenReturn("101", "102");

        List<Appointment> result = appointmentDao.searchAppointments("", "ADMIN", 0);

        assertEquals(2, result.size());
    }
}