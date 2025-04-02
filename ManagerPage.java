import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        viewReportsBtn.setOnAction(e -> showReportPage());
        manageEmployeesBtn.setOnAction(e -> showAlert("Manage Employees", "This would manage employees (placeholder)."));

        VBox layout = new VBox(15, welcomeLabel, viewReportsBtn, manageEmployeesBtn, logoutButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 250);
        stage.setScene(scene);
        stage.show();
    }

    private void showReportPage() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Appointment History Report");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Appointment ID"), 0, 0);
        grid.add(new Label("Customer ID"), 1, 0);
        grid.add(new Label("Date"), 2, 0);
        grid.add(new Label("Time"), 3, 0);
        grid.add(new Label("Amount Paid (QR)"), 4, 0);

        try (Connection con = DBUtils.establishConnection()) {
            String query = "SELECT * FROM history_records";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            int row = 1;
            while (rs.next()) {
                grid.add(new Label(rs.getString("appointmentId")), 0, row);
                grid.add(new Label(rs.getString("customerId")), 1, row);
                grid.add(new Label(rs.getString("appointmentDate")), 2, row);
                grid.add(new Label(rs.getString("appointmentTime")), 3, row);
                grid.add(new Label(rs.getString("amountPaid")), 4, row);
                row++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> reportStage.close());
        grid.add(backButton, 0, grid.getChildren().size());

        Scene scene = new Scene(grid, 600, 400);
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
