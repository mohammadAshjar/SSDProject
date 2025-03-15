import com.sun.javafx.stage.EmbeddedWindow;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Admin extends Application {
    private Stage stage;
    private String username;
    public Admin(Stage primaryStage, String username){
        this.stage = primaryStage;
        this.username = username;
    }
    @Override
    public void start(Stage stage) throws Exception {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Enter Your details"), 0, 0);
        Scene scene = new Scene(grid, 500, 300);
        // and the stage (window) encompasses the scene
        stage.setScene(scene);
        stage.show();
    }
}
