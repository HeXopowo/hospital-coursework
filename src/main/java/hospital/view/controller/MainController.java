package hospital.view.controller;

import hospital.daomodel.User;
import hospital.view.MainApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.util.Optional;

public class MainController {
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Button addDoctorButton;
    @FXML
    private Button addPatientButton;

    private User currentUser;
    private MainApp mainApp;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initByRole();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void initByRole() {
        if (currentUser == null) return;

        // Создаем вкладки в зависимости от роли
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            openAdminTab(); // Новая вкладка для администратора
        } else if ("DOCTOR".equalsIgnoreCase(currentUser.getRole())) {
            openDoctorsTab();
        } else if ("PATIENT".equalsIgnoreCase(currentUser.getRole())) {
            openPatientsTab();
        }

        // Показывать кнопки только если роль — ADMIN
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (addDoctorButton != null) addDoctorButton.setVisible(isAdmin);
        if (addPatientButton != null) addPatientButton.setVisible(isAdmin);
    }

    @FXML
    private void handleShowAppointments(ActionEvent event) {
        // Для администратора открываем окно со всеми приемами
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            openAllAppointments();
        } else {
            // Для врачей и пациентов открываем их приемы
            openMyAppointments();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Вы действительно хотите выйти из учётной записи?");
        alert.setContentText("Нажмите \"OK\" для подтверждения.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage stage = (Stage) mainTabPane.getScene().getWindow();
                stage.close();
                mainApp.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.exit();
            }
        }
    }

    // Новая вкладка для администратора
    private void openAdminTab() {
        if (isTabOpen("Администратор")) {
            selectTab("Администратор");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/admin.fxml"));
            Parent content = loader.load();
            AdminController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Tab tab = new Tab("Администратор");
            tab.setContent(content);
            tab.setClosable(false);
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDoctorsTab() {
        if (isTabOpen("Врач")) {
            selectTab("Врач");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/doctor.fxml"));
            Parent content = loader.load();
            DoctorController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Tab tab = new Tab("Врач");
            tab.setContent(content);
            tab.setClosable(false);
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPatientsTab() {
        if (isTabOpen("Пациент")) {
            selectTab("Пациент");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/patient.fxml"));
            Parent content = loader.load();
            PatientController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            Tab tab = new Tab("Пациент");
            tab.setContent(content);
            tab.setClosable(false);
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAllAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/appointments.fxml"));
            Parent root = loader.load();
            AppointmentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Все приёмы");
            Scene scene = new Scene(root, 700, 700);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMyAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/appointments.fxml"));
            Parent root = loader.load();
            AppointmentsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Мои приёмы");
            Scene scene = new Scene(root, 700, 700);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTabOpen(String tabName) {
        return mainTabPane.getTabs().stream()
                .anyMatch(tab -> tab.getText().equals(tabName));
    }

    private void selectTab(String tabName) {
        mainTabPane.getTabs().stream()
                .filter(tab -> tab.getText().equals(tabName))
                .findFirst()
                .ifPresent(tab -> mainTabPane.getSelectionModel().select(tab));
    }

    @FXML
    private void handleAddDoctor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/doctor_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление врачами");

            // Устанавливаем увеличенный размер окна
            Scene scene = new Scene(root, 700, 600);
            stage.setScene(scene);

            // Центрируем окно
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPatient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/patient_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Управление пациентами");
            stage.setScene(new javafx.scene.Scene(root));
            centerStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void centerStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
}