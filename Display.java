import java.awt.event.ActionEvent;
import java.sql.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
    private String customerId;
    private String appointment;
    public Display(Stage primaryStage,String user,String customerId,String appointment){
        this.user = user;
        this.stage = primaryStage;
        this.customerId = customerId;
        this.appointment = appointment;
    }
    public void displayApp(){
        Button goBack = new Button("Back");
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
        grid.add(new Label("Customer Id"),0,0);
        grid.add(new Label(customerId),0,1);
        grid.add(new Label("Appointment"),1,0);
        grid.add(new Label(appointment),1,1);
        grid.add(goBack,0,2);
        Scene scene = new Scene(grid, 500, 300);
        stage.setScene(scene);
        stage.show();


    }
}
