package hospital.view.controller;

import hospital.DoctorDao;
import hospital.daomodel.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DocManController {
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, Number> idColumn;
    @FXML private TableColumn<Doctor, String> firstNameColumn;
    @FXML private TableColumn<Doctor, String> lastNameColumn;
    @FXML private TableColumn<Doctor, String> specializationColumn;
    @FXML private TableColumn<Doctor, String> roomNumberColumn;
    @FXML private TableColumn<Doctor, String> scheduleColumn;
    @FXML private TableColumn<Doctor, String> emailColumn;
    @FXML private TextField searchDoctorsField; // Добавлено поле поиска

    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private final DoctorDao doctorDao = new DoctorDao();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDoctorId()));
        firstNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        specializationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialization()));
        roomNumberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRoomNumber()));
        scheduleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSchedule()));
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        doctorTable.setItems(doctorList);

        // Автоматическая настройка ширины столбцов при изменении размера таблицы
        doctorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadDoctorsFromDB();

        // Добавлен обработчик поиска
        setupSearchFilter();
    }

    private void setupSearchFilter() {
        searchDoctorsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDoctors(newValue);
        });
    }

    private void searchDoctors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadDoctorsFromDB();
            return;
        }

        try {
            doctorList.clear();
            List<Doctor> foundDoctors = doctorDao.searchDoctors(searchTerm.trim());
            doctorList.addAll(foundDoctors);
        } catch (SQLException e) {
            showAlert("Ошибка при поиске врачей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDoctorsFromDB() {
        try {
            doctorList.clear();
            doctorList.addAll(doctorDao.getAllDoctors());
        } catch (SQLException e) {
            showAlert("Ошибка загрузки врачей из базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleAddDoctor() {
        Dialog<Doctor> dialog = createDoctorDialog(null);
        dialog.setTitle("Добавить врача");
        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            try {
                doctorDao.addDoctor(doctor);
                loadDoctorsFromDB();
            } catch (SQLException e) {
                showAlert("Ошибка при добавлении врача: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void handleEditDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Пожалуйста, выберите врача для редактирования.");
            return;
        }
        Dialog<Doctor> dialog = createDoctorDialog(selected);
        dialog.setTitle("Редактировать врача");
        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            try {
                doctor.setDoctorId(selected.getDoctorId());
                doctorDao.updateDoctor(doctor);
                loadDoctorsFromDB();
            } catch (SQLException e) {
                showAlert("Ошибка при обновлении врача: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void handleDeleteDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Пожалуйста, выберите врача для удаления.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить выбранного врача? Все его данные (приёмы, рецепты, медзаписи, учёт) будут удалены.", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Подтверждение удаления");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    doctorDao.archiveDoctor(selected.getDoctorId(), "admin");
                    loadDoctorsFromDB();
                    showAlert("Врач удалён (архивирован).");
                } catch (SQLException e) {
                    showAlert("Ошибка при удалении врача: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private Dialog<Doctor> createDoctorDialog(Doctor doctor) {
        Dialog<Doctor> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField specializationField = new TextField();
        TextField roomField = new TextField();
        TextField scheduleField = new TextField();
        TextField emailField = new TextField();

        firstNameField.setPromptText("Имя");
        lastNameField.setPromptText("Фамилия");
        specializationField.setPromptText("Терапевт");
        roomField.setPromptText("101");
        scheduleField.setPromptText("Пн-Пт 9:00-18:00");
        emailField.setPromptText("doctor@hospital.ru");

        if (doctor != null) {
            firstNameField.setText(doctor.getFirstName());
            lastNameField.setText(doctor.getLastName());
            specializationField.setText(doctor.getSpecialization());
            roomField.setText(doctor.getRoomNumber());
            scheduleField.setText(doctor.getSchedule());
            emailField.setText(doctor.getEmail());
        }

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.addRow(0, new Label("Имя:"), firstNameField);
        grid.addRow(1, new Label("Фамилия:"), lastNameField);
        grid.addRow(2, new Label("Специализация:"), specializationField);
        grid.addRow(3, new Label("Кабинет:"), roomField);
        grid.addRow(4, new Label("График:"), scheduleField);
        grid.addRow(5, new Label("Email:"), emailField);

        dialog.getDialogPane().setContent(grid);

        // Получаем кнопку OK
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        // Перехватываем событие нажатия на OK
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Doctor d = new Doctor();
                d.setFirstName(firstNameField.getText());
                d.setLastName(lastNameField.getText());
                d.setSpecialization(specializationField.getText());
                d.setRoomNumber(roomField.getText());
                d.setSchedule(scheduleField.getText());
                d.setEmail(emailField.getText());
                if (doctor != null) d.setDoctorId(doctor.getDoctorId());
                dialog.setResult(d); // сохраняем результат
                // Если всё прошло успешно, диалог закроется автоматически
            } catch (IllegalArgumentException e) {
                showAlert("Ошибка ввода: " + e.getMessage());
                event.consume(); // предотвращает закрытие диалога
            }
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return dialog.getResult();
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
}