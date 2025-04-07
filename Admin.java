import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Admin extends Application {
    private Stage stage;
    private static String algorithm = "SHA-256";
    private String username,role;
    private static TextField newName, newRole, newPassword, newPhone;

    public Admin(Stage primaryStage, String username, String role) {
        this.stage = primaryStage;
        this.username = username;
        this.role = role;
    }

    public void initializeComponents() {
        stage.setTitle("ADMIN");

        Button manageUsersButton = new Button("Manage Users");
        Button manageAppointmentButton = new Button("Manage Appointments");
        Button logoutButton = new Button("Logout");

        manageUsersButton.setOnAction(actionEvent -> {
            try {
                manageUsers(stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        manageAppointmentButton.setOnAction(actionEvent -> appointmentData());

        logoutButton.setOnAction(e -> AppUtils.logout(stage));


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("WELCOME ADMIN"), 0, 0);
        grid.add(manageUsersButton, 0, 1);
        grid.add(manageAppointmentButton, 1, 1);
        grid.add(logoutButton, 1, 2);
        Scene scene = new Scene(grid, 400, 200);

        stage.setScene(scene);
        stage.show();
    }

    // Method to manage users
    private void manageUsers(Stage stage) throws SQLException {
        String query = "SELECT * FROM users";
        try (Connection con = DBUtils.establishConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Create a new GridPane for displaying user data
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));

            // Add headers for the user list
            grid.add(new Label("Name"), 0, 0);
            grid.add(new Label("Phone"), 1, 0);
            grid.add(new Label("Role"), 2, 0);
            grid.add(new Label("Actions"), 3, 0);

            int row = 1;
            while (rs.next()) {
                int uID = rs.getInt("uID");
                String name = rs.getString("Name");
                String phone = rs.getString("Phone");
                String role = rs.getString("Role");

                Label nameLabel = new Label(name);
                Label phoneLabel = new Label(phone);
                Label roleLabel = new Label(role);
                Button deleteButton = new Button("Delete");

                // Delete user action
                deleteButton.setOnAction(e -> {
                    try {
                        deleteUser(uID, grid);
                    } catch (SQLException ex) {
                        AppUtils.showAlert("Error", "Failed to delete user.");
                    }
                });

                // Add user details and delete button to the grid
                grid.add(nameLabel, 0, row);
                grid.add(phoneLabel, 1, row);
                grid.add(roleLabel, 2, row);
                grid.add(deleteButton, 3, row);

                row++;
            }

            // Button to add a new user
            Button addUserButton = new Button("Add New User");
            addUserButton.setOnAction(e -> {
                try {
                    new newUser().start(new Stage());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            grid.add(addUserButton, 0, row);

            Button backButton = new Button("Back");
            backButton.setOnAction(e -> initializeComponents());
            grid.add(backButton, 1, row);

            Scene scene = new Scene(grid, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (SQLException e) {
            AppUtils.showAlert("Database Error", "Unable to retrieve users.");
        }
    }

    // Method to delete a user
    private void deleteUser(int uID, GridPane grid) throws SQLException {
        String query = "DELETE FROM `users` WHERE `uID` = ?";
        try (Connection con = DBUtils.establishConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, uID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                AppUtils.showAlert("Success", "User deleted successfully.");
                grid.getChildren().clear();
                try {
                    manageUsers(stage); // Refresh the user list after deletion
                } catch (SQLException e) {
                    AppUtils.showAlert("Error", "Failed to refresh user list.");
                }
            } else {
                AppUtils.showAlert("Error", "Failed to delete user.");
            }

        } catch (SQLException e) {
            AppUtils.showAlert("Error", "Failed to delete user.");
        }
    }

    // Method to show appointment data
    private void appointmentData() {
        String SQL = "SELECT * FROM appointments";
        try (Connection con = DBUtils.establishConnection();
             Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = statement.executeQuery(SQL)) {

            if (rs.next()) {
                Display display = new Display(stage, username, rs,role);
                display.displayApp();
            }

        } catch (SQLException e) {
            AppUtils.showAlert("Error", "Database Error: " + e.getMessage());
        }
    }

    // New User registration form
    public static class newUser extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Registration");
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
            grid.add(new Label("Enter Manager details"), 0, 0);
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

    // Add user to the database
    private static void addUser(Stage primaryStage) throws NoSuchAlgorithmException {
        String name = newName.getText();
        String password = newPassword.getText();
        String role = newRole.getText();
        byte[] salt = createSalt();
        String hashPass = generateHash(password, algorithm, salt);
        String phone = newPhone.getText();

        // Regex validation
        String regexRole = "^(Manager|manager|Employee|employee)$";
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
            String query = "INSERT INTO `users` (`Role`, `Name`, `Password`, `Salt`, `Phone`) values(?,?,?,?,?) ;";

            try (Connection con = DBUtils.establishConnection();
                 PreparedStatement statement = con.prepareStatement(query)) {

                statement.setString(1, role);
                statement.setString(2, name);
                statement.setString(3, hashPass);
                statement.setString(4, Arrays.toString(salt));
                statement.setString(5, phone);

                int rs = statement.executeUpdate();
                if (rs == 1) {
                    AppUtils.showAlert("Success", "User Added");
                } else {
                    AppUtils.showAlert("Failure", "Failed to Add User");
                }

                DBUtils.closeConnection(con, statement);
                primaryStage.close();
            } catch (SQLException e) {
                AppUtils.showAlert("Failure", "Failed to connect to Database");
            }
        } else {
            AppUtils.showAlert("Failure", "Invalid Details");
        }
    }

    // Generate salt for password hashing
    public static byte[] createSalt() {
        byte[] bytes = new byte[5];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }

    // Generate hash using the provided algorithm and salt
    public static String generateHash(String data, String algorithm, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(data.getBytes());
        return bytesToHexString(hash);
    }

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Convert bytes to hex string
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}
