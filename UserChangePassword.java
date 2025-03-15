import java.sql.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class UserChangePassword {
    private Scene changePasswordScene;
    private PasswordField newPasswordField = new PasswordField();
    private Stage stage;
    private String username;
    private static String algorithm = "SHA-256";
    public UserChangePassword(Stage primaryStage, String username){
        this.stage = primaryStage;
        this.username = username;
    }

    public void initializeComponents() {
        VBox changePasswordLayout = new VBox(10);
        changePasswordLayout.setPadding(new Insets(10));
        Button changePasswordButton = new Button("Change Password");
        Button logOutButton = new Button("Log Out");
        changePasswordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                //changePassword();
                try {
                    changePassword();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        logOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                UserLogin login = new UserLogin(stage); //primary stage gets replaced with stage
                login.initializeComponents();
            }
        });

        changePasswordLayout.getChildren().addAll(new Label("Welcome " + username), new Label("New Password:"), newPasswordField, changePasswordButton,logOutButton);
        //this will link Layout to scene
        changePasswordScene = new Scene(changePasswordLayout, 300, 200);
        stage.setTitle("Change Password");
        stage.setScene(changePasswordScene);
        stage.show();
    }

    private void changePassword() throws NoSuchAlgorithmException {
        String newPassword = newPasswordField.getText();
        Connection con = DBUtils.establishConnection();
        byte[] salt = createSalt();
        String hashPass = generateHash(newPassword,algorithm,salt);
        String query = "UPDATE users SET password=?,salt=? WHERE username =?;";
        try{
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(2, Arrays.toString(salt));
            statement.setString(1, hashPass);
            statement.setString(3, username);
            int result=statement.executeUpdate();

            if (result == 1) {
                showAlert("Success", "Password successfully changed");
            } else {
                showAlert("Failure", "Failed to update password");
            }
            DBUtils.closeConnection(con, statement);
        }catch(Exception e){
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect to the database.");
        }
    }
    private void showAlert(String title, String content) {
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


