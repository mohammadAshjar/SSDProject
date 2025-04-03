import com.sun.javafx.stage.EmbeddedWindow;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
    private String username;
    private static TextField newName, newRole, newPassword, newPhone;
    public Admin(Stage primaryStage, String username){
        this.stage = primaryStage;
        this.username = username;
    }
    public void initializeComponents() {
        stage.setTitle("ADMIN");
        Button registerUser = new Button("Register New User");
        Button viewAppointment = new Button("View Appointment");
        Button logoutButton = new Button("Logout");
        registerUser.setOnAction(actionEvent -> {
            try {
                new newUser().start(new Stage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        viewAppointment.setOnAction(actionEvent -> appointmentData());
        logoutButton.setOnAction(e -> {
            new UserLogin(stage).initializeComponents();
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("WELCOME ADMIN"),0,0);
        grid.add(registerUser,0,1);
        grid.add(viewAppointment,1,1);
        grid.add(logoutButton, 1, 2);
        Scene scene = new Scene(grid, 500, 300);

        stage.setScene(scene);
        stage.show();

    }

    private void appointmentData() {
        String SQL = "SELECT * FROM appointments";
        Connection con = DBUtils.establishConnection();
        try {
            Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(SQL);
            if(rs.next()) {
                Display display = new Display(stage,username, rs);
                display.displayApp();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class newUser extends Application{

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
            grid.add(new Label("Enter Manager details"), 0, 0);
            grid.add(new Label("Enter Role:"), 0, 2);
            grid.add(newRole, 1, 2);
            grid.add(new Label("Enter Name:"),0,3);
            grid.add(newName,1,3);
            grid.add(new Label("Enter Password:"),0,4);
            grid.add(newPassword,1,4);
            grid.add(new Label("Enter Phone Number:"),0,5);
            grid.add(newPhone,1,5);
            grid.add(registerButton,0,6);
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
        String hashPass = generateHash(password,algorithm,salt);
        String phone = newPhone.getText();
        String query = "INSERT INTO `users` (`Role`, `Name`, `Password`, `Salt`, `Phone`) values(?,?,?,?,?) ;";
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

        if(matcher.matches() && matcherName.matches() && matcherPass.matches() && matcherPhone.matches()){
            try{
                Connection con = DBUtils.establishConnection();
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, role);
                statement.setString(2, name);
                statement.setString(3, hashPass);
                statement.setString(4, Arrays.toString(salt));
                statement.setString(5,phone);
                int rs = statement.executeUpdate();
                if (rs == 1) {
                    showAlert("Success", "Manager Added");
                } else {
                    showAlert("Failure", "Failed to Add Manager");
                }
                DBUtils.closeConnection(con,statement);
                primaryStage.close();
            }catch (Exception e) {
                showAlert("Failure","Failed to connect to Database");
            }
        }
    else{
        showAlert("Failure","Wrong Details");
        }
    }


    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

    @Override
    public void start(Stage stage) throws Exception {

    }
}
