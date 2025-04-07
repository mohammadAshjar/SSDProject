import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
    private String username,role;
    private static String algorithm = "SHA-256";
    private static TextField newName, newRole, newPassword, newPhone;


    public ManagerPage(Stage stage, String username, String role) {
        this.stage = stage;
        this.username = username;
        this.role = role;
    }

    public void initializeComponents() {
        stage.setTitle("Manager Dashboard - " + username);

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> AppUtils.logout(stage));


        Label welcomeLabel = new Label("Welcome, Manager " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button viewReportsBtn = new Button("View Reports");
        Button manageEmployeesBtn = new Button("Manage Employees");
        Button viewAppointmentButton = new Button("View Appointments");

        viewReportsBtn.setOnAction(e -> showReportPage());
        viewAppointmentButton.setOnAction(actionEvent -> appointmentData());
        manageEmployeesBtn.setOnAction(e -> {
            ManageEmployee manageEmployee = new ManageEmployee();
            try {
                manageEmployee.start(stage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        Button manageSparePartsBtn = new Button("Manage Spare Parts");
        manageSparePartsBtn.setOnAction(e -> {
            ManageSpareParts manageSpareParts = new ManageSpareParts();
            try {
                manageSpareParts.start(stage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        VBox layout = new VBox(15, welcomeLabel,viewAppointmentButton, viewReportsBtn, manageEmployeesBtn, manageSparePartsBtn, logoutButton);
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
            AppUtils.showAlert("Error",e.getMessage());
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> reportStage.close());
        grid.add(backButton, 0, grid.getChildren().size());

        Scene scene = new Scene(grid, 600, 400);
        reportStage.setScene(scene);
        reportStage.show();
    }

    public class ManageSpareParts extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Manage Spare Parts");
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);

            Label title = new Label("Spare Parts Inventory");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            grid.add(title, 0, 0, 3, 1);

            try (Connection con = DBUtils.establishConnection()) {
                String sql = "SELECT * FROM spare_parts";
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                int row = 1;
                while (rs.next()) {
                    int id = rs.getInt("part_id");
                    String name = rs.getString("part_name");
                    int price = rs.getInt("price");
                    int quantity = rs.getInt("quantity");

                    Label info = new Label("ID: " + id + ", Name: " + name + ", Price: " + price + ", Qty: " + quantity);
                    Button deleteBtn = new Button("Delete");
                    Button updateBtn = new Button("Update");

                    deleteBtn.setOnAction(e -> deletePart(id, stage));
                    updateBtn.setOnAction(e -> openUpdateDialog(id, stage));

                    grid.add(info, 0, row);
                    grid.add(updateBtn, 1, row);
                    grid.add(deleteBtn, 2, row);
                    row++;
                }
            } catch (SQLException e) {
                AppUtils.showAlert("Error", e.getMessage());
            }

            Button back = new Button("Back");
            back.setOnAction(e -> new ManagerPage(stage, "Manager", "Manager").initializeComponents());
            grid.add(back, 0, 20);

            Button addPartButton = new Button("Add Spare Part");
            addPartButton.setOnAction(e -> openAddPartDialog(stage));

            grid.add(addPartButton, 1, 20);

            Scene scene = new Scene(grid, 600, 400);
            stage.setScene(scene);
            stage.show();
        }

        private void openAddPartDialog(Stage stage) {
            Stage dialog = new Stage();
            dialog.setTitle("Add Spare Part");

            GridPane grid = new GridPane();
            grid.setPadding(new Insets(10));
            grid.setVgap(10);
            grid.setHgap(10);

            TextField nameField = new TextField();
            TextField priceField = new TextField();
            TextField qtyField = new TextField();

            grid.add(new Label("Part Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Price:"), 0, 1);
            grid.add(priceField, 1, 1);
            grid.add(new Label("Quantity:"), 0, 2);
            grid.add(qtyField, 1, 2);

            Button addButton = new Button("Add");
            addButton.setOnAction(e -> {
                String name = nameField.getText();
                String priceStr = priceField.getText();
                String qtyStr = qtyField.getText();

                if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                    AppUtils.showAlert("Input Error", "All fields must be filled.");
                    return;
                }
                String regexName = "^[A-Za-z_ -]*$";
                String regexPrice = "^\\d*$";
                String regexQty = "^\\d*$";
                Pattern patternName = Pattern.compile(regexName);
                Pattern patternPrice = Pattern.compile(regexPrice);
                Pattern patternQty = Pattern.compile(regexQty);
                Matcher matcherName = patternName.matcher(name);
                Matcher matcherPrice = patternPrice.matcher(priceStr);
                Matcher matcherQty = patternQty.matcher(qtyStr);
                if(matcherPrice.matches()&& matcherName.matches() && matcherQty.matches()){
                    try {
                        int price = Integer.parseInt(priceStr);
                        int quantity = Integer.parseInt(qtyStr);

                        try (Connection con = DBUtils.establishConnection()) {
                            String sql = "INSERT INTO spare_parts (part_name, price, quantity) VALUES (?, ?, ?)";
                            PreparedStatement stmt = con.prepareStatement(sql);
                            stmt.setString(1, name);
                            stmt.setInt(2, price);
                            stmt.setInt(3, quantity);
                            int result = stmt.executeUpdate();

                            if (result > 0) {
                                AppUtils.showAlert("Success", "Spare part added.");
                                dialog.close();
                                start(stage); // refresh view
                            } else {
                                AppUtils.showAlert("Failure", "Could not add part.");
                            }
                        }

                } catch (NumberFormatException nfe) {
                        AppUtils.showAlert("Input Error", "Price and Quantity must be valid numbers.");
                } catch (Exception ex) {
                        AppUtils.showAlert("Error", ex.getMessage());
                }}
                else{
                    AppUtils.showAlert("Error","Invalid Input");
                }
            });

            grid.add(addButton, 1, 3);

            Scene scene = new Scene(grid, 350, 250);
            dialog.setScene(scene);
            dialog.show();
        }

        private void deletePart(int partId, Stage stage) {
            try (Connection con = DBUtils.establishConnection()) {
                String query = "DELETE FROM spare_parts WHERE part_id = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, partId);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    AppUtils.showAlert("Success", "Spare part deleted.");
                    start(stage); // refresh
                } else {
                    AppUtils.showAlert("Failed", "Could not delete part.");
                }
            } catch (Exception e) {
                AppUtils.showAlert("Error", e.getMessage());
            }
        }

        private void openUpdateDialog(int partId, Stage stage) {
            Stage dialog = new Stage();
            dialog.setTitle("Update Part");

            GridPane grid = new GridPane();
            grid.setPadding(new Insets(10));
            grid.setVgap(10);
            grid.setHgap(10);

            TextField priceField = new TextField();
            TextField qtyField = new TextField();

            grid.add(new Label("New Price:"), 0, 0);
            grid.add(priceField, 1, 0);
            grid.add(new Label("New Quantity:"), 0, 1);
            grid.add(qtyField, 1, 1);

            Button updateBtn = new Button("Update");
            updateBtn.setOnAction(e -> {
                try {
                    int newPrice = Integer.parseInt(priceField.getText());
                    int newQty = Integer.parseInt(qtyField.getText());
                    String regexPrice = "^\\d*$";
                    String regexQty = "^\\d+$";
                    Pattern patternPrice = Pattern.compile(regexPrice);
                    Pattern patternQty = Pattern.compile(regexQty);
                    Matcher matcherPrice = patternPrice.matcher(priceField.getText());
                    Matcher matcherQty = patternQty.matcher(qtyField.getText());
                    if(matcherPrice.matches() && matcherQty.matches()){
                        try (Connection con = DBUtils.establishConnection()) {
                            String sql = "UPDATE spare_parts SET price = ?, quantity = ? WHERE part_id = ?";
                            PreparedStatement stmt = con.prepareStatement(sql);
                            stmt.setInt(1, newPrice);
                            stmt.setInt(2, newQty);
                            stmt.setInt(3, partId);
                            int result = stmt.executeUpdate();

                            if (result > 0) {
                                AppUtils.showAlert("Success", "Part updated.");
                                dialog.close();
                                start(stage); // refresh
                            } else {
                                AppUtils.showAlert("Failed", "Update failed.");
                            }
                        }
                    }
                    else{
                        AppUtils.showAlert("Error","Wrong Input");
                    }

                    } catch (Exception ex) {
                    AppUtils.showAlert("Error", "Invalid input or DB error.");
                    }
            });

            grid.add(updateBtn, 1, 2);

            Scene scene = new Scene(grid, 300, 200);
            dialog.setScene(scene);
            dialog.show();
        }
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
            backButton.setOnAction(e -> new ManagerPage(stage, "Manager", "Manager").initializeComponents());
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
                AppUtils.showAlert("Error", "Database error: " + e.getMessage());
            }
        }

        public class newEmployee extends Application {

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
                Button backButton = new Button("Back");
                backButton.setOnAction(e -> {
                    try {
                        new ManageEmployee().start(stage);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                grid.add(backButton,0,grid.getChildren().size());
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
                        AppUtils.showAlert("Success", "Employee Added");
                        new ManagerPage(primaryStage, "Manager", "Manager").initializeComponents();
                    } else {
                        AppUtils.showAlert("Failure", "Failed to Add Manager");
                    }
                    DBUtils.closeConnection(con, statement);
                } catch (Exception e) {
                    AppUtils.showAlert("Failure", "Failed to connect to Database");
                }
            } else {
                AppUtils.showAlert("Failure", "Wrong Details");
            }
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