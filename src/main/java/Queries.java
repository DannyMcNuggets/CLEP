import java.sql.*;

public class Queries {

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
    public static ResultSet executeQuery(Connection connection, String query) throws SQLException {
       Statement st = connection.createStatement();
       return st.executeQuery(query);
    }

    // INSERT, UPDATE.      // change int to boolean (?)
    public static int executeInsertOrUpdate(Connection connection, String query) throws SQLException {
        Statement st = connection.createStatement();
        return st.executeUpdate(query);
    }

    public static boolean insertSaltAndHash(Connection connection, int id, byte [] salt, byte [] hash) throws SQLException {

        String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setBytes(2, hash);
            pstmt.setBytes(3, salt);

            int rowsAffected =  pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean insertUser(Connection connection, String name, String email) throws SQLException{

        String query =  "INSERT INTO users (username, email, role) VALUES (?, ?, 'customer');";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return true;
            }
        }

        return false;
    }

    public static ResultSet getSaltandHash(Connection connection, int id){

        String query = "SELECT salt, password_hash FROM user_credentials WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, id);
            return pstmt.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkIfNameFree(Connection connection, String name) throws SQLException {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next(); // return false if name already exists
            }
        }
    }

    public static int getUserID(Connection connection, String name) {

        String query = "SELECT id FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);  // Set the username parameter
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO
    private static String resultSetToJson(ResultSet resultSet) {
       return null;
    }
}
