import java.sql.*;

public class Queries {


    public static boolean insertSaltAndHash(Connection connection, int id, byte [] salt, byte [] hash) throws SQLException {
        String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setBytes(2, hash);
            pstmt.setBytes(3, salt);

            int rowsAffected =  pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }


    public static boolean insertCustomer(Connection connection, String name, String email) throws SQLException{
        String query =  "INSERT INTO users (username, email, role) VALUES (?, ?, 'customer');";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        }
    }


    public static ResultSet getSaltandHash(Connection connection, int id) {
        String query = "SELECT salt, password_hash FROM user_credentials WHERE user_id = ?";
        try { // tws breaks the thing for some reason
            PreparedStatement pstmt = connection.prepareStatement(query);
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


    public static int getUserID(Connection connection, String name) throws SQLException{
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else { return -1;}
            }
        }
    }


    public static String getUserRole(Connection connection, int customerID) throws SQLException {
        String query = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    return rs.getString("role");
                } else { return null;}
            }
        }
    }


}
