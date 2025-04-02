import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public class appointment extends Application {
        private TextField qidField;
        private TextField appointmentDate;
        private TextField appointmentTime;

        @Override
        public void start(Stage stage) {
            stage.setTitle("Appointment");

            qidField = new TextField();
            appointmentDate = new TextField();
            appointmentTime = new TextField();
            Button createAppointmentBtn = new Button("Create Appointment");
            Button backButton = new Button("Back");

            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Create Appointment"), 0, 0);
            grid.add(new Label("QID:"), 0, 1);
            grid.add(qidField, 1, 1);
            grid.add(new Label("Date (YYYY-MM-DD):"), 0, 2);
            grid.add(appointmentDate, 1, 2);
            grid.add(new Label("Time (HH:MM):"), 0, 3);
            grid.add(appointmentTime, 1, 3);
            grid.add(createAppointmentBtn, 0, 4);
            grid.add(backButton, 1, 4);

            createAppointmentBtn.setOnAction(e -> createAppointment());
            backButton.setOnAction(e -> new EmployeePage(stage, "Employee").initializeComponents());

            Scene scene = new Scene(grid, 400, 300);
            stage.setScene(scene);
            stage.show();
        }

        private void createAppointment() {
            String qid = qidField.getText();
            String date = appointmentDate.getText();
            String time = appointmentTime.getText() + ":00"; // Ensuring HH:MM:SS format

            if (qid.isEmpty() || date.isEmpty() || time.isEmpty()) {
                showAlert("Error", "QID, Date, and Time must not be empty!");
                return;
            }

            try (Connection con = DBUtils.establishConnection()) {
                // Step 1: Check if the QID exists in customers table
                String getCustomerQuery = "SELECT QID FROM customer WHERE QID = ?";
                PreparedStatement getCustomerStmt = con.prepareStatement(getCustomerQuery);
                getCustomerStmt.setString(1, qid);
                ResultSet rs = getCustomerStmt.executeQuery();

                if (!rs.next()) {
                    showAlert("Error", "Customer with QID not found!");
                    return;
                }

                String customerId = rs.getString("QID"); // Keep QID as a String

                // Step 2: Insert into appointments table
                String insertAppointmentQuery = "INSERT INTO appointments (customerId, date, time) VALUES (?, ?, ?)";
                PreparedStatement insertAppointmentStmt = con.prepareStatement(insertAppointmentQuery);
                insertAppointmentStmt.setString(1, customerId); // Use String instead of int
                insertAppointmentStmt.setString(2, date);
                insertAppointmentStmt.setString(3, time);
                int appointmentInserted = insertAppointmentStmt.executeUpdate();

                if (appointmentInserted > 0) {
                    // Step 3: Update due amount in customers table
                    String updateDueQuery = "UPDATE customer SET due = due + 100 WHERE QID = ?";
                    PreparedStatement updateDueStmt = con.prepareStatement(updateDueQuery);
                    updateDueStmt.setString(1, qid);
                    updateDueStmt.executeUpdate();

                    showAlert("Success", "Appointment created and due updated to +100 QAR!");
                } else {
                    showAlert("Failure", "Failed to create appointment.");
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


    public class userRegister extends Application{

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

    public class SparePartsPage extends Application {

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
        
    }

    private static void registerUser() {
        String qid = qId.getText();
        String nameData = name.getText();
        String phoneData = phone.getText();
        String regexQid = "^\\d{11}$";
        String regexName = "^[A-Za-z ]{3,}$";
        String regexPhone = "^\\d{8}$";
        Pattern patternQid = Pattern.compile(regexQid);
        Pattern patternName = Pattern.compile(regexName);
        Pattern patternPhone = Pattern.compile(regexPhone);
        Matcher matcherQid = patternQid.matcher(qid);
        Matcher matcherName = patternName.matcher(nameData);
        Matcher matcherPhone = patternPhone.matcher(phoneData);
        if(matcherQid.matches()&&matcherPhone.matches()&&matcherName.matches()) {
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
        else{
            showAlert("Error","Invalid Data");
        }
    }

    private static void createAppointment() {
        String date = appointmentDate.getText();
        String time = appointmentTime.getText();
        String regexDate = "^\\d{4}-\\d{2}-\\d{2}$";
        String regexTime = "^\\d{2}:\\d{2}$";
        Pattern patternDate = Pattern.compile(regexDate);
        Pattern patternTime = Pattern.compile(regexTime);
        Matcher matcherDate = patternDate.matcher(date);
        Matcher matcherTime = patternTime.matcher(time);

        String query = "INSERT INTO appointments (employee_id, appointment_date, appointment_time) VALUES (?, ?, ?)";

        if(matcherDate.matches() && matcherTime.matches()){
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
            }}
        else{
            showAlert("Error", "Invalid date or time");
        }
    }

    private void payBill() {
        // Ask for QID
        TextInputDialog qidDialog = new TextInputDialog();
        qidDialog.setTitle("Customer QID");
        qidDialog.setHeaderText(null);
        qidDialog.setContentText("Enter Customer QID:");

        String qid = qidDialog.showAndWait().orElse("");
        String regexQid = "^\\d{11}$";
        Pattern patternQid = Pattern.compile(regexQid);
        Matcher matcherQid = patternQid.matcher(qid);

        if (!matcherQid.matches()) {
            showAlert("Error", "Invalid QID format.");
            return;
        }

        String fetchQuery = "SELECT Due FROM customer WHERE QID = ?";
        try (Connection con = DBUtils.establishConnection();
             PreparedStatement fetchStmt = con.prepareStatement(fetchQuery)) {
            fetchStmt.setString(1, qid);
            ResultSet rs = fetchStmt.executeQuery();

            if (rs.next()) {
                double dueAmount = rs.getDouble("Due");

                TextInputDialog paymentDialog = new TextInputDialog();
                paymentDialog.setTitle("Pay Bill");
                paymentDialog.setHeaderText("Due Amount: " + dueAmount + " QR");
                paymentDialog.setContentText("Enter payment amount:");

                String amountText = paymentDialog.showAndWait().orElse("");
                if (!amountText.matches("^\\d+(\\.\\d{1,2})?$")) {
                    showAlert("Error", "Invalid Amount");
                    return;
                }

                double amountPaid = Double.parseDouble(amountText);
                double newDue = dueAmount - amountPaid;

                String updateQuery = "UPDATE customer SET Due = ? WHERE QID = ?";
                try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, newDue);
                    updateStmt.setString(2, qid);
                    int result = updateStmt.executeUpdate();

                    if (result > 0) {
                        // Insert into history_records
                        String insertHistoryQuery = "INSERT INTO history_records (appointmentId, customerId, appointmentDate, appointmentTime, amountPaid) " +
                                "SELECT appId, customerId, date, time, ? FROM appointments WHERE customerId = ?";
                        try (PreparedStatement insertStmt = con.prepareStatement(insertHistoryQuery)) {
                            insertStmt.setDouble(1, amountPaid);
                            insertStmt.setString(2, qid);
                            insertStmt.executeUpdate();
                        }

                        showAlert("Success", "Payment successful! New due: " + newDue + " QR");
                    } else {
                        showAlert("Failure", "Failed to update payment.");
                    }
                }
            } else {
                showAlert("Error", "Customer with QID " + qid + " not found.");
            }
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
