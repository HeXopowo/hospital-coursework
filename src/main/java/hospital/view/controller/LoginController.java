package hospital.view.controller;

import hospital.daomodel.User;
import hospital.UserDao;
import hospital.util.DialogStyler;
import hospital.view.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class LoginController {
    private MainApp mainApp;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    private UserDao userDao = new UserDao();
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    @FXML
    private void initialize() {
        // Подсказки
        usernameField.setTooltip(new Tooltip("Введите логин, выданный администратором"));
        passwordField.setTooltip(new Tooltip("Введите пароль"));

        // Enter — вход
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        // Esc — очистка полей; фокус на первое поле при открытии
        Platform.runLater(() -> {
            if (usernameField.getScene() != null) {
                usernameField.getScene().setOnKeyPressed(e -> {
                    if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) handleCancel();
                });
            }
            usernameField.requestFocus();
        });
    }
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = null;
        try {
            user = userDao.authenticate(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка при попытке аутентификации");
            return;
        }
        if (user != null) {
            try {
                mainApp.showMainWindow(user);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Ошибка при открытии главного окна");
            }
        } else {
            showError("Неверный логин или пароль");
        }
    }
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        DialogStyler.apply(alert);
        alert.showAndWait();
    }
    @FXML
    public void handleCancel() {
        usernameField.clear();
        passwordField.clear();
    }
}
