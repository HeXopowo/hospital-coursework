package hospital.view.controller;
import hospital.AppointmentDao;
import hospital.daomodel.Appointment;
import hospital.daomodel.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
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

    // Список допустимых статусов
    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Запланирован", "Завершён", "Отменён", "Подтверждён"
    );

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user.getRole().equalsIgnoreCase("ADMIN")) {
            titleLabel.setText("Все приёмы");
        } else {
            titleLabel.setText("Мои приёмы");
        }
        try {
            appointments.setAll(appointmentDao.getAppointmentsByRole(user.getRole(), user.getRoleId()));
            appointmentTable.setItems(appointments);

            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                titleLabel.setText("Все приёмы");
                personColumn.setText("Пациент / Врач");
                enableEditing();
                doctorComboBox.setItems(FXCollections.observableArrayList(appointmentDao.getAllDoctors()));
                patientComboBox.setItems(FXCollections.observableArrayList(appointmentDao.getAllPatients()));

                // Настройка ComboBox для статуса
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
                if (user.getRole().equalsIgnoreCase("DOCTOR")) {
                    personColumn.setText("Пациент");
                } else if (user.getRole().equalsIgnoreCase("PATIENT")) {
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
                case "PATIENT" -> new SimpleStringProperty(appt.getDoctorName());
                case "DOCTOR" -> new SimpleStringProperty(appt.getPatientName());
                case "ADMIN" -> new SimpleStringProperty(appt.getPatientName() + " / " + appt.getDoctorName());
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

        // Автоматическая настройка ширины столбцов при изменении размера таблицы
        appointmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        searchAppointmentsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchAppointments(newValue);
        });
    }

    private void enableEditing() {
        appointmentTable.setEditable(true);

        // Используем ComboBoxTableCell для редактирования статуса в таблице
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(statusOptions));
        statusColumn.setOnEditCommit(event -> {
            Appointment appointment = event.getRowValue();
            appointment.setStatus(event.getNewValue());
            saveAppointment(appointment);
        });

        noteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        noteColumn.setOnEditCommit(event -> {
            Appointment appointment = event.getRowValue();
            appointment.setNote(event.getNewValue());
            saveAppointment(appointment);
        });
    }

    @FXML
    private void handleAddAppointment() {
        String doctorName = doctorComboBox.getValue();
        String patientName = patientComboBox.getValue();
        String date = datePicker.getValue() != null ? datePicker.getValue().toString() : null;
        String time = timeField.getText();
        String status = statusComboBox.getValue(); // Получаем значение из ComboBox
        String note = noteField.getText();

        if (doctorName == null || patientName == null || date == null || time == null || time.isBlank()) {
            showError("Пожалуйста, заполните все поля для назначения приёма.");
            return;
        }
        if (!time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            showError("Введите время в формате HH:mm");
            return;
        }
        if (status == null) {
            showError("Пожалуйста, выберите статус приёма.");
            return;
        }

        String dateTime = date + " " + time + ":00";
        try {
            appointmentDao.addAppointment(doctorName, patientName, dateTime, status, note);
            loadAppointmentsData();

            // Очистка полей после добавления
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при добавлении приёма: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите приём для редактирования.");
            return;
        }
        String doctorName = doctorComboBox.getValue();
        String patientName = patientComboBox.getValue();
        String date = datePicker.getValue() != null ? datePicker.getValue().toString() : null;
        String time = timeField.getText();
        String status = statusComboBox.getValue(); // Получаем значение из ComboBox
        String note = noteField.getText();

        if (doctorName == null || patientName == null || date == null || time == null || time.isBlank()) {
            showError("Пожалуйста, заполните все поля для редактирования приёма.");
            return;
        }
        if (!time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            showError("Введите время в формате HH:mm");
            return;
        }
        if (status == null) {
            showError("Пожалуйста, выберите статус приёма.");
            return;
        }

        String dateTime = date + " " + time + ":00";
        selected.setDoctorName(doctorName);
        selected.setPatientName(patientName);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        selected.setDateTime(java.time.LocalDateTime.parse(dateTime, formatter));
        selected.setStatus(status);
        selected.setNote(note);
        saveAppointment(selected);
        try {
            loadAppointmentsData();

            // Очистка полей после редактирования
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при обновлении таблицы после редактирования.");
        }
    }

    @FXML
    private void handleDeleteAppointment() {
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