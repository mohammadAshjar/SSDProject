import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserLogin {
    private static String algorithm = "SHA-256";
    private Scene loginScene;
    private static TextField usernameField = new TextField();
    private static PasswordField passwordField = new PasswordField();
    private static Stage stage;
    private static Label label;

    public UserLogin(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void initializeComponents() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));
        Button loginButton = new Button("Sign In");
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                authenticate();
            }
        });
        loginLayout.getChildren().addAll(new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                loginButton);

        loginScene = new Scene(loginLayout, 300, 200);
        stage.setTitle("User Login");
        stage.setScene(loginScene);
        stage.show();
    }

    private static void authenticate() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Connection con = DBUtils.establishConnection();
        String usernameRegex = "^[A-Za-z ]+$";
        Pattern patternUser = Pattern.compile(usernameRegex);
        Matcher userMatch = patternUser.matcher(username);
        String passRegex = "^[A-Za-z0-9!@#$%^&*_ -]+$";
        Pattern patternPass = Pattern.compile(passRegex);
        Matcher passMatch = patternPass.matcher(password);
        String verificationQuery ="SELECT * FROM users WHERE Name=?;";
        String query = "SELECT * FROM users WHERE Name=? AND Password=?;";
        if(userMatch.matches() && passMatch.matches()){
            try{
                PreparedStatement statement = con.prepareStatement(verificationQuery);
                statement.setString(1,username);
                ResultSet rs = statement.executeQuery();
                if(rs.next()){
                    byte[] salt = toBytes(rs.getString(5));
                    String hashPass = generateHash(password,algorithm,salt);
                    PreparedStatement st = con.prepareStatement(query);
                    st.setString(1, username);
                    st.setString(2, hashPass);
                    ResultSet rs1 = st.executeQuery();
                    if (rs1.next()) {
                        if(rs1.getString(2).equals("Admin") || rs1.getString(2).equals("admin")){
                            Admin admin = new Admin(stage,username,rs1.getString(2));
                            admin.initializeComponents();
                        }
                        if(rs1.getString(2).equals("Employee") || rs1.getString(2).equals("employee")){
                            EmployeePage employeePage = new EmployeePage(stage, username,rs1.getString(2));
                            employeePage.initializeComponents();
                        }
                        if(rs1.getString(2).equals("Manager") || rs1.getString(2).equals("manager")) {
                            ManagerPage managerPage = new ManagerPage(stage, username,rs1.getString(2));
                            managerPage.initializeComponents();
                        }
                    } else {
                        AppUtils.showAlert("Authentication Failed", "Invalid username or password.");
                }}
                DBUtils.closeConnection(con, statement);
            } catch (Exception e) {
                AppUtils.showAlert("Database Error", "Failed to connect to the database.");
            }}
        else{
            AppUtils.showAlert("Authentication Failed","Invalid username or password.");
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
    public static byte[] toBytes(String data){
        // Remove the brackets
        data = data.substring(1, data.length() - 1);

        // Split the string by commas
        String[] parts = data.split(",");

        // Create a byte array with the same length as the number of parts
        byte[] byteArray = new byte[parts.length];

        // Convert each part to a byte and store it in the byte array
        for (int i = 0; i < parts.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(parts[i].trim());
        }

        return byteArray;

        }

}
