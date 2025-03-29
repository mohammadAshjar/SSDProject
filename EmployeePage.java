import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;

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

        Button sparePartsButton = new Button("Spare Parts");
        sparePartsButton.setOnAction(e -> {
            try {
                new SparePartsPage().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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
        grid.add(sparePartsButton,0, 3 );
        grid.add(logoutButton, 0, 4);

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
            Button backButton = new Button("Back");
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
            grid.add(backButton, 1, 3);

            createAppointmentBtn.setOnAction(e -> createAppointment());
            backButton.setOnAction(e -> new EmployeePage(stage, "Employee").initializeComponents());
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
            Button backButton = new Button("Back");
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
            grid.add(backButton, 1, 4);

            registerUser.setOnAction(e -> registerUser());
            backButton.setOnAction(e -> new EmployeePage(stage, "Employee").initializeComponents());
            Scene scene = new Scene(grid, 400, 300);
            stage.setScene(scene);
            stage.show();

        }
    }

    public static class SparePartsPage extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Spare Parts Inventory");

            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Available Spare Parts"), 0, 0);

            loadSpareParts(grid);

            Button backButton = new Button("Back");
            backButton.setOnAction(e -> new EmployeePage(stage, "Employee").initializeComponents());
            grid.add(backButton, 1, 0);

            Scene scene = new Scene(grid, 400, 400);
            stage.setScene(scene);
            stage.show();
        }

        private void loadSpareParts(GridPane grid) {
            String sql = "SELECT part_name, quantity, price FROM spare_parts";

            try (Connection con = DBUtils.establishConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                int row = 1;
                while (rs.next()) {
                    String partName = rs.getString("part_name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    Label partLabel = new Label("Name: " + partName + " | Quantity: " + quantity + " | Price: " + price + " QR");
                    grid.add(partLabel, 0, row++);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage());
            }
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
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
        // Ask for QID
        TextInputDialog qidDialog = new TextInputDialog();
        qidDialog.setTitle("Customer QID");
        qidDialog.setHeaderText(null);
        qidDialog.setContentText("Enter Customer QID:");

        String qid = qidDialog.showAndWait().orElse("");
        if (qid.isEmpty()) {
            showAlert("Error", "QID must not be empty!");
            return;
        }

        // Fetch due amount from database
        String fetchQuery = "SELECT Due FROM customer WHERE QID = ?";
        try (Connection con = DBUtils.establishConnection();
             PreparedStatement fetchStmt = con.prepareStatement(fetchQuery)) {

            fetchStmt.setString(1, qid);
            ResultSet rs = fetchStmt.executeQuery();

            if (rs.next()) {
                double dueAmount = Double.parseDouble(rs.getString("Due"));

                // Show due amount and ask for payment
                TextInputDialog paymentDialog = new TextInputDialog();
                paymentDialog.setTitle("Pay Bill");
                paymentDialog.setHeaderText("Due Amount: " + dueAmount + " QR");
                paymentDialog.setContentText("Enter payment amount:");

                String amountText = paymentDialog.showAndWait().orElse("");
                if (amountText.isEmpty()) {
                    showAlert("Error", "Payment amount must not be empty!");
                    return;
                }

                double amountPaid = Double.parseDouble(amountText);
                double newDue = dueAmount - amountPaid;

                // Update the due amount in the database
                String updateQuery = "UPDATE customer SET Due = ? WHERE QID = ?";
                try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, String.valueOf(newDue));
                    updateStmt.setString(2, qid);
                    int result = updateStmt.executeUpdate();

                    if (result > 0) {
                        showAlert("Success", "Payment successful! New due: " + newDue + " QR");
                    } else {
                        showAlert("Failure", "Failed to update payment.");
                    }
                }

            } else {
                showAlert("Error", "Customer with QID " + qid + " not found.");
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
