package hospital.view.controller;

import hospital.PatientDao;
import hospital.daomodel.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PatManController {
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Number> idColumn;
    @FXML private TableColumn<Patient, String> firstNameColumn;
    @FXML private TableColumn<Patient, String> lastNameColumn;
    @FXML private TableColumn<Patient, String> birthDateColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;
    @FXML private TableColumn<Patient, String> snilsColumn;
    @FXML private TableColumn<Patient, String> policyColumn;
    @FXML private TableColumn<Patient, Number> districtColumn;
    @FXML private TableColumn<Patient, String> addressColumn;
    @FXML private TextField searchPatientsField; // Добавлено поле поиска

    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private final PatientDao patientDao = new PatientDao();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getPatientId()));
        firstNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        birthDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBirthDate() != null ? data.getValue().getBirthDate().toString() : ""));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        snilsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSnils()));
        policyColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPolicyOMS()));
        districtColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDistrict()));
        addressColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAddress()));

        patientTable.setItems(patientList);
        loadPatientsFromDB();

        // Добавлен обработчик поиска
        setupSearchFilter();
    }

    private void setupSearchFilter() {
        searchPatientsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPatients(newValue);
        });
    }

    private void searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPatientsFromDB();
            return;
        }

        try {
            patientList.clear();
            List<Patient> foundPatients = patientDao.searchPatients(searchTerm.trim());
            patientList.addAll(foundPatients);
        } catch (SQLException e) {
            showAlert("Ошибка при поиске пациентов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPatientsFromDB() {
        try {
            patientList.clear();
            patientList.addAll(patientDao.getAllPatients());
        } catch (SQLException e) {
            showAlert("Ошибка загрузки пациентов из базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleAddPatient() {
        Dialog<Patient> dialog = createPatientDialog(null);
        dialog.setTitle("Добавление пациента");
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            try {
                patientDao.addPatient(patient);
                loadPatientsFromDB();
            } catch (SQLException e) {
                showAlert("Ошибка при добавлении пациента: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void handleEditPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Не выбран пациент", "Пожалуйста, выберите пациента для редактирования.");
            return;
        }
        Dialog<Patient> dialog = createPatientDialog(selected);
        dialog.setTitle("Редактирование пациента");
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            try {
                patient.setPatientId(selected.getPatientId());
                patientDao.updatePatient(patient);
                loadPatientsFromDB();
            } catch (SQLException e) {
                showAlert("Ошибка при обновлении пациента: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void handleDeletePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Не выбран пациент", "Пожалуйста, выберите пациента для удаления.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить пациента?");
        alert.setContentText("Вы уверены, что хотите удалить пациента " +
                selected.getFirstName() + " " + selected.getLastName() +
                "? Все его данные (приёмы, рецепты, медзаписи, учёт) будут удалены без возможности восстановления.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Используем архивацию
                    patientDao.archivePatient(selected.getPatientId(), "admin"); // или текущий пользователь
                    loadPatientsFromDB();
                    showInfo("Пациент удалён (архивирован).");
                } catch (SQLException e) {
                    showAlert("Ошибка при удалении пациента: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private Dialog<Patient> createPatientDialog(Patient patient) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        DatePicker birthDatePicker = new DatePicker();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField snilsField = new TextField();
        TextField policyField = new TextField();
        TextField districtField = new TextField();
        TextField addressField = new TextField();

        // Добавляем TextFormatter для проверки ввода только цифр в поле участка
        districtField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        // Подсказки для полей ввода
        firstNameField.setPromptText("Имя");
        lastNameField.setPromptText("Фамилия");
        phoneField.setPromptText("+79161234567");
        emailField.setPromptText("email@example.com");
        snilsField.setPromptText("123-456-789 00");
        policyField.setPromptText("1234567890123456");
        districtField.setPromptText("1");
        addressField.setPromptText("ул. Ленина, д. 10");

        if (patient != null) {
            firstNameField.setText(patient.getFirstName());
            lastNameField.setText(patient.getLastName());
            birthDatePicker.setValue(patient.getBirthDate());
            phoneField.setText(patient.getPhoneNumber());
            emailField.setText(patient.getEmail());
            snilsField.setText(patient.getSnils());
            policyField.setText(patient.getPolicyOMS());
            districtField.setText(String.valueOf(patient.getDistrict()));
            addressField.setText(patient.getAddress());
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);

        grid.addRow(0, new Label("Имя:"), firstNameField);
        grid.addRow(1, new Label("Фамилия:"), lastNameField);
        grid.addRow(2, new Label("Дата рождения:"), birthDatePicker);
        grid.addRow(3, new Label("Телефон:"), phoneField);
        grid.addRow(4, new Label("Email:"), emailField);
        grid.addRow(5, new Label("СНИЛС:"), snilsField);
        grid.addRow(6, new Label("Полис ОМС:"), policyField);
        grid.addRow(7, new Label("Участок:"), districtField);
        grid.addRow(8, new Label("Адрес:"), addressField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Patient p = new Patient();
                p.setFirstName(firstNameField.getText());
                p.setLastName(lastNameField.getText());
                p.setBirthDate(birthDatePicker.getValue());
                p.setPhoneNumber(phoneField.getText());
                p.setEmail(emailField.getText());
                p.setSnils(snilsField.getText());
                p.setPolicyOMS(policyField.getText());

                // Преобразуем текст в int для участка
                try {
                    if (!districtField.getText().isEmpty()) {
                        p.setDistrict(Integer.parseInt(districtField.getText()));
                    } else {
                        p.setDistrict(0); // или значение по умолчанию
                    }
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Участок должен быть числом");
                    return null;
                }

                p.setAddress(addressField.getText());
                if (patient != null) {
                    p.setPatientId(patient.getPatientId());
                }
                return p;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(header);
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