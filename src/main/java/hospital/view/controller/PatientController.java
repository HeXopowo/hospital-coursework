package hospital.view.controller;

import hospital.DoctorDao;
import hospital.PatientDao;
import hospital.daomodel.Doctor;
import hospital.daomodel.Patient;
import hospital.daomodel.User;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import hospital.daomodel.PatientRegistration;
import hospital.PatientRegistrationDao;

public class PatientController {
    @FXML private TabPane patientTabPane;

    // Таблица пациентов (Мои данные)
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> firstNameColumn;
    @FXML private TableColumn<Patient, String> lastNameColumn;
    @FXML private TableColumn<Patient, LocalDate> birthDateColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> snilsColumn;
    @FXML private TableColumn<Patient, String> policyColumn;
    @FXML private TableColumn<Patient, String> districtColumn;
    @FXML private TableColumn<Patient, String> addressColumn;

    // Таблица врачей
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Integer> doctorIdColumn;
    @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> roomColumn;
    @FXML private TableColumn<Doctor, String> scheduleColumn;
    @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    @FXML private TextField searchDoctorsField;

    // Таблица учёта пациента
    @FXML private TableView<PatientRegistration> registrationsTable;
    @FXML private TableColumn<PatientRegistration, String> regDoctorColumn;
    @FXML private TableColumn<PatientRegistration, LocalDate> regDateColumn;
    @FXML private TableColumn<PatientRegistration, String> regDiagnosisColumn;
    @FXML private TableColumn<PatientRegistration, String> regNotesColumn;

    private ObservableList<PatientRegistration> registrationsData = FXCollections.observableArrayList();
    private PatientRegistrationDao registrationDao = new PatientRegistrationDao();

    private ObservableList<Patient> patientData = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorsData = FXCollections.observableArrayList();
    private User currentUser;
    private PatientDao patientDao = new PatientDao();
    private DoctorDao doctorDao = new DoctorDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("PatientController: currentUser set to " + user.getUsername());
        loadPatientData();
        loadDoctorsData();
        loadRegistrationsData();
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы пациентов
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        snilsColumn.setCellValueFactory(new PropertyValueFactory<>("snils"));
        policyColumn.setCellValueFactory(new PropertyValueFactory<>("policyOMS"));
        districtColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        patientTable.setItems(patientData);

        // Инициализация таблицы врачей
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        doctorFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        doctorLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        doctorEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        doctorsTable.setItems(doctorsData);
        setupSearchFilters();

        // Таблица учёта
        regDoctorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        regDiagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        regNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        registrationsTable.setItems(registrationsData);
    }

    private void loadPatientData() {
        if (currentUser == null) {
            System.err.println("loadPatientData(): currentUser is null");
            return;
        }
        try {
            patientData.clear();
            if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
                Patient patient = patientDao.getPatientById(currentUser.getRoleId());
                if (patient != null) {
                    patientData.add(patient);
                }
            } else {
                // Для других ролей показываем всех пациентов
                patientData.addAll(patientDao.getAllPatients());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке данных пациентов.");
        }
    }

    private void loadDoctorsData() {
        try {
            doctorsData.clear();
            doctorsData.addAll(doctorDao.getAllDoctors());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке данных врачей.");
        }
    }

    @FXML
    private void handleMyAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/appointments.fxml"));
            Parent root = loader.load();
            AppointmentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Приёмы");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Не удалось открыть окно записей.");
        }
    }

    @FXML
    private void handleMyPrescriptions() {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/prescriptions.fxml"));
            Parent root = loader.load();
            PrescriptionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            Stage stage = new Stage();
            stage.setTitle("Рецепты");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно рецептов.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupSearchFilters() {
        // Поиск врачей
        searchDoctorsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDoctors(newValue);
        });
    }

    private void searchDoctors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadDoctorsData(); // Показать всех врачей
            return;
        }

        try {
            doctorsData.clear();
            List<Doctor> foundDoctors = doctorDao.searchDoctors(searchTerm.trim());
            doctorsData.addAll(foundDoctors);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске врачей.");
        }
    }

    @FXML
    private void handleMyMedicalRecords() {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/medical_records.fxml"));
            Parent root = loader.load();
            MedicalRecordsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Мои медицинские записи");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно медицинских записей.");
        }
    }

    private void loadRegistrationsData() {
        if (currentUser == null || !"PATIENT".equalsIgnoreCase(currentUser.getRole())) return;
        try {
            registrationsData.clear();
            registrationsData.addAll(registrationDao.getActiveRegistrationsByPatient(currentUser.getRoleId()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки учётных записей.");
        }
    }
}