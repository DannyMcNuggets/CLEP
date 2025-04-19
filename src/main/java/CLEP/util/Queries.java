package CLEP.util;

import java.sql.*;

public class Queries {

    private final Connection connection;

    public Queries(Connection connection) {
        this.connection = connection;
    }

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


    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstms = connection.prepareStatement(query);
        prepareParams(pstms, params);
        return pstms.executeQuery();

    }


    public boolean executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstms = connection.prepareStatement(query);
        prepareParams(pstms, params);
        return pstms.executeUpdate() > 0;
    }


    public boolean insertSaltAndHash(int id, byte [] hash, byte [] salt) throws SQLException {
        String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
        return executeUpdate(query, id, hash, salt);
    }


    public boolean insertUser(String name, String email, String role) throws SQLException{
        String query =  "INSERT INTO users (username, email, role) VALUES (?, ?, ?);";
        return executeUpdate( query, name, email, role);
    }


    public ResultSet getSaltandHash(int id) throws SQLException {
        String query = "SELECT salt, password_hash FROM user_credentials WHERE user_id = ?";
        return executeQuery(query, id);
    }


    public boolean checkIfNameFree(String name) throws SQLException {
        String query = "SELECT 1 FROM users WHERE username = ?";
        return !executeQuery(query,name).next();
    }


    public int getUserID(String name) throws SQLException{
        String query = "SELECT id FROM users WHERE username = ?";
        try (ResultSet rs = executeQuery(query, name)){
            return rs.next() ? rs.getInt("id") : -1;
        }
    }


    public String getUserRole(int customerID) throws SQLException {
        String query = "SELECT role FROM users WHERE id = ?";
        try (ResultSet rs = executeQuery(query, customerID)){
            return rs.next() ? rs.getString("role") : null;
        }
    }


    public ResultSet lookUpProduct(String product) throws SQLException {

        String type = Helpers.productType(product);
        String queryTemplate = "SELECT id, name, description, price, stock, ean FROM products WHERE %s LIKE ?";
        String query;

        switch (type){
            case "ean" -> query = String.format(queryTemplate, "ean");
            case "name" -> query = String.format(queryTemplate, "name");
            default -> {
                return null;
            }
        }

        String wildcardProduct = "%" + product + "%";

        ResultSet rs = executeQuery(query, wildcardProduct);
        if (rs.isBeforeFirst()) {
            return rs;
        }

        String queryByDescription = "SELECT name, description, price, stock, ean FROM products WHERE description LIKE ?";
        return executeQuery(queryByDescription, wildcardProduct);
    }


    public ResultSet getAllOrders() throws SQLException {
        String query = "SELECT * FROM orders";
        return executeQuery(query);
    }

    public boolean deductStock(int amount, int id) throws SQLException {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        return executeUpdate(query, amount, id, amount);
    }


}
