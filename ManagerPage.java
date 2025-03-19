import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerPage {
    private Stage stage;
    private String username;

    public ManagerPage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void initializeComponents() {
        stage.setTitle("Manager Dashboard - " + username);

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            new UserLogin(stage).initializeComponents(); // <-- Logout logic
        });

        Label welcomeLabel = new Label("Welcome, Manager " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button viewReportsBtn = new Button("View Reports");
        Button manageEmployeesBtn = new Button("Manage Employees");

        viewReportsBtn.setOnAction(e -> showAlert("Reports", "This would show reports (placeholder)."));
        manageEmployeesBtn.setOnAction(e -> showAlert("Manage Employees", "This would manage employees (placeholder)."));

        VBox layout = new VBox(15, welcomeLabel, viewReportsBtn, manageEmployeesBtn, logoutButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 250);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

