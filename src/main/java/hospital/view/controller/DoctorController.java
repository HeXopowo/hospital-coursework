package hospital.view.controller;

import hospital.DoctorDao;
import hospital.PatientDao;
import hospital.daomodel.Doctor;
import hospital.daomodel.Patient;
import hospital.daomodel.User;
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
import java.util.List;

public class DoctorController {
    @FXML private TabPane doctorTabPane;

    // Таблица врачей (Мои данные)
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, Integer> idColumn;
    @FXML private TableColumn<Doctor, String> firstNameColumn;
    @FXML private TableColumn<Doctor, String> lastNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> roomColumn;
    @FXML private TableColumn<Doctor, String> scheduleColumn;
    @FXML private TableColumn<Doctor, String> emailColumn;

    // Таблица пациентов (Мои пациенты)
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

    private ObservableList<Doctor> doctorData = FXCollections.observableArrayList();
    private ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    private User currentUser;
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("DoctorController: currentUser set to " + user.getUsername());
        loadDoctorData();
        loadPatientsData();
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы врачей
        idColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        doctorTable.setItems(doctorData);

        // Инициализация таблицы пациентов
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
        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPatients(newValue);
        });
    }

    private void loadDoctorData() {
        if (currentUser == null) {
            System.err.println("loadDoctorData(): currentUser is null");
            return;
        }
        try {
            doctorData.clear();
            if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
                Doctor doctor = doctorDao.getDoctorById(currentUser.getRoleId());
                if (doctor != null) {
                    doctorData.add(doctor);
                }
            } else {
                // Для других ролей показываем всех врачей
                doctorData.addAll(doctorDao.getAllDoctors());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке данных врачей.");
        }
    }

    private void loadPatientsData() {
        if (currentUser == null) return;
        try {
            patientsData.clear();
            if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
                // Для врача показываем всех пациентов
                patientsData.addAll(patientDao.getAllPatients());
            } else {
                // Для других ролей показываем всех пациентов
                patientsData.addAll(patientDao.getAllPatients());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке данных пациентов.");
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

    private void searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPatientsData(); // Показать всех пациентов
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

    @FXML
    private void handlePatientMedicalRecords() {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/medical_records.fxml"));
            Parent root = loader.load();
            MedicalRecordsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Медицинские записи пациентов");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно медицинских записей.");
        }
    }
}