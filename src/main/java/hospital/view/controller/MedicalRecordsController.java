package hospital.view.controller;

import hospital.MedicalRecordDao;
import hospital.PatientDao;
import hospital.daomodel.MedicalRecord;
import hospital.daomodel.Patient;
import hospital.daomodel.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MedicalRecordsController {
    @FXML private Label titleLabel;
    @FXML private TableView<MedicalRecord> recordsTable;
    @FXML private TableColumn<MedicalRecord, String> dateColumn;
    @FXML private TableColumn<MedicalRecord, String> personColumn;
    @FXML private TableColumn<MedicalRecord, String> diseaseHistoryColumn;
    @FXML private TableColumn<MedicalRecord, String> diagnosisColumn;
    @FXML private TableColumn<MedicalRecord, String> recommendationsColumn;
    @FXML private TextField searchField;
    @FXML private HBox doctorButtonsPanel;
    @FXML private Button addRecordButton;
    @FXML private Button editRecordButton;
    @FXML private Button deleteRecordButton;

    private ObservableList<MedicalRecord> recordsData = FXCollections.observableArrayList();
    private ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    private User currentUser;
    private MedicalRecordDao medicalRecordDao = new MedicalRecordDao();
    private PatientDao patientDao = new PatientDao();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadMedicalRecords();
        setupRoleSpecificUI();
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            if (record.getCreationDate() != null) {
                return new SimpleStringProperty(record.getCreationDate().toString());
            } else {
                return new SimpleStringProperty("Нет даты");
            }
        });

        personColumn.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
                return new SimpleStringProperty(record.getDoctorName() != null ?
                        record.getDoctorName() : "Врач не найден");
            } else {
                return new SimpleStringProperty(record.getPatientName() != null ?
                        record.getPatientName() : "Пациент не найден");
            }
        });

        diseaseHistoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().getDiseaseHistory())));
        diagnosisColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().getDiagnosis())));
        recommendationsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().getRecommendations())));

        // Установка фабрики для переноса текста в ячейках
        setupCellWrap(diseaseHistoryColumn);
        setupCellWrap(diagnosisColumn);
        setupCellWrap(recommendationsColumn);

        recordsTable.setItems(recordsData);
    }

    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    private void setupCellWrap(TableColumn<MedicalRecord, String> column) {
        column.setCellFactory(tc -> {
            TableCell<MedicalRecord, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(column.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    private void setupRoleSpecificUI() {
        if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
            titleLabel.setText("Мои медицинские записи");
            personColumn.setText("Врач");
            searchField.setPromptText("Поиск по ФИО врача...");
            doctorButtonsPanel.setVisible(false);
            doctorButtonsPanel.setManaged(false);
        } else if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
            titleLabel.setText("Медицинские записи пациентов");
            personColumn.setText("Пациент");
            searchField.setPromptText("Поиск по ФИО пациента...");
            doctorButtonsPanel.setVisible(true);
            doctorButtonsPanel.setManaged(true);
            loadPatientsForDoctor();
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchMedicalRecords(newValue);
        });
    }

    private void loadMedicalRecords() {
        try {
            recordsData.clear();
            List<MedicalRecord> records;

            if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
                records = medicalRecordDao.getMedicalRecordsByPatient(currentUser.getRoleId());
            } else if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
                records = medicalRecordDao.getMedicalRecordsByDoctor(currentUser.getRoleId());
            } else {
                showError("Доступ запрещен. Только пациенты и врачи могут просматривать медицинские записи.");
                return;
            }

            recordsData.addAll(records);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке медицинских записей: " + e.getMessage());
        }
    }

    private void loadPatientsForDoctor() {
        try {
            patientsData.clear();
            // Получаем пациентов, которые были у врача на приемах
            List<Patient> patients = patientDao.getPatientsByDoctorId(currentUser.getRoleId());
            patientsData.addAll(patients);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при загрузке пациентов: " + e.getMessage());
        }
    }

    private void searchMedicalRecords(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadMedicalRecords();
            return;
        }

        try {
            recordsData.clear();
            List<MedicalRecord> foundRecords;

            if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
                foundRecords = medicalRecordDao.searchMedicalRecordsByPatient(
                        currentUser.getRoleId(), searchTerm.trim());
            } else if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
                foundRecords = medicalRecordDao.searchMedicalRecordsByDoctor(
                        currentUser.getRoleId(), searchTerm.trim());
            } else {
                return;
            }

            recordsData.addAll(foundRecords);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске медицинских записей.");
        }
    }

    @FXML
    private void handleAddRecord() {
        if (patientsData.isEmpty()) {
            showAlert("Нет пациентов", "У вас нет пациентов для создания медицинской записи.");
            return;
        }

        Dialog<MedicalRecord> dialog = createMedicalRecordDialog(null);
        dialog.setTitle("Добавление медицинской записи");
        Optional<MedicalRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            try {
                // Устанавливаем doctorId текущего врача
                record.setDoctorId(currentUser.getRoleId());
                medicalRecordDao.addMedicalRecord(record);
                loadMedicalRecords();
                showSuccess("Медицинская запись успешно добавлена.");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Ошибка при добавлении медицинской записи: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditRecord() {
        MedicalRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Не выбрана запись", "Пожалуйста, выберите запись для редактирования.");
            return;
        }

        // Проверяем, что запись принадлежит текущему врачу
        if (selected.getDoctorId() != currentUser.getRoleId()) {
            showAlert("Ошибка доступа", "Вы можете редактировать только свои записи.");
            return;
        }

        Dialog<MedicalRecord> dialog = createMedicalRecordDialog(selected);
        dialog.setTitle("Редактирование медицинской записи");
        Optional<MedicalRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            try {
                record.setRecordId(selected.getRecordId());
                record.setDoctorId(currentUser.getRoleId());
                record.setPatientId(selected.getPatientId());
                medicalRecordDao.updateMedicalRecord(record);
                loadMedicalRecords();
                showSuccess("Медицинская запись успешно обновлена.");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Ошибка при обновлении медицинской записи: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteRecord() {
        MedicalRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Не выбрана запись", "Пожалуйста, выберите запись для удаления.");
            return;
        }

        // Проверяем, что запись принадлежит текущему врачу
        if (selected.getDoctorId() != currentUser.getRoleId()) {
            showAlert("Ошибка доступа", "Вы можете удалять только свои записи.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить медицинскую запись?");
        alert.setContentText("Вы уверены, что хотите удалить запись от " +
                selected.getCreationDate() + " для пациента " + selected.getPatientName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    medicalRecordDao.deleteMedicalRecord(selected.getRecordId());
                    loadMedicalRecords();
                    showSuccess("Медицинская запись успешно удалена.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Ошибка при удалении медицинской записи: " + e.getMessage());
                }
            }
        });
    }

    private Dialog<MedicalRecord> createMedicalRecordDialog(MedicalRecord record) {
        Dialog<MedicalRecord> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Поля для ввода
        ComboBox<Patient> patientComboBox = new ComboBox<>();
        patientComboBox.setItems(patientsData);
        patientComboBox.setConverter(new javafx.util.StringConverter<Patient>() {
            @Override
            public String toString(Patient patient) {
                if (patient == null) {
                    return "Пациент не найден";
                }
                return patient.getFirstName() + " " + patient.getLastName();
            }

            @Override
            public Patient fromString(String string) {
                return null; // Не требуется
            }
        });

        TextArea diseaseHistoryArea = new TextArea();
        TextArea lifeHistoryArea = new TextArea();
        TextArea objectiveStatusArea = new TextArea();
        TextArea diagnosisArea = new TextArea();
        TextArea recommendationsArea = new TextArea();

        // Устанавливаем подсказки
        diseaseHistoryArea.setPromptText("Введите анамнез заболевания (жалобы, симптомы, история развития)");
        lifeHistoryArea.setPromptText("Введите историю жизни (наследственность, условия жизни, вредные привычки)");
        objectiveStatusArea.setPromptText("Введите объективный статус (результаты осмотра, обследования)");
        diagnosisArea.setPromptText("Введите диагноз");
        recommendationsArea.setPromptText("Введите рекомендации по лечению и дальнейшему наблюдению");

        // Устанавливаем размеры
        diseaseHistoryArea.setPrefRowCount(3);
        lifeHistoryArea.setPrefRowCount(2);
        objectiveStatusArea.setPrefRowCount(2);
        diagnosisArea.setPrefRowCount(2);
        recommendationsArea.setPrefRowCount(3);

        if (record != null) {
            // Редактирование существующей записи
            // Находим пациента по patientId
            Patient patient = patientsData.stream()
                    .filter(p -> p.getPatientId() == record.getPatientId())
                    .findFirst()
                    .orElse(null);

            if (patient != null) {
                patientComboBox.setValue(patient);
            } else {
                // Если пациент не найден в списке, создаем временного пациента для отображения
                Patient tempPatient = new Patient();
                tempPatient.setPatientId(record.getPatientId());
                tempPatient.setFirstName("Пациент");
                tempPatient.setLastName("(ID: " + record.getPatientId() + ")");
                patientComboBox.setValue(tempPatient);
            }

            patientComboBox.setDisable(true); // При редактировании нельзя менять пациента

            // Заполняем поля данными из записи
            diseaseHistoryArea.setText(record.getDiseaseHistory());
            lifeHistoryArea.setText(record.getLifeHistory());
            objectiveStatusArea.setText(record.getObjectiveStatus());
            diagnosisArea.setText(record.getDiagnosis());
            recommendationsArea.setText(record.getRecommendations());
        }

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("Пациент:"), patientComboBox);
        grid.addRow(1, new Label("Анамнез заболевания:"), diseaseHistoryArea);
        grid.addRow(2, new Label("История жизни:"), lifeHistoryArea);
        grid.addRow(3, new Label("Объективный статус:"), objectiveStatusArea);
        grid.addRow(4, new Label("Диагноз:"), diagnosisArea);
        grid.addRow(5, new Label("Рекомендации:"), recommendationsArea);

        // Делаем диалог больше для удобства
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.getDialogPane().setContent(grid);

        // Валидация
        if (record == null) {
            // Для новой записи
            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    if (patientComboBox.getValue() == null) {
                        showAlert("Ошибка", "Выберите пациента.");
                        return null;
                    }
                    if (diagnosisArea.getText().trim().isEmpty() ||
                            recommendationsArea.getText().trim().isEmpty()) {
                        showAlert("Ошибка", "Заполните обязательные поля: диагноз и рекомендации.");
                        return null;
                    }
                    MedicalRecord newRecord = new MedicalRecord();
                    newRecord.setPatientId(patientComboBox.getValue().getPatientId());
                    newRecord.setDiseaseHistory(diseaseHistoryArea.getText().trim());
                    newRecord.setLifeHistory(lifeHistoryArea.getText().trim());
                    newRecord.setObjectiveStatus(objectiveStatusArea.getText().trim());
                    newRecord.setDiagnosis(diagnosisArea.getText().trim());
                    newRecord.setRecommendations(recommendationsArea.getText().trim());
                    return newRecord;
                }
                return null;
            });
        } else {
            // Для редактирования существующей записи
            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    if (diagnosisArea.getText().trim().isEmpty() ||
                            recommendationsArea.getText().trim().isEmpty()) {
                        showAlert("Ошибка", "Заполните обязательные поля: диагноз и рекомендации.");
                        return null;
                    }
                    record.setDiseaseHistory(diseaseHistoryArea.getText().trim());
                    record.setLifeHistory(lifeHistoryArea.getText().trim());
                    record.setObjectiveStatus(objectiveStatusArea.getText().trim());
                    record.setDiagnosis(diagnosisArea.getText().trim());
                    record.setRecommendations(recommendationsArea.getText().trim());
                    return record;
                }
                return null;
            });
        }

        return dialog;
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}