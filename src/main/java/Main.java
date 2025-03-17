import java.sql.*;

public class Main {
    private static Connection connection;
    public static void main(String[] args) throws SQLException {

        System.out.println("Hello, World!");
        String db = "database.db";
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        Statement statement = connection.createStatement();
        String script = "INSERT INTO orders (user_id, order_date) VALUES (17, CURRENT_TIMESTAMP);";
        //statement.executeUpdate(script);
        ResultSet resultSet = statement.executeQuery("SELECT * FROM orders");
        while (resultSet.next()) {
            int userId = resultSet.getInt("user_id");
            String orderDate = resultSet.getString("order_date");
            System.out.println(userId + " : " + orderDate);
        }
        statement.close();
    }
}