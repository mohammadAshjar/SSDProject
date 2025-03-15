import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

public class RegisterUser {
    private static String algorithm = "SHA-256";
    private static TextField newUsername, newPassword, newRole, newFirstname, newLastname;
    public static class AddUser extends Application {
        @Override
        public void start(Stage primaryStage) {
            primaryStage.setTitle("Registeration");
            newUsername = new TextField();
            newPassword = new TextField();
            newRole = new TextField();
            newFirstname = new TextField();
            newLastname = new TextField();
            Button registerButton = new Button("Register");
            registerButton.setOnAction(e -> {
                try {
                    addMember(primaryStage);
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            });
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));
            grid.add(new Label("Enter Your details"), 0, 0);
            grid.add(new Label("Enter Your name:"), 0, 1); //Set the col number 0, and row number 1
            grid.add(newUsername, 1, 1); //Set the col number 1, and row number 1
            grid.add(new Label("Enter Password:"), 0, 2);
            grid.add(newPassword, 1, 2);
            grid.add(new Label("Enter your role:"),0,3);
            grid.add(newRole,1,3);
            grid.add(new Label("Enter your First Name:"),0,4);
            grid.add(newFirstname,1,4);
            grid.add(new Label("Enter your Last Name:"),0,5);
            grid.add(newLastname,1,5);
            grid.add(registerButton,0,6);
            Scene scene = new Scene(grid, 500, 300);
            // and the stage (window) encompasses the scene
            primaryStage.setScene(scene);
            primaryStage.show(); // Display the stage
        }
    private static void addMember(Stage primaryStage) throws NoSuchAlgorithmException {
        String username = newUsername.getText();
        String password = newPassword.getText();
        byte[] salt = createSalt();
        String hashPass = generateHash(password,algorithm,salt);
        String role = newRole.getText();
        String firstName = newFirstname.getText();
        String lastName = newLastname.getText();
        String query = "INSERT INTO `users` (`username`, `password`, `role`, `firstname`, `lastname`, `salt`) values(?,?,?,?,?,?) ;";
        try{
            Connection con = DBUtils.establishConnection();
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hashPass);
            statement.setString(3, role);
            statement.setString(4, firstName);
            statement.setString(5, lastName);
            statement.setString(6, Arrays.toString(salt));
            int rs = statement.executeUpdate();
            DBUtils.closeConnection(con,statement);
            primaryStage.close();
        }catch (Exception e){
            System.out.println(e.getMessage());

        }

    }}
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



