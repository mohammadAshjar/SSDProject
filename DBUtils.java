import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

class DBUtils {
    private static String url = "jdbc:mysql://localhost:3306/project";
    private static String appUsername = "root";
    private static String appPassword = "";

    public static Connection establishConnection() {
        Connection con = null;
        try{
            con = DriverManager.getConnection(url, appUsername, appPassword);
            System.out.println("Connection Successful");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return con;
    }

    public static void closeConnection(Connection con, Statement stmt){
        try {
            stmt.close();
            con.close();
            System.out.println("Connection is closed");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}

