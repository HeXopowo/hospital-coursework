package hospital.view;

// import hospital.api.RestApiServer;
import hospital.daomodel.User;
import hospital.view.controller.LoginController;
import hospital.view.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showLogin();
        RestApiServer.start();
    }

    private void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setMainApp(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(scene);

        // Устанавливаем размеры окна и запрещаем изменение размера
        primaryStage.setWidth(400);
        primaryStage.setHeight(300);
        primaryStage.setResizable(false);

        // Центрируем окно
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void showMainWindow(User loggedUser) throws Exception {
        // Закрываем окно авторизации перед открытием главного окна
        primaryStage.close();

        // Создаем новое окно для основного приложения
        Stage mainStage = new Stage();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/fxml/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setCurrentUser(loggedUser);
        controller.setMainApp(this);

        Scene scene = new Scene(root);
        // Устанавливаем цвет фона сцены, чтобы избежать черных областей
        scene.setFill(Color.WHITE);

        mainStage.setTitle("Hospital System - " + loggedUser.getRole());
        mainStage.setScene(scene);

        // Устанавливаем размеры окна
        mainStage.setWidth(1200);
        mainStage.setHeight(800);

        // Центрируем окно
        mainStage.centerOnScreen();

        // Показываем окно
        mainStage.show();

        // Обновляем primaryStage для последующих операций
        this.primaryStage = mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}