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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

public class Admin extends Application {
    private Stage stage;
    private static String algorithm = "SHA-256";
    private String username;
    private static TextField newUId, newName, newRole, newPassword, newPhone;
    public Admin(Stage primaryStage, String username){
        this.stage = primaryStage;
        this.username = username;
    }
    public void initializeComponents() {
        stage.setTitle("ADMIN");
        Button registerManager = new Button("Register Manager");
        Button viewAppointment = new Button("View Appointment");
        registerManager.setOnAction(actionEvent -> {
            try {
                new newManager().start(new Stage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("WELCOME ADMIN"),0,0);
        grid.add(registerManager,0,1);
        grid.add(viewAppointment,1,1);
        Scene scene = new Scene(grid, 500, 300);

        stage.setScene(scene);
        stage.show(); // Display the stage

    }
    public static class newManager extends Application{

        @Override
        public void start(Stage stage) throws Exception {
            stage.setTitle("Registeration");
            newName = new TextField();
            newPassword = new TextField();
            newRole = new TextField();
            newPhone = new TextField();
            newUId = new TextField();
            Button registerButton = new Button("Register");

            registerButton.setOnAction(e -> {
                try {
                    addManager(stage);
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
            });
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));
            grid.add(new Label("Enter Manager details"), 0, 0);
            grid.add(new Label("Enter UID:"), 0, 1); //Set the col number 0, and row number 1
            grid.add(newUId, 1, 1); //Set the col number 1, and row number 1
            grid.add(new Label("Enter Role:"), 0, 2);
            grid.add(newRole, 1, 2);
            grid.add(new Label("Enter Name:"),0,3);
            grid.add(newName,1,3);
            grid.add(new Label("Enter Password:"),0,4);
            grid.add(newPassword,1,4);
            grid.add(new Label("Enter your Phone Number:"),0,5);
            grid.add(newPhone,1,5);
            grid.add(registerButton,0,6);
            Scene scene = new Scene(grid, 500, 300);
            stage.setScene(scene);
            stage.show(); // Display the stage
        }
    }
    private static void addManager(Stage primaryStage) throws NoSuchAlgorithmException {
        String name = newName.getText();
        String password = newPassword.getText();
        String role = newRole.getText();
        String phone = newPhone.getText();
        String uId = newUId.getText();
        byte[] salt = createSalt();
        String hashPass = generateHash(password,algorithm,salt);
        String query = "INSERT INTO `users` (`UID`, `Role`, `Name`, `Password`, `Salt`,`Phone Number`) values(?,?,?,?,?,?) ;";
        String regex = "Manager|manager";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(role);
        if(matcher.matches()){
            try{
                Connection con = DBUtils.establishConnection();
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, uId);
                statement.setString(2, role);
                statement.setString(3, name);
                statement.setString(4, hashPass);
                statement.setString(5, Arrays.toString(salt));
                statement.setString(6, phone);
                int rs = statement.executeUpdate();
                if (rs == 1) {
                    showAlert("Success", "Manager Added");
                } else {
                    showAlert("Failure", "Failed to Add Manager");
                }
                DBUtils.closeConnection(con,statement);
                primaryStage.close();
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    else{
        showAlert("Failure","Wrong Role");
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
