import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class EmployeePage {
    private Stage stage;
    private String username;
    private static TextField qId;
    private static TextField name;
    private static TextField phone;
    private static TextField appointmentDate;
    private static TextField appointmentTime;
    private TextField billAmount;
    private Button createAppointmentBtn, payBillBtn;

    public EmployeePage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void initializeComponents() {
        stage.setTitle("Employee Dashboard - " + username);
        Button logoutButton = new Button("Logout");
        Button registerCustomer = new Button("Register Customer");
        logoutButton.setOnAction(e -> {
            new UserLogin(stage).initializeComponents(); // <-- Logout logic
        });

        createAppointmentBtn = new Button("Create Appointment");

        billAmount = new TextField();
        payBillBtn = new Button("Pay Bill");

        createAppointmentBtn.setOnAction(actionEvent -> {
            try {
                new appointment().start(stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        registerCustomer.setOnAction(actionEvent -> {
            try {
                new userRegister().start(stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        payBillBtn.setOnAction(e -> payBill());

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        grid.add(createAppointmentBtn, 0, 0);
        grid.add(payBillBtn, 0, 1);
        grid.add(registerCustomer,0,2);
        grid.add(logoutButton, 0, 3);

        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    public static class appointment extends Application{

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Appointment");
            TextField appointmentDate = new TextField();
            TextField appointmentTime = new TextField();
            Button createAppointmentBtn = new Button("Create Appointment");
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Create Appointment"), 0, 0);
            grid.add(new Label("Date (YYYY-MM-DD):"), 0, 1);
            grid.add(appointmentDate, 1, 1);
            grid.add(new Label("Time (HH:MM):"), 0, 2);
            grid.add(appointmentTime, 1, 2);
            grid.add(createAppointmentBtn, 0, 3);
            createAppointmentBtn.setOnAction(e -> createAppointment());
            Scene scene = new Scene(grid, 400, 300);
            stage.setScene(scene);
            stage.show();

        }
    }
    public static class userRegister extends Application{

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Register");
            qId = new TextField();
            name = new TextField();
            phone = new TextField();
            Button registerUser = new Button("Register User");
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Register User"), 0, 0);
            grid.add(new Label("QID:"), 0, 1);
            grid.add(qId, 1, 1);
            grid.add(new Label("Name"), 0, 2);
            grid.add(name, 1, 2);
            grid.add(new Label("Phone"), 0, 3);
            grid.add(phone, 1, 3);
            grid.add(registerUser, 0, 4);
            registerUser.setOnAction(e -> registerUser());
            Scene scene = new Scene(grid, 400, 300);
            stage.setScene(scene);
            stage.show();

        }
    }
    private static void registerUser() {
        String qid = qId.getText();
        String nameData = name.getText();
        String phoneData = phone.getText();
        String query = "INSERT into customer (QID , Name , Phone) values (?,?,?)";
        try (Connection con = DBUtils.establishConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, qid);
            stmt.setString(2, nameData);
            stmt.setString(3, phoneData);
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Customer Registered successfully!");
            } else {
                showAlert("Failure", "Failed to Register Customer.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private static void createAppointment() {
        String date = appointmentDate.getText();
        String time = appointmentTime.getText();

        if (date.isEmpty() || time.isEmpty()) {
            showAlert("Error", "Date and Time must not be empty!");
            return;
        }

        String query = "INSERT INTO appointments (employee_id, appointment_date, appointment_time) VALUES (?, ?, ?)";
        try (Connection con = DBUtils.establishConnection()) {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, getEmployeeId()); // Update this method properly if needed
            stmt.setString(2, date);
            stmt.setString(3, time);
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Appointment created successfully!");
            } else {
                showAlert("Failure", "Failed to create appointment.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void payBill() {
        String amountText = billAmount.getText();

        if (amountText.isEmpty()) {
            showAlert("Error", "Amount must not be empty!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            String query = "UPDATE bills SET amount_paid = ?, status = 'Paid' WHERE employee_id = ? AND status = 'Pending'";
            try (Connection con = DBUtils.establishConnection()) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setDouble(1, amount);
                stmt.setInt(2, getEmployeeId());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert("Success", "Bill paid successfully!");
                } else {
                    showAlert("Failure", "No pending bills found or payment failed.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format.");
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private static int getEmployeeId() {
        // TODO: Replace this placeholder logic with actual employee ID logic using `username`
        return 1;
    }

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
