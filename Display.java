import java.awt.event.ActionEvent;
import java.sql.*;

import javafx.application.Application;
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
    private String user;
    private Scene display;
    private Stage stage;
    private ResultSet rs;
    public Display(Stage primaryStage,String user, ResultSet rs){
        this.user = user;
        this.stage = primaryStage;
        this.rs = rs;
    }
    public void displayApp() throws SQLException {
        Button goBack = new Button("Back");
        Button deleteAppointment = new Button("Delete");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10,10,10,10));
        goBack.setOnAction(actionEvent -> {
            Admin admin = new Admin(stage,user);
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
            String carId = rs.getString("CarId");
            String appointment = rs.getString("Appointment");

            grid.add(new Label("Car Id: "), 0, row);
            grid.add(new Label(carId), 1, row);
            grid.add(new Label("Appointment: "), 2, row);
            grid.add(new Label(appointment), 3, row);

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(event -> deleteAppointment(appId));

            grid.add(deleteButton, 4, row);
            row++;
        }
        grid.add(goBack,0,row);
        Scene display = new Scene(grid, 500, 300);
        stage.setScene(display);
        stage.show();
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
    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
