import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManagerPage {
    private Stage stage;
    private String username;
    private static String algorithm = "SHA-256";
    private static TextField newName, newRole, newPassword, newPhone;


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
        manageEmployeesBtn.setOnAction(e -> {
            ManageEmployee manageEmployee = new ManageEmployee();
            try {
                manageEmployee.start(stage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

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

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public class ManageEmployee extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Manage Employees");
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10));
            grid.add(new Label("Employees"), 0, 0);
            loadEmployeeDetails(grid);

            Button backButton = new Button("Back");
            Button addEmployee = new Button("Add");
            backButton.setOnAction(e -> new ManagerPage(stage, "Manager").initializeComponents());
            addEmployee.setOnAction(actionEvent -> {
                try {
                    new newEmployee().start(stage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            grid.add(backButton, 1, 0);
            grid.add(addEmployee, 2, 0);


            Scene scene = new Scene(grid, 400, 400);
            stage.setScene(scene);
            stage.show();

        }

        private void loadEmployeeDetails(GridPane grid) {
            String sql = "SELECT * from users where Role = 'Employee'";

            try (Connection con = DBUtils.establishConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                int row = 1;
                while (rs.next()) {
                    String name = rs.getString("Name");
                    String phone = rs.getString("Phone");
                    Label detailLabel = new Label("Name: " + name + " | Phone: " + phone);
                    grid.add(detailLabel, 0, row++);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage());
            }
        }

        public static class newEmployee extends Application {

            @Override
            public void start(Stage stage) throws Exception {
                stage.setTitle("Registeration");
                newName = new TextField();
                newPassword = new TextField();
                newRole = new TextField();
                newPhone = new TextField();
                Button registerButton = new Button("Register");

                registerButton.setOnAction(e -> {
                    try {
                        addUser(stage);
                    } catch (NoSuchAlgorithmException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                GridPane grid = new GridPane();
                grid.setVgap(10);
                grid.setHgap(10);
                grid.setPadding(new Insets(10, 10, 10, 10));
                grid.add(new Label("Enter User details"), 0, 0);
                grid.add(new Label("Enter Role:"), 0, 2);
                grid.add(newRole, 1, 2);
                grid.add(new Label("Enter Name:"), 0, 3);
                grid.add(newName, 1, 3);
                grid.add(new Label("Enter Password:"), 0, 4);
                grid.add(newPassword, 1, 4);
                grid.add(new Label("Enter Phone Number:"), 0, 5);
                grid.add(newPhone, 1, 5);
                grid.add(registerButton, 0, 6);
                Scene scene = new Scene(grid, 500, 300);
                stage.setScene(scene);
                stage.show();
            }
        }

        private static void addUser(Stage primaryStage) throws NoSuchAlgorithmException {
            String name = newName.getText();
            String password = newPassword.getText();
            String role = newRole.getText();
            byte[] salt = createSalt();
            String hashPass = generateHash(password, algorithm, salt);
            String phone = newPhone.getText();
            String query = "INSERT INTO `users` (`Role`, `Name`, `Password`, `Salt`, `Phone`) values(?,?,?,?,?) ;";
            String regexRole = "^(Employee|employee)$";
            String regexName = "^[A-Za-z]+$";
            String regexPassword = "^[A-Za-z0-9!@#$%^&*_ -]+$";
            String regexPhone = "^\\d{8}$";
            Pattern pattern = Pattern.compile(regexRole);
            Pattern patternName = Pattern.compile(regexName);
            Pattern patternPass = Pattern.compile(regexPassword);
            Pattern patternPhone = Pattern.compile(regexPhone);
            Matcher matcher = pattern.matcher(role);
            Matcher matcherName = patternName.matcher(name);
            Matcher matcherPass = patternPass.matcher(password);
            Matcher matcherPhone = patternPhone.matcher(phone);

            if (matcher.matches() && matcherName.matches() && matcherPass.matches() && matcherPhone.matches()) {
                try {
                    Connection con = DBUtils.establishConnection();
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, role);
                    statement.setString(2, name);
                    statement.setString(3, hashPass);
                    statement.setString(4, Arrays.toString(salt));
                    statement.setString(5,phone);
                    int rs = statement.executeUpdate();
                    if (rs == 1) {
                        showAlert("Success", "Employee Added");
                    } else {
                        showAlert("Failure", "Failed to Add Manager");
                    }
                    DBUtils.closeConnection(con, statement);
                    primaryStage.close();
                } catch (Exception e) {
                    showAlert("Failure", "Failed to connect to Database");
                }
            } else {
                showAlert("Failure", "Wrong Details");
            }
        }
    }
    public static byte[] createSalt(){
        byte[] bytes = new byte[5];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
    public static String generateHash(String data, String algorithm, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(data.getBytes());
        return bytesToHexString(hash);
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes){
        char[] hexChars = new char[bytes.length*2];
        for(int j = 0; j < bytes.length; j++ ){
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v >>> 4];
            hexChars[j*2+1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}