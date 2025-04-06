// utils/AppUtils.java
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AppUtils {

    public static void logout(Stage stage) {
        UserLogin login = new UserLogin(stage);
        login.initializeComponents();
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
