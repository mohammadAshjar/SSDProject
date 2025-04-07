import java.sql.*;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class Display {
    private String user,role;
    private Scene display;
    private Stage stage;
    private ResultSet rs;
    public Display(Stage primaryStage,String user, ResultSet rs, String role){
        this.user = user;
        this.stage = primaryStage;
        this.rs = rs;
        this.role = role;
    }
    public void displayApp() throws SQLException {
        if(user.equals("Admin") || user.equals("admin")){
            Button goBack = new Button("Back");
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10,10,10,10));
            goBack.setOnAction(actionEvent -> {
                Admin admin = new Admin(stage,user, role);
                try {
                    admin.initializeComponents();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            rs.beforeFirst();
            int row = 0;
            while(rs.next()) {
                String appId = rs.getString("appId");
                String carId = rs.getString("CustomerId");
                String date = rs.getString("date");

                grid.add(new Label("Appoitnment Id: "), 0, row);
                grid.add(new Label(appId), 1, row);
                grid.add(new Label("Customer Id: "), 2, row);
                grid.add(new Label(carId), 3, row);
                grid.add(new Label("Date: "), 4, row);
                grid.add(new Label(date), 5, row);

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(event -> deleteAppointment(appId));

                Button updateButton = new Button("Update");
                updateButton.setOnAction(event -> openUpdateWindow(appId));


                grid.add(deleteButton, 6, row);
                grid.add(updateButton, 7, row);
                row++;
            }
            grid.add(goBack,0,row);
            Scene display = new Scene(grid, 500, 300);
            stage.setScene(display);
            stage.show();
        }
        else {
            Button goBack = new Button("Back");
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));
            goBack.setOnAction(actionEvent -> {
                if(role.equals("Manager") || role.equals("manager")){
                    ManagerPage managerPage = new ManagerPage(stage,user,"Manager");
                    try {
                        managerPage.initializeComponents();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                }}
                else{
                    EmployeePage employeePage = new EmployeePage(stage,user, role);
                    try {
                        employeePage.initializeComponents();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                }}
            });
            rs.beforeFirst();
            int row = 0;
            while (rs.next()) {
                String appId = rs.getString("appId");
                String carId = rs.getString("CustomerId");
                String date = rs.getString("date");

                grid.add(new Label("Appoitnment Id: "), 0, row);
                grid.add(new Label(appId), 1, row);
                grid.add(new Label("Customer Id: "), 2, row);
                grid.add(new Label(carId), 3, row);
                grid.add(new Label("Date: "), 4, row);
                grid.add(new Label(date), 5, row);
                row++;
            }
            grid.add(goBack, 0, row);
            Scene display = new Scene(grid, 500, 300);
            stage.setScene(display);
            stage.show();

        }
    }


    public static void deleteAppointment(String appId){
        String query = "DELETE from appointments where `appId`=? ";
        try{
            Connection con = DBUtils.establishConnection();
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1,appId);
            int rs = statement.executeUpdate();
            if(rs>0){
                showAlert("Success", "Appointment Deleted");
            }
            else{
                showAlert("Failure","Error");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void openUpdateWindow(String appId) {
        Stage updateStage = new Stage();
        updateStage.setTitle("Update Appointment");

        GridPane updateGrid = new GridPane();
        updateGrid.setHgap(10);
        updateGrid.setVgap(10);
        updateGrid.setPadding(new Insets(10));

        Label lblDate = new Label("New Date (YYYY-MM-DD):");
        TextField txtDate = new TextField();

        Button btnSave = new Button("Save");
        btnSave.setOnAction(e -> {
            updateAppointment(appId, txtDate.getText());
            updateStage.close();
        });

        updateGrid.add(lblDate, 0, 0);
        updateGrid.add(txtDate, 1, 0);
        updateGrid.add(btnSave, 1, 1);

        Scene updateScene = new Scene(updateGrid, 300, 150);
        updateStage.setScene(updateScene);
        updateStage.show();
    }

    private void updateAppointment(String appId, String newDate) {
        String query = "UPDATE appointments SET date = ? WHERE appId = ?";
        try (Connection con = DBUtils.establishConnection();
             PreparedStatement statement = con.prepareStatement(query)) {

            statement.setString(1, newDate);
            statement.setString(2, appId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                showAlert("Success", "Appointment updated successfully.");
            } else {
                showAlert("Failure", "No changes made.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database update failed: " + e.getMessage());
        }
    }

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
