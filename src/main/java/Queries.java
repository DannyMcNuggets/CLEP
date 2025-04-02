import java.sql.*;

public class Queries {

    private static void prepareParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            switch (params[i]) {
                case Integer integer -> pstmt.setInt(i + 1, integer);
                case String s -> pstmt.setString(i + 1, s);
                case byte[] bytes -> pstmt.setBytes(i + 1, bytes);
                default -> throw new SQLException("Unsupported parameter type: " + params[i].getClass());
            }
        }
    }


    public static ResultSet executeQuery(Connection connection, String query, Object... params) throws SQLException {
        PreparedStatement pstms = connection.prepareStatement(query);
        prepareParams(pstms, params);
        return pstms.executeQuery();

    }


    public static boolean executeUpdate(Connection connection, String query, Object... params) throws SQLException {
        PreparedStatement pstms = connection.prepareStatement(query);
        prepareParams(pstms, params);
        return pstms.executeUpdate() > 0;
    }


    public static boolean insertSaltAndHash(Connection connection, int id,byte [] hash,  byte [] salt) throws SQLException {
        String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
        return executeUpdate(connection, query, id, hash, salt);
    }


    public static boolean insertUser(Connection connection, String name, String email, String role) throws SQLException{
        String query =  "INSERT INTO users (username, email, role) VALUES (?, ?, ?);";
        return executeUpdate(connection, query, name, email, role);
    }


    public static ResultSet getSaltandHash(Connection connection, int id) throws SQLException {
        String query = "SELECT salt, password_hash FROM user_credentials WHERE user_id = ?";
        return executeQuery(connection, query, id);
    }


    public static boolean checkIfNameTaken(Connection connection, String name) throws SQLException {
        String query = "SELECT 1 FROM users WHERE username = ?";
        return executeQuery(connection, query,name).next();
    }


    public static int getUserID(Connection connection, String name) throws SQLException{
        String query = "SELECT id FROM users WHERE username = ?";
        try (ResultSet rs = executeQuery(connection, query, name)){
            return rs.next() ? rs.getInt("id") : -1;
        }
    }


    public static String getUserRole(Connection connection, int customerID) throws SQLException {
        String query = "SELECT role FROM users WHERE id = ?";
        try (ResultSet rs = executeQuery(connection, query, customerID)){
            return rs.next() ? rs.getString("role") : null;
        }

    }


    public static ResultSet getAllOrders(Connection connection) throws SQLException {
        String query = "SELECT * FROM orders";
        return executeQuery(connection, query);
    }


}
