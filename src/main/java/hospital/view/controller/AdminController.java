package hospital.view.controller;

import hospital.DoctorDao;
import hospital.PatientDao;
import hospital.PatientRegistrationDao;
import hospital.daomodel.Doctor;
import hospital.daomodel.Patient;
import hospital.daomodel.PatientRegistration;
import hospital.daomodel.User;
import hospital.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class AdminController {

    @FXML private TabPane adminTabPane;

    // Вкладка "Врачи"
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Integer> doctorIdColumn;
    @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> roomColumn;
    @FXML private TableColumn<Doctor, String> scheduleColumn;
    @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    @FXML private TextField searchDoctorsField;

    // Вкладка "Пациенты"
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> patientIdColumn;
    @FXML private TableColumn<Patient, String> patientFirstNameColumn;
    @FXML private TableColumn<Patient, String> patientLastNameColumn;
    @FXML private TableColumn<Patient, String> birthDateColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> patientEmailColumn;
    @FXML private TableColumn<Patient, String> snilsColumn;
    @FXML private TableColumn<Patient, String> policyColumn;
    @FXML private TableColumn<Patient, String> districtColumn;
    @FXML private TableColumn<Patient, String> addressColumn;
    @FXML private TextField searchPatientsField;

    // Вкладка "Учёт пациентов"
    @FXML private TableView<PatientRegistration> registrationsTable;
    @FXML private TableColumn<PatientRegistration, String> regPatientColumn;
    @FXML private TableColumn<PatientRegistration, String> regDoctorColumn;
    @FXML private TableColumn<PatientRegistration, LocalDate> regDateColumn;
    @FXML private TableColumn<PatientRegistration, String> regNotesColumn;

    // Вкладка "Архив"
    @FXML private TableView<Patient> archivedPatientsTable;
    @FXML private TableColumn<Patient, Integer> archPatIdColumn;
    @FXML private TableColumn<Patient, String> archPatFirstNameColumn;
    @FXML private TableColumn<Patient, String> archPatLastNameColumn;
    @FXML private TableColumn<Patient, String> archPatBirthDateColumn;
    @FXML private TableColumn<Patient, String> archPatPhoneColumn;
    @FXML private TableColumn<Patient, String> archPatEmailColumn;
    @FXML private TableColumn<Patient, String> archPatSnilsColumn;
    @FXML private TableColumn<Patient, String> archPatPolicyColumn;
    @FXML private TableColumn<Patient, String> archPatDistrictColumn;
    @FXML private TableColumn<Patient, String> archPatAddressColumn;
    @FXML private TableColumn<Patient, LocalDate> archPatDateColumn;
    @FXML private TableColumn<Patient, Void> archPatActionColumn;

    @FXML private TableView<Doctor> archivedDoctorsTable;
    @FXML private TableColumn<Doctor, Integer> archDocIdColumn;
    @FXML private TableColumn<Doctor, String> archDocFirstNameColumn;
    @FXML private TableColumn<Doctor, String> archDocLastNameColumn;
    @FXML private TableColumn<Doctor, String> archDocSpecializationColumn;
    @FXML private TableColumn<Doctor, String> archDocRoomColumn;
    @FXML private TableColumn<Doctor, String> archDocScheduleColumn;
    @FXML private TableColumn<Doctor, String> archDocEmailColumn;
    @FXML private TableColumn<Doctor, LocalDate> archDocDateColumn;
    @FXML private TableColumn<Doctor, Void> archDocActionColumn;

    // Данные
    private ObservableList<Doctor> doctorsData = FXCollections.observableArrayList();
    private ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    private ObservableList<PatientRegistration> registrationsData = FXCollections.observableArrayList();
    private ObservableList<Patient> archivedPatientsData = FXCollections.observableArrayList();
    private ObservableList<Doctor> archivedDoctorsData = FXCollections.observableArrayList();

    private User currentUser;
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();
    private PatientRegistrationDao registrationDao = new PatientRegistrationDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadData();
        loadArchivedData();
    }

    @FXML
    private void initialize() {
        setupDoctorsTable();
        setupPatientsTable();
        setupRegistrationsTable();
        setupArchivedTables();
        setupSearchFilters();
        loadData();
        loadArchivedData();
    }

    // Настройка таблиц
    private void setupDoctorsTable() {
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        doctorFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        doctorLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        doctorEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        doctorsTable.setItems(doctorsData);
    }

    private void setupPatientsTable() {
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        patientLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        patientEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        snilsColumn.setCellValueFactory(new PropertyValueFactory<>("snils"));
        policyColumn.setCellValueFactory(new PropertyValueFactory<>("policyOMS"));
        districtColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        patientsTable.setItems(patientsData);
    }

    private void setupRegistrationsTable() {
        regPatientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        regDoctorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        regNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        registrationsTable.setItems(registrationsData);
    }

    private void setupArchivedTables() {
        // Архив пациентов
        archPatIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        archPatFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        archPatLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        archPatBirthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        archPatPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        archPatEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        archPatSnilsColumn.setCellValueFactory(new PropertyValueFactory<>("snils"));
        archPatPolicyColumn.setCellValueFactory(new PropertyValueFactory<>("policyOMS"));
        archPatDistrictColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
        archPatAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        archPatDateColumn.setCellValueFactory(new PropertyValueFactory<>("archivedDate"));
        archPatActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button restoreBtn = new Button("Восстановить");
            {
                restoreBtn.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleRestorePatient(patient);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : restoreBtn);
            }
        });
        archivedPatientsTable.setItems(archivedPatientsData);

        // Архив врачей
        archDocIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        archDocFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        archDocLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        archDocSpecializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        archDocRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        archDocScheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        archDocEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        archDocDateColumn.setCellValueFactory(new PropertyValueFactory<>("archivedDate"));
        archDocActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button restoreBtn = new Button("Восстановить");
            {
                restoreBtn.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    handleRestoreDoctor(doctor);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : restoreBtn);
            }
        });
        archivedDoctorsTable.setItems(archivedDoctorsData);
    }

    // Загрузка данных
    private void loadData() {
        loadDoctorsData();
        loadPatientsData();
        loadRegistrationsData();
    }

    private void loadDoctorsData() {
        try {
            doctorsData.clear();
            doctorsData.addAll(doctorDao.getAllDoctors());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке врачей: " + e.getMessage());
        }
    }

    private void loadPatientsData() {
        try {
            patientsData.clear();
            patientsData.addAll(patientDao.getAllPatients());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке пациентов: " + e.getMessage());
        }
    }

    private void loadRegistrationsData() {
        try {
            registrationsData.clear();
            registrationsData.addAll(registrationDao.getAllActiveRegistrations());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки учётных записей.");
        }
    }

    private void loadArchivedData() {
        try {
            archivedPatientsData.clear();
            archivedPatientsData.addAll(patientDao.getArchivedPatients());
            archivedDoctorsData.clear();
            archivedDoctorsData.addAll(doctorDao.getArchivedDoctors());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки архива.");
        }
    }

    // Кнопки обновления
    @FXML
    private void handleRefreshDoctors() {
        loadDoctorsData();
    }

    @FXML
    private void handleRefreshPatients() {
        loadPatientsData();
    }

    @FXML
    private void handleRefreshArchive() {
        loadArchivedData();
    }

    @FXML
    private void handleRefreshRegistrations() {
        loadRegistrationsData();
    }

    // Обработчики других действий
    @FXML
    private void handleAllAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/appointments.fxml"));
            Parent root = loader.load();
            AppointmentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Приёмы");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно приёмов.");
        }
    }

    @FXML
    private void handleManageDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/doctor_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление врачами");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно управления врачами.");
        }
    }

    @FXML
    private void handleManagePatients() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/patient_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление пациентами");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно управления пациентами.");
        }
    }

    @FXML
    private void handleCleanupArchive() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение очистки");
        confirm.setHeaderText("Удалить записи из архива старше 50 лет?");
        confirm.setContentText("Это действие необратимо. Все архивированные данные, созданные более 50 лет назад, будут безвозвратно удалены.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    try (Connection conn = DatabaseConnection.getConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT cleanup_old_archives()");
                    }
                    showInfo("Архив очищен от записей старше 50 лет.");
                    loadArchivedData(); // обновить отображение
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Ошибка при очистке архива: " + e.getMessage());
                }
            }
        });
    }

    // Восстановление из архива
    private void handleRestorePatient(Patient patient) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение восстановления");
        confirm.setHeaderText("Восстановить пациента?");
        confirm.setContentText("Пациент " + patient.getFirstName() + " " + patient.getLastName() +
                " будет восстановлен из архива. Его связанные данные (приёмы, рецепты, медицинские записи, учёт) не восстановятся.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    patientDao.restorePatient(patient.getArchivedId());
                    loadArchivedData();
                    loadPatientsData();
                    showInfo("Пациент восстановлен.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Ошибка восстановления: " + e.getMessage());
                }
            }
        });
    }

    private void handleRestoreDoctor(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение восстановления");
        confirm.setHeaderText("Восстановить врача?");
        confirm.setContentText("Врач " + doctor.getFirstName() + " " + doctor.getLastName() +
                " будет восстановлен из архива. Его связанные данные (приёмы, рецепты, медицинские записи, учёт) не восстановятся.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    doctorDao.restoreDoctor(doctor.getArchivedId());
                    loadArchivedData();
                    loadDoctorsData();
                    showInfo("Врач восстановлен.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Ошибка восстановления: " + e.getMessage());
                }
            }
        });
    }

    // Поиск
    private void setupSearchFilters() {
        searchDoctorsField.textProperty().addListener((observable, oldValue, newValue) -> searchDoctors(newValue));
        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> searchPatients(newValue));
    }

    private void searchDoctors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadDoctorsData();
            return;
        }
        try {
            doctorsData.clear();
            doctorsData.addAll(doctorDao.searchDoctors(searchTerm.trim()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске врачей.");
        }
    }

    private void searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPatientsData();
            return;
        }
        try {
            patientsData.clear();
            patientsData.addAll(patientDao.searchPatients(searchTerm.trim()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске пациентов.");
        }
    }

    // вспомогательные методы
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}