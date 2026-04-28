package hospital.view.controller;

import hospital.DoctorDao;
import hospital.PatientDao;
import hospital.PatientRegistrationDao;
import hospital.daomodel.Doctor;
import hospital.daomodel.Patient;
import hospital.daomodel.PatientRegistration;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    // Таблица пациентов (только те, у кого были приёмы у данного врача)
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

    // Таблица учёта пациентов
    @FXML private TableView<PatientRegistration> registrationsTable;
    @FXML private TableColumn<PatientRegistration, String> regPatientColumn;
    @FXML private TableColumn<PatientRegistration, LocalDate> regDateColumn;
    @FXML private TableColumn<PatientRegistration, String> regDiagnosisColumn;
    @FXML private TableColumn<PatientRegistration, String> regNotesColumn;
    @FXML private TableColumn<PatientRegistration, Void> regActionColumn;
    @FXML private TableColumn<PatientRegistration, Void> regEditColumn;

    private ObservableList<Doctor> doctorData = FXCollections.observableArrayList();
    private ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    private ObservableList<PatientRegistration> registrationsData = FXCollections.observableArrayList();

    private User currentUser;
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();
    private PatientRegistrationDao registrationDao = new PatientRegistrationDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("DoctorController: currentUser set to " + user.getUsername());
        loadDoctorData();
        loadPatientsData();
        loadRegistrationsData();
    }

    @FXML
    private void initialize() {
        // Таблица врачей
        idColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        doctorTable.setItems(doctorData);

        // Таблица пациентов
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

        // Поиск пациентов
        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPatients(newValue);
        });

        // Таблица учёта
        regPatientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        regDiagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        regNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Кнопка "Снять"
        regActionColumn.setCellFactory(col -> new TableCell<PatientRegistration, Void>() {
            private final Button btn = new Button("Снять");
            {
                btn.setOnAction(event -> {
                    PatientRegistration reg = getTableView().getItems().get(getIndex());
                    handleDeactivateRegistration(reg);
                });
                btn.setStyle("-fx-background-color: #ff8a80;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Кнопка "Редактировать"
        regEditColumn.setCellFactory(col -> new TableCell<PatientRegistration, Void>() {
            private final Button btn = new Button("✎");
            {
                btn.setOnAction(event -> {
                    PatientRegistration reg = getTableView().getItems().get(getIndex());
                    handleEditRegistration(reg);
                });
                btn.setStyle("-fx-background-color: #ffd966;");
                btn.setPrefWidth(60);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        registrationsTable.setItems(registrationsData);
    }

    private void loadDoctorData() {
        if (currentUser == null) return;
        try {
            doctorData.clear();
            if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
                Doctor doctor = doctorDao.getDoctorById(currentUser.getRoleId());
                if (doctor != null) doctorData.add(doctor);
            } else {
                doctorData.addAll(doctorDao.getAllDoctors());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки данных врачей.");
        }
    }

    private void loadPatientsData() {
        if (currentUser == null || !"DOCTOR".equalsIgnoreCase(currentUser.getRole())) return;
        try {
            patientsData.clear();
            List<Patient> patients = patientDao.getPatientsByDoctorId(currentUser.getRoleId());
            patientsData.addAll(patients);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки пациентов.");
        }
    }

    private void loadRegistrationsData() {
        if (currentUser == null || !"DOCTOR".equalsIgnoreCase(currentUser.getRole())) return;
        try {
            registrationsData.clear();
            registrationsData.addAll(registrationDao.getActiveRegistrationsByDoctor(currentUser.getRoleId()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка загрузки учётных записей.");
        }
    }

    @FXML
    private void handleMyAppointments() {
        openWindow("/hospital/view/fxml/appointments.fxml", "Приёмы");
    }

    @FXML
    private void handleMyPrescriptions() {
        openWindow("/hospital/view/fxml/prescriptions.fxml", "Рецепты");
    }

    @FXML
    private void handlePatientMedicalRecords() {
        openWindow("/hospital/view/fxml/medical_records.fxml", "Медицинские записи пациентов");
    }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AppointmentsController) {
                ((AppointmentsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof PrescriptionController) {
                ((PrescriptionController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof MedicalRecordsController) {
                ((MedicalRecordsController) controller).setCurrentUser(currentUser);
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно.");
        }
    }

    private void searchPatients(String searchTerm) {
        if (currentUser == null || !"DOCTOR".equalsIgnoreCase(currentUser.getRole())) return;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPatientsData();
            return;
        }
        try {
            patientsData.clear();
            List<Patient> found = patientDao.searchPatientsByDoctor(currentUser.getRoleId(), searchTerm.trim());
            patientsData.addAll(found);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка поиска пациентов.");
        }
    }

    // Учёт пациентов

    @FXML
    private void handleAddRegistration() {
        if (patientsData.isEmpty()) {
            showAlert("Нет пациентов", "У вас нет пациентов для постановки на учёт.");
            return;
        }

        Dialog<PatientRegistration> dialog = createRegistrationDialog(null);
        Optional<PatientRegistration> result = dialog.showAndWait();
        result.ifPresent(reg -> {
            try {
                registrationDao.addRegistration(reg);
                loadRegistrationsData();
                showInfo("Пациент поставлен на учёт.");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Ошибка при добавлении: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditRegistration() {
        PatientRegistration selected = registrationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Не выбрана запись", "Пожалуйста, выберите запись для редактирования.");
            return;
        }
        handleEditRegistration(selected);
    }

    private void handleEditRegistration(PatientRegistration selected) {
        Dialog<PatientRegistration> dialog = createRegistrationDialog(selected);
        dialog.setTitle("Редактирование учётной записи");
        Optional<PatientRegistration> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            try {
                registrationDao.updateRegistration(updated);
                loadRegistrationsData();
                showInfo("Запись успешно обновлена.");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Ошибка при обновлении: " + e.getMessage());
            }
        });
    }

    // Создаёт диалог для добавления или редактирования учётной записи.
    // @param existing если null – создаётся новая запись, иначе редактируется существующая.
    private Dialog<PatientRegistration> createRegistrationDialog(PatientRegistration existing) {
        Dialog<PatientRegistration> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Постановка на учёт" : "Редактирование учётной записи");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResizable(true);

        ComboBox<Patient> patientCombo = new ComboBox<>();
        if (existing == null) {
            patientCombo.setItems(patientsData);
            patientCombo.setConverter(new javafx.util.StringConverter<Patient>() {
                @Override
                public String toString(Patient p) {
                    return p == null ? "" : p.getFirstName() + " " + p.getLastName();
                }
                @Override
                public Patient fromString(String s) { return null; }
            });
            patientCombo.setPromptText("Выберите пациента");
        } else {
            Patient dummy = new Patient();
            String[] nameParts = existing.getPatientName().split(" ", 2);
            dummy.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
            dummy.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            patientCombo.setValue(dummy);
            patientCombo.setDisable(true);
        }

        DatePicker regDatePicker = new DatePicker(existing != null ? existing.getRegistrationDate() : LocalDate.now());
        TextField diagnosisField = new TextField(existing != null ? existing.getDiagnosis() : "");
        diagnosisField.setPromptText("Диагноз (обязательно)");

        TextArea notesArea = new TextArea(existing != null ? existing.getNotes() : "");
        notesArea.setPromptText("Примечания");
        notesArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.addRow(0, new Label("Пациент:"), patientCombo);
        grid.addRow(1, new Label("Дата постановки:"), regDatePicker);
        grid.addRow(2, new Label("Диагноз:"), diagnosisField);
        grid.addRow(3, new Label("Примечания:"), notesArea);

        GridPane.setHgrow(patientCombo, Priority.ALWAYS);
        GridPane.setHgrow(regDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(diagnosisField, Priority.ALWAYS);
        GridPane.setHgrow(notesArea, Priority.ALWAYS);
        patientCombo.setMaxWidth(Double.MAX_VALUE);
        regDatePicker.setMaxWidth(Double.MAX_VALUE);
        diagnosisField.setMaxWidth(Double.MAX_VALUE);
        notesArea.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 400);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (existing == null) {
                    Patient selectedPatient = patientCombo.getValue();
                    if (selectedPatient == null) {
                        showAlert("Ошибка", "Выберите пациента.");
                        return null;
                    }
                    if (diagnosisField.getText().trim().isEmpty()) {
                        showAlert("Ошибка", "Введите диагноз.");
                        return null;
                    }
                    PatientRegistration reg = new PatientRegistration();
                    reg.setPatientId(selectedPatient.getPatientId());
                    reg.setPatientName(selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
                    reg.setDoctorId(currentUser.getRoleId());
                    reg.setRegistrationDate(regDatePicker.getValue());
                    reg.setDiagnosis(diagnosisField.getText().trim());
                    reg.setNotes(notesArea.getText().trim());
                    reg.setActive(true);
                    return reg;
                } else {
                    if (diagnosisField.getText().trim().isEmpty()) {
                        showAlert("Ошибка", "Введите диагноз.");
                        return null;
                    }
                    existing.setRegistrationDate(regDatePicker.getValue());
                    existing.setDiagnosis(diagnosisField.getText().trim());
                    existing.setNotes(notesArea.getText().trim());
                    return existing;
                }
            }
            return null;
        });
        return dialog;
    }

    private void handleDeactivateRegistration(PatientRegistration reg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Снятие с учёта");
        alert.setHeaderText("Снять пациента с учёта?");
        alert.setContentText("Пациент: " + reg.getPatientName() + "\nДиагноз: " + reg.getDiagnosis());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                registrationDao.deactivateRegistration(reg.getRegistrationId());
                loadRegistrationsData();
                showInfo("Пациент снят с учёта.");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Ошибка при снятии: " + e.getMessage());
            }
        }
    }

    // Вспомогательные методы для сообщений
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void showAlert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}