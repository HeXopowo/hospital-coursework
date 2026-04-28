package hospital.view.controller;

import hospital.AppointmentDao;
import hospital.daomodel.Appointment;
import hospital.daomodel.User;
import hospital.util.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class AppointmentsController {
    @FXML private Label titleLabel;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> personColumn;
    @FXML private TableColumn<Appointment, String> roomColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, String> noteColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private ComboBox<String> patientComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField noteField;
    @FXML private Button editButton;
    @FXML private VBox adminForm;
    @FXML private Button deleteButton;
    @FXML private TextField searchAppointmentsField;

    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private User currentUser;
    private AppointmentDao appointmentDao = new AppointmentDao();

    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            Constants.STATUS_SCHEDULED,
            Constants.STATUS_COMPLETED,
            Constants.STATUS_CANCELLED,
            Constants.STATUS_CONFIRMED
    );
    private static final List<String> FORBIDDEN_PAST_STATUSES = Constants.FORBIDDEN_PAST_STATUSES;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = Constants.DATE_TIME_FORMATTER;
    private static final DateTimeFormatter TIME_FORMATTER = Constants.TIME_FORMATTER;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user.getRole().equalsIgnoreCase(Constants.ROLE_ADMIN)) {
            titleLabel.setText("Все приёмы");
        } else {
            titleLabel.setText("Мои приёмы");
        }
        try {
            appointments.setAll(appointmentDao.getAppointmentsByRole(user.getRole(), user.getRoleId()));
            appointmentTable.setItems(appointments);

            if (user.getRole().equalsIgnoreCase(Constants.ROLE_ADMIN)) {
                titleLabel.setText("Все приёмы");
                personColumn.setText("Пациент / Врач");
                setupAdminView();
                doctorComboBox.setItems(FXCollections.observableArrayList(appointmentDao.getAllDoctors()));
                patientComboBox.setItems(FXCollections.observableArrayList(appointmentDao.getAllPatients()));

                statusComboBox.setItems(statusOptions);
                statusComboBox.setPromptText("Выберите статус");

                editButton.setVisible(true);
                editButton.setManaged(true);
                adminForm.setVisible(true);
                adminForm.setManaged(true);
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
            } else {
                titleLabel.setText("Мои приёмы");
                adminForm.setVisible(false);
                adminForm.setManaged(false);
                deleteButton.setVisible(false);
                deleteButton.setManaged(false);
                if (user.getRole().equalsIgnoreCase(Constants.ROLE_DOCTOR)) {
                    personColumn.setText("Пациент");
                } else if (user.getRole().equalsIgnoreCase(Constants.ROLE_PATIENT)) {
                    personColumn.setText("Врач");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        personColumn.setCellValueFactory(cellData -> {
            Appointment appt = cellData.getValue();
            if (currentUser == null) return new SimpleStringProperty("");
            return switch (currentUser.getRole().toUpperCase()) {
                case Constants.ROLE_PATIENT -> new SimpleStringProperty(appt.getDoctorName());
                case Constants.ROLE_DOCTOR -> new SimpleStringProperty(appt.getPatientName());
                case Constants.ROLE_ADMIN -> new SimpleStringProperty(appt.getPatientName() + " / " + appt.getDoctorName());
                default -> new SimpleStringProperty("");
            };
        });
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateTime().toString())
        );
        appointmentTable.setItems(appointments);

        appointmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        searchAppointmentsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchAppointments(newValue);
        });

        // Автозаполнение формы при выборе строки
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && currentUser != null && currentUser.getRole().equalsIgnoreCase(Constants.ROLE_ADMIN)) {
                populateFormWithSelectedAppointment(newSelection);
            }
        });
    }

    private void setupAdminView() {
        appointmentTable.setEditable(false); // таблица только для чтения
    }

    private void populateFormWithSelectedAppointment(Appointment appointment) {
        if (appointment.getDoctorName() != null && doctorComboBox.getItems() != null) {
            doctorComboBox.setValue(appointment.getDoctorName());
        }
        if (appointment.getPatientName() != null && patientComboBox.getItems() != null) {
            patientComboBox.setValue(appointment.getPatientName());
        }
        if (appointment.getDateTime() != null) {
            datePicker.setValue(appointment.getDateTime().toLocalDate());
            timeField.setText(appointment.getDateTime().format(TIME_FORMATTER));
        }
        if (appointment.getStatus() != null && statusComboBox.getItems() != null) {
            statusComboBox.setValue(appointment.getStatus());
        }
        noteField.setText(appointment.getNote());
    }

    private boolean isPastDateForbidden(LocalDateTime dateTime, String status) {
        return dateTime.isBefore(LocalDateTime.now()) && FORBIDDEN_PAST_STATUSES.contains(status);
    }

    @FXML
    private void handleAddAppointment() {
        if (!validateAppointmentInput()) return;

        try {
            LocalDateTime appointmentDateTime = buildDateTime();
            if (!validateDateTimeWithStatus(appointmentDateTime)) return;

            String doctorName = doctorComboBox.getValue();
            String patientName = patientComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String status = statusComboBox.getValue();
            String note = noteField.getText();

            int doctorId = appointmentDao.getDoctorIdByName(doctorName);
            int patientId = appointmentDao.getPatientIdByName(patientName);

            if (!checkDoctorAvailability(doctorId, appointmentDateTime)) return;
            if (!checkPatientAvailability(patientId, appointmentDateTime)) return;
            if (!checkFourteenDaysInterval(doctorName, patientName, date, null)) return;

            saveAppointment(doctorName, patientName, appointmentDateTime, status, note);
        } catch (SQLException e) {
            showError("Ошибка БД: " + e.getMessage());
        } catch (Exception e) {
            showError("Ошибка: " + e.getMessage());
        }
    }

    private boolean validateAppointmentInput() {
        if (doctorComboBox.getValue() == null || patientComboBox.getValue() == null ||
                datePicker.getValue() == null || timeField.getText() == null || timeField.getText().isBlank()) {
            showError("Пожалуйста, заполните все поля для назначения приёма.");
            return false;
        }
        if (!timeField.getText().matches(Constants.TIME_PATTERN)) {
            showError("Введите время в формате HH:mm");
            return false;
        }
        if (statusComboBox.getValue() == null) {
            showError("Пожалуйста, выберите статус приёма.");
            return false;
        }
        return true;
    }

    private LocalDateTime buildDateTime() {
        String dateTimeStr = datePicker.getValue().toString() + " " + timeField.getText() + ":00";
        return LocalDateTime.parse(dateTimeStr, Constants.DATE_TIME_FORMATTER);
    }

    private boolean validateDateTimeWithStatus(LocalDateTime dateTime) {
        if (isPastDateForbidden(dateTime, statusComboBox.getValue())) {
            showError("Нельзя назначить приём со статусом \"" + statusComboBox.getValue() + "\" на прошедшую дату.");
            return false;
        }
        return true;
    }

    private boolean checkDoctorAvailability(int doctorId, LocalDateTime dateTime, Integer excludeId) throws SQLException {
        if (appointmentDao.hasDoctorAppointmentAtTime(doctorId, dateTime, excludeId)) {
            showError("Врач уже занят в это время.");
            return false;
        }
        return true;
    }

    private boolean checkDoctorAvailability(int doctorId, LocalDateTime dateTime) throws SQLException {
        return checkDoctorAvailability(doctorId, dateTime, null);
    }

    private boolean checkPatientAvailability(int patientId, LocalDateTime dateTime, Integer excludeId) throws SQLException {
        if (appointmentDao.hasPatientAppointmentAtTime(patientId, dateTime, excludeId)) {
            showError("У пациента уже есть приём в это время (у другого врача).");
            return false;
        }
        return true;
    }

    private boolean checkPatientAvailability(int patientId, LocalDateTime dateTime) throws SQLException {
        return checkPatientAvailability(patientId, dateTime, null);
    }

    private boolean checkFourteenDaysInterval(String doctorName, String patientName, LocalDate date, Integer excludeId) throws SQLException {
        if (appointmentDao.hasAppointmentWithinDays(doctorName, patientName, date, 13, excludeId)) {
            showError("Нельзя назначить приём чаще чем раз в 14 дней для одного пациента и врача (если пациент не состоит на учёте).");
            return false;
        }
        return true;
    }

    private void saveAppointment(String doctorName, String patientName, LocalDateTime dateTime, String status, String note) throws SQLException {
        String dateTimeStr = dateTime.format(Constants.DATE_TIME_FORMATTER);
        appointmentDao.addAppointment(doctorName, patientName, dateTimeStr, status, note);
        loadAppointmentsData();
        clearForm();
    }

    @FXML
    private void handleEditAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите приём для редактирования.");
            return;
        }

        if (!validateAppointmentInput()) return;

        try {
            LocalDateTime newDateTime = buildDateTime();
            if (!validateDateTimeWithStatus(newDateTime)) return;

            String doctorName = doctorComboBox.getValue();
            String patientName = patientComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String status = statusComboBox.getValue();
            String note = noteField.getText();

            int doctorId = appointmentDao.getDoctorIdByName(doctorName);
            int patientId = appointmentDao.getPatientIdByName(patientName);

            // Проверки с исключением текущего ID
            if (!checkDoctorAvailability(doctorId, newDateTime, selected.getAppointmentId())) return;
            if (!checkPatientAvailability(patientId, newDateTime, selected.getAppointmentId())) return;
            if (!checkFourteenDaysInterval(doctorName, patientName, date, selected.getAppointmentId())) return;

            // Обновление объекта и сохранение
            selected.setDoctorName(doctorName);
            selected.setPatientName(patientName);
            selected.setDateTime(newDateTime);
            selected.setStatus(status);
            selected.setNote(note);
            appointmentDao.updateAppointment(selected);
            loadAppointmentsData();
            clearForm();
        } catch (SQLException e) {
            showError("Ошибка при обновлении приёма: " + e.getMessage());
        } catch (Exception e) {
            showError("Ошибка при обработке данных: " + e.getMessage());
        }
    }



    @FXML
    public void handleDeleteAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите приём для удаления.");
            return;
        }
        try {
            appointmentDao.deleteAppointment(selected);
            loadAppointmentsData();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при удалении приёма");
        }
    }

    private void saveAppointment(Appointment appointment) {
        try {
            appointmentDao.updateAppointment(appointment);
            loadAppointmentsData();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при сохранении данных");
        }
    }

    private void loadAppointmentsData() {
        try {
            appointments.setAll(appointmentDao.getAppointmentsByRole(currentUser.getRole(), currentUser.getRoleId()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке приёмов.");
        }
    }

    private void clearForm() {
        doctorComboBox.setValue(null);
        patientComboBox.setValue(null);
        datePicker.setValue(null);
        timeField.clear();
        statusComboBox.setValue(null);
        noteField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void searchAppointments(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadAppointmentsData();
            return;
        }

        try {
            appointments.clear();
            List<Appointment> foundAppointments = appointmentDao.searchAppointments(
                    searchTerm.trim(),
                    currentUser.getRole(),
                    currentUser.getRoleId()
            );
            appointments.addAll(foundAppointments);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске приёмов.");
        }
    }
}