import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queries {

    private static final String selectAllFromOrders = "SELECT * FROM orders";

    // TODO: get user_id where username == provided name
    /*
    CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('admin', 'employee', 'customer')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
     */

    // TODO: get salt and password_hash where user_id == provided id
    /*
    CREATE TABLE user_credentials (
    user_id INTEGER PRIMARY KEY,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);
     */

    // SELECT
    private static ResultSet executeQuery(Connection connection, String query) throws SQLException {
       Statement st = connection.createStatement();
       return st.executeQuery(query);
    }

    // INSERT, UPDATE.      // change int to boolean (?)
    public static int executeInsertOrUpdate(Connection connection, String query) throws SQLException {
        Statement st = connection.createStatement();
        return st.executeUpdate(query);
    }

    // TODO
    private static String resultSetToJson(ResultSet resultSet) {
       return null;
    }
}
