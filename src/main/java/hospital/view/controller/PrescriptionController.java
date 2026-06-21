package hospital.view.controller;

import hospital.PrescriptionDao;
import hospital.daomodel.Prescription;
import hospital.daomodel.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

public class PrescriptionController {
    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, String> personColumn;
    @FXML private TableColumn<Prescription, String> medicineColumn;
    @FXML private TableColumn<Prescription, String> dosageColumn;
    @FXML private VBox doctorForm; // форма для врача
    @FXML private ComboBox<String> patientComboBox;
    @FXML private TextField medicineField;
    @FXML private TextField dosageField;
    @FXML private Button addPrescriptionButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchPrescriptionsField;

    private final ObservableList<Prescription> prescriptionData = FXCollections.observableArrayList();
    private User currentUser;
    public void setCurrentUser(User user) {
        this.currentUser = user;
        PrescriptionDao dao = new PrescriptionDao();
        try {
            prescriptionData.setAll(dao.getPrescriptionsByRole(user.getRole(), user.getRoleId()));
            prescriptionTable.setItems(prescriptionData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            personColumn.setText("Пациент");
            doctorForm.setVisible(true);
            doctorForm.setManaged(true);
            try {
                patientComboBox.setItems(FXCollections.observableArrayList(
                        dao.getPatientsByDoctorId(user.getRoleId())));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            personColumn.setText("Врач");
            doctorForm.setVisible(false);
            doctorForm.setManaged(false);
        }
        // Показываем кнопки только для DOCTOR или ADMIN
        boolean canEdit = "DOCTOR".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole());
        if (editButton != null) {
            editButton.setVisible(canEdit);
            editButton.setManaged(canEdit);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(canEdit);
            deleteButton.setManaged(canEdit);
        }
    }
    @FXML
    private void initialize() {
        personColumn.setCellValueFactory(cellData -> {
            Prescription p = cellData.getValue();
            if (currentUser == null) return new SimpleStringProperty("");
            return "DOCTOR".equalsIgnoreCase(currentUser.getRole())
                    ? new SimpleStringProperty(p.getPatientName())
                    : new SimpleStringProperty(p.getDoctorName());
        });
        medicineColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedicine()));
        dosageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDosage()));
        searchPrescriptionsField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPrescriptions(newValue);
        });
    }
    @FXML
    private void handleAddPrescription() {
        String patientName = patientComboBox.getValue();
        String medicine = medicineField.getText();
        String dosage = dosageField.getText();
        if (patientName == null || patientName.isEmpty() ||
                medicine == null || medicine.isEmpty() ||
                dosage == null || dosage.isEmpty()) {
            showError("Пожалуйста, заполните все поля для добавления рецепта.");
            return;
        }
        PrescriptionDao dao = new PrescriptionDao();
        try {
            dao.addPrescription(currentUser.getRoleId(), patientName, medicine, dosage);
            prescriptionData.setAll(dao.getPrescriptionsByRole(currentUser.getRole(), currentUser.getRoleId()));
            patientComboBox.setValue(null);
            medicineField.clear();
            dosageField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при добавлении рецепта: " + e.getMessage());
        }
    }
    @FXML
    private void handleDeletePrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите рецепт для удаления.");
            return;
        }
        PrescriptionDao dao = new PrescriptionDao();
        try {
            dao.deletePrescription(selected.getPrescriptionId());
            prescriptionData.setAll(dao.getPrescriptionsByRole(currentUser.getRole(), currentUser.getRoleId()));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при удалении рецепта: " + e.getMessage());
        }
    }
    @FXML
    private void handleEditPrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите рецепт для изменения.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.getMedicine());
        dialog.setTitle("Изменить рецепт");
        dialog.setHeaderText("Изменить лекарство и дозировку");
        dialog.setContentText("Новое название лекарства:");
        dialog.showAndWait().ifPresent(newMedicine -> {
            TextInputDialog dosageDialog = new TextInputDialog(selected.getDosage());
            dosageDialog.setTitle("Изменить дозировку");
            dosageDialog.setHeaderText("Изменить дозировку");
            dosageDialog.setContentText("Новая дозировка:");
            dosageDialog.showAndWait().ifPresent(newDosage -> {
                PrescriptionDao dao = new PrescriptionDao();
                try {
                    dao.updatePrescription(selected.getPrescriptionId(), newMedicine, newDosage);
                    prescriptionData.setAll(dao.getPrescriptionsByRole(currentUser.getRole(), currentUser.getRoleId()));
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Ошибка при обновлении рецепта: " + e.getMessage());
                }
            });
        });
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void searchPrescriptions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Загружаем все рецепты
            PrescriptionDao dao = new PrescriptionDao();
            try {
                prescriptionData.setAll(dao.getPrescriptionsByRole(currentUser.getRole(), currentUser.getRoleId()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            prescriptionData.clear();
            PrescriptionDao dao = new PrescriptionDao();
            List<Prescription> foundPrescriptions = dao.searchPrescriptions(searchTerm.trim(), currentUser.getRole(), currentUser.getRoleId());
            prescriptionData.addAll(foundPrescriptions);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Ошибка при поиске рецептов.");
        }
    }
}
