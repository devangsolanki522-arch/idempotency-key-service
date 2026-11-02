import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {

    public static void main(String[] args) {
        try {
            Connection connection = DB.getConnection();
            System.out.println("Database connected!");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}