// utils/AppUtils.java
import javafx.stage.Stage;

public class AppUtils {

    public static void logout(Stage stage) {
        // Session.clear();
        UserLogin login = new UserLogin(stage);
        login.initializeComponents();
    }

    public static void switchTo(Stage stage, javafx.scene.Scene scene, String title) {
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
