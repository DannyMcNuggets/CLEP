package CLEP.util;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Queries {

    private final Connection connection;

    public Queries(Connection connection) {
        this.connection = connection;
    }

    public static void prepareParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            switch (params[i]) {
                case Integer integer -> pstmt.setInt(i + 1, integer);
                case String s -> pstmt.setString(i + 1, s);
                case BigDecimal bg -> pstmt.setBigDecimal(i + 1, bg);
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


    // TODO: log and handle sql exceptions
    public boolean executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstms = connection.prepareStatement(query);
        prepareParams(pstms, params);
        return pstms.executeUpdate() > 0;
    }


    public int executeInsertAndReturnKey(String query, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            prepareParams(pstmt, params);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return 0;
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }


    public boolean insertSaltAndHash(int id, byte [] hash, byte [] salt) throws SQLException {
        String query = "INSERT INTO user_credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
        return executeUpdate(query, id, hash, salt);
    }


    public int insertUser(String name, String email, String role) throws SQLException{
        String query =  "INSERT INTO users (username, email, role) VALUES (?, ?, ?);";
        return executeInsertAndReturnKey(query, name, email, role);
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


    public boolean updateViews(HashSet<Integer> lookedNumbers) throws SQLException{
        if (lookedNumbers.isEmpty()) return false;

        StringBuilder query = new StringBuilder("""
                INSERT INTO product_views (product_id, total_views, latest_view_date) VALUES 
            """);

        String values = lookedNumbers.stream()
                .map(id -> "(?, 1, CURRENT_TIMESTAMP)")
                .collect(Collectors.joining(", "));

        query.append(values);
        query.append("""
            ON CONFLICT(product_id) DO UPDATE SET
            total_views = total_views + 1,
            latest_view_date = CURRENT_TIMESTAMP
        """);

        Object[] params = lookedNumbers.toArray();

        return executeUpdate(query.toString(), params);
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

    public boolean addProduct(String name, String description, BigDecimal price, int stock, String ean) throws SQLException {
        String query = "INSERT INTO products (name, description, price, stock, ean) VALUES (?, ?, ?, ?, ?)";
        return executeUpdate(query, name, description, price, stock, ean);
    }


    public int insertOrder(int userID) throws SQLException {
        String query = "INSERT INTO orders (user_id) VALUES (?)";
        return executeInsertAndReturnKey(query, userID);
    }


    public boolean insertOrderItem(int orderID, int itemID, int amount) throws SQLException {
        String query = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?);";
        return executeUpdate(query, orderID, itemID, amount);
    }


    public ResultSet viewTopProducts() throws SQLException {
        String query = "SELECT products.id AS code, products.name, products.ean, SUM(order_items.quantity) " +
                "AS total_ordered " +
                "FROM order_items " +
                "JOIN products ON order_items.product_id = products.id " +
                "GROUP BY products .id, products .name, products .ean " +
                "ORDER BY total_ordered DESC;";
        return executeQuery(query);
    }

}
