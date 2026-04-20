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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import hospital.daomodel.PatientRegistration;
import hospital.PatientRegistrationDao;

public class AdminController {
    @FXML private TabPane adminTabPane;
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableView<Patient> patientsTable;

    // Таблица врачей
    @FXML private TableColumn<Doctor, Integer> doctorIdColumn;
    @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> roomColumn;
    @FXML private TableColumn<Doctor, String> scheduleColumn;
    @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    @FXML private TextField searchDoctorsField;

    // Таблица пациентов
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

    // Таблица учёта пациентов
    @FXML private TableView<PatientRegistration> registrationsTable;
    @FXML private TableColumn<PatientRegistration, String> regPatientColumn;
    @FXML private TableColumn<PatientRegistration, String> regDoctorColumn;
    @FXML private TableColumn<PatientRegistration, LocalDate> regDateColumn;
    @FXML private TableColumn<PatientRegistration, String> regDiagnosisColumn;
    @FXML private TableColumn<PatientRegistration, String> regNotesColumn;

    private ObservableList<PatientRegistration> registrationsData = FXCollections.observableArrayList();
    private PatientRegistrationDao registrationDao = new PatientRegistrationDao();

    private ObservableList<Doctor> doctorsData = FXCollections.observableArrayList();
    private ObservableList<Patient> patientsData = FXCollections.observableArrayList();

    private User currentUser;
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadData();
    }

    @FXML
    private void initialize() {
        setupDoctorsTable();
        setupPatientsTable();
        loadData();
        setupSearchFilters();
        setupRegistrationsTable();
    }

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
        regDiagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        regNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        registrationsTable.setItems(registrationsData);
    }

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

    @FXML
    private void handleAllAppointments() {
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
            stage.setScene(new javafx.scene.Scene(root));
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
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно управления пациентами.");
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
        searchDoctorsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDoctors(newValue);
        });

        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPatients(newValue);
        });
    }

    private void searchDoctors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadDoctorsData();
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

    private void searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPatientsData();
            return;
        }

        try {
            patientsData.clear();
            List<Patient> foundPatients = patientDao.searchPatients(searchTerm.trim());
            patientsData.addAll(foundPatients);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске пациентов.");
        }
    }
}