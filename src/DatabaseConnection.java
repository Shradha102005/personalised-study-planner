import java.sql.*;
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/personalised_study_planner";
    private static final String USER = "root";  
    private static final String PASSWORD = "shradha@10";

    public static Connection getConnection() throws SQLException 
    {
        try 
        {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            throw new SQLException("Connection to database failed!");
        }
    }
}