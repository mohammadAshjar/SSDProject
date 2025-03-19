import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class EmployeePage {
    private Stage stage;
    private String username;

    private TextField appointmentDate, appointmentTime, billAmount;
    private Button createAppointmentBtn, payBillBtn;

    public EmployeePage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void initializeComponents() {
        stage.setTitle("Employee Dashboard - " + username);

        appointmentDate = new TextField();
        appointmentTime = new TextField();
        createAppointmentBtn = new Button("Create Appointment");

        billAmount = new TextField();
        payBillBtn = new Button("Pay Bill");

        createAppointmentBtn.setOnAction(e -> createAppointment());
        payBillBtn.setOnAction(e -> payBill());

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

        grid.add(new Label("Pay Bill"), 0, 4);
        grid.add(new Label("Amount to Pay:"), 0, 5);
        grid.add(billAmount, 1, 5);
        grid.add(payBillBtn, 0, 6);

        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private void createAppointment() {
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

    private int getEmployeeId() {
        // TODO: Replace this placeholder logic with actual employee ID logic using `username`
        return 1;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
