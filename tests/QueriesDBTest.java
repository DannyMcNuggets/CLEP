import CLEP.util.Queries;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashSet;
import java.util.Random;

public class QueriesDBTest {

    private static final String TEST_DB_PATH ="test_database.db";
    private static final File dbFile = new File(TEST_DB_PATH);
    private static final String scriptSQL = "dbscript.sql";
    private static Connection connection;

    @BeforeClass
    static void setUp() throws SQLException, IOException {

        if (dbFile.exists()) { dbFile.delete();}

        boolean created = dbFile.createNewFile();
        if (created){
            System.out.println("Database created with file: " + dbFile.getAbsolutePath());
        } else {
            System.out.println("Failed to created DB.");
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB_PATH);

        String sql = Files.readString(Path.of(scriptSQL));

        try (Statement stmt = connection.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }


    @Test
    public void executeQueryTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            String query = "INSERT INTO users (username, email, role) VALUES ('test_user', 'test_user@gmail.com', 'admin')";
            connectionMem.createStatement().executeUpdate(query);

            Queries queries = new Queries(connectionMem);
            ResultSet rs = queries.executeQuery("SELECT username FROM users WHERE id = ?", 1);
            Assert.assertTrue(rs.next(), "We expected a result");
            Assert.assertEquals(rs.getString("username"), "test_user", "Result did not match");
        }
    }


    @Test
    public void executeUpdateInsertSuccessTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            String username = "test_user";
            String email = "test_user@example.com";
            String role = "customer";

            String insertQuery = "INSERT INTO users (username, email, role) VALUES (?, ?, ?)";

            boolean result = queries.executeUpdate(insertQuery, username, email, role);

            connectionMem.close();
            Assert.assertTrue(result, "Insert unsuccessful");
        }
    }


    @Test
    public void executeUpdateInsertCorrectDataTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            String username = "test_user";
            String email = "test_user@example.com";
            String role = "customer";

            String insertQuery = "INSERT INTO users (username, email, role) VALUES (?, ?, ?)";
            queries.executeUpdate(insertQuery, username, email, role);

            String selectQuery = "SELECT username, email, role FROM users WHERE username = ?";
            try (PreparedStatement pstms = connectionMem.prepareStatement(selectQuery)) {
                pstms.setString(1, username);
                ResultSet rs = pstms.executeQuery();

                Assert.assertTrue(rs.next());
                Assert.assertEquals(rs.getString("username"), username);
                Assert.assertEquals(rs.getString("email"), email);
                Assert.assertEquals(rs.getString("role"), role);
            }
        }
    }


    @Test
    public void executeUpdateFailTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);
            String username = "non-existent";
            String query = "UPDATE users SET role = 'admin' WHERE username = ?;";
            boolean result = queries.executeUpdate(query, username);
            Assert.assertFalse(result);
        }
    }


    @Test
    public void executeInsertAndReturnKeyTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            String username = "test_user";
            String email = "test_user@example.com";
            String role = "customer";

            String query = "INSERT INTO users (username, email, role) VALUES (?, ?, ?)";
            int id = queries.executeInsertAndReturnKey(query, username, email, role);

            Assert.assertEquals(id, 1);
        }
    }


    @Test
    public void executeInsertAndReturnKeyFailTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            String username = "test_user";
            String email = "test_user@example.com";
            String role = "customer";

            String query = "INSERT INTO users (username, email, role) SELECT ?, ?, ? WHERE 1 = 0";
            int id = queries.executeInsertAndReturnKey(query, username, email, role);

            Assert.assertEquals(id, 0);
        }
    }


    @Test
    public void insertSaltAndHashTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            createUserCredentialsTable(connectionMem);

            Queries queries = new Queries(connectionMem);
            int userId = queries.insertUser("testuser", "test@example.com", "customer");
            byte[] hash = {1, 2, 3};
            byte[] salt = {9, 8, 7};

            boolean result = queries.insertSaltAndHash(userId, hash, salt);
            Assert.assertTrue(result);
        }
    }


    @Test
    public void getSaltAndHashTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            createUserCredentialsTable(connectionMem);

            Queries queries = new Queries(connectionMem);
            int userId = queries.insertUser("testuser", "test@example.com", "customer");

            byte[] expectedHash = {1, 2, 3};
            byte[] expectedSalt = {9, 8, 7};
            queries.insertSaltAndHash(userId, expectedHash, expectedSalt);

            ResultSet rs = queries.getSaltandHash(userId);
            Assert.assertTrue(rs.next());
            Assert.assertEquals(expectedHash, rs.getBytes("password_hash"));
            Assert.assertEquals(expectedSalt, rs.getBytes("salt"));
        }
    }


    @Test
    public void checkIfNameFreeTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            boolean result = queries.checkIfNameFree("new_user");
            Assert.assertTrue(result, "Expected the username to be available.");
        }
    }


    @Test
    public void checkIfNameFreeFalseTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()) {
            createUsersTable(connectionMem);
            Queries queries = new Queries(connectionMem);

            try (PreparedStatement pstmt = connectionMem.prepareStatement("""
            INSERT INTO users (username, email, role) VALUES (?, ?, ?)
        """)) {
                pstmt.setString(1, "existing_user");
                pstmt.setString(2, "user@example.com");
                pstmt.setString(3, "customer");
                pstmt.executeUpdate();
            }

            boolean result = queries.checkIfNameFree("existing_user");
            Assert.assertFalse(result, "Expecte name to be taken");
        }
    }

    @Test
    public void updateViewsTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            Queries queries = new Queries(connectionMem);
            createProductViews(connectionMem);
            HashSet<Integer> lookedNumbers = new HashSet<>();
            lookedNumbers.add(1);
            boolean inserted = queries.updateViews(lookedNumbers);
            Assert.assertTrue(inserted);

            String query = "SELECT * from product_views WHERE product_id = 1";
            ResultSet rs = connectionMem.prepareStatement(query).executeQuery();
            rs.next();
            Assert.assertEquals(rs.getInt("product_id"), 1);
            Assert.assertEquals(rs.getInt("total_views"), 1);
            Assert.assertNotNull(rs.getTime("latest_view_date"));
        }
    }

    @Test
    public void updateViewsMultipleTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            Queries queries = new Queries(connectionMem);
            createProductViews(connectionMem);
            HashSet<Integer> lookedNumbers = new HashSet<>();
            lookedNumbers.add(1);
            int repetiotions = 5;
            for (int i = 0; i < repetiotions; i++){
                boolean inserted = queries.updateViews(lookedNumbers);
                Assert.assertTrue(inserted);
            }

            String query = "SELECT * from product_views WHERE product_id = 1";
            ResultSet rs = connectionMem.prepareStatement(query).executeQuery();
            rs.next();
            Assert.assertEquals(rs.getInt("product_id"), 1);
            Assert.assertEquals(rs.getInt("total_views"), repetiotions);
            Assert.assertNotNull(rs.getTime("latest_view_date"));
        }
    }

    @Test //TODO: partial match
    public void lookUpProductTest() throws SQLException {
        try (Connection connectionMem = createInMemoryConnection()){
            Queries queries = new Queries(connectionMem);
            createProducts(connectionMem);

            String name = "product";
            String description = "keyword the description";
            BigDecimal price = BigDecimal.valueOf(10);
            int stock = 5;
            String ean = "123456789";
            populateProducts(connectionMem, name, description, price, stock, ean);

            ResultSet rs1 = queries.lookUpProduct(name);
            rs1.next();
            Assert.assertEquals(rs1.getString("ean"), ean, "Not found by name");

            ResultSet rs2 = queries.lookUpProduct("key");
            rs2.next();
            Assert.assertEquals(rs2.getString("ean"), ean, "Not found by description");
        }


    }


    @Test
    public void getAllOrdersTest() throws SQLException {
        try(Connection connectionMem = createInMemoryConnection()){
            Queries queries = new Queries(connectionMem);
            createOrders(connectionMem);

            int[] userIDs = new Random()
                    .ints(10, 1, 13)
                    .toArray();

            for (int userID : userIDs) {
                try (PreparedStatement pstmt = connectionMem.prepareStatement("""
                          INSERT INTO orders (user_id) VALUES (?);
                        """)) {
                    pstmt.setInt(1, userID);
                    pstmt.executeUpdate();
                }
            }

            ResultSet rs = queries.getAllOrders();

            int i = 0;
            while (rs.next()) {
                Assert.assertTrue(i < userIDs.length, "Received more values than inserted");
                Assert.assertEquals(userIDs[i], rs.getInt("user_id"), "IDs and orders do not match");
                i++;
            }
            Assert.assertEquals(i, userIDs.length, "Not all inserted values were returned");
        }
    }


    @Test
    public void deductStockTest() throws SQLException {
        try(Connection connectionMem = createInMemoryConnection()){
            Queries queries = new Queries(connectionMem);
            createProducts(connectionMem);

            String name = "product";
            String description = "keyword the description";
            BigDecimal price = BigDecimal.valueOf(10);
            int stock = 5;
            String ean = "123456789";
            populateProducts(connectionMem, name, description, price, stock, ean);
            queries.deductStock(3, 1);

            String query = "SELECT stock from products WHERE id = 1";
            ResultSet rs = connectionMem.prepareStatement(query).executeQuery();
            rs.next();
            Assert.assertEquals(rs.getInt("stock"), 2);
        }
    }

    // TODO: add the last ones




    @Test
    public void dbFileExists() {
        assert dbFile.exists();
    }

    @Test
    public void establishConnection() throws SQLException {
        Assert.assertTrue(connection.isValid(3));
    }

    @Test
    public void checkSchema() throws SQLException, IOException {
        StringBuilder sb = new StringBuilder("");

        String query = "SELECT sql FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String tableSQL = rs.getString("sql");
                sb.append(tableSQL + "; ");
            }
        }

        String sql = Files.readString(Path.of(scriptSQL));
        String expected = sql
                .replaceAll("(?i)CREATE TABLE IF NOT EXISTS", "CREATE TABLE")
                .replaceAll("\"", "")
                .replaceAll("\\s+", " ")
                .trim();

        String actual = sb.toString()
                .replaceAll("\"", "")
                .replaceAll("\\s+", " ")
                .trim();

        Assert.assertEquals(actual, expected);
    }


    @AfterClass
    static void tearDown() throws SQLException {
        System.out.println("we are in afterclass");
        connection.close();
        dbFile.delete();
    }


    private Connection createInMemoryConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite::memory:");
    }


    private void populateProducts(Connection connectionMem, String name, String description, BigDecimal price, int stock, String ean) throws SQLException {
        try (PreparedStatement pstmt = connectionMem.prepareStatement("""
            INSERT into products (name, description, price, stock, ean)
            VALUES (?, ?, ?, ?, ?)
        """)){
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setBigDecimal(3, price);
            pstmt.setInt(4, stock);
            pstmt.setString(5, ean);
            pstmt.executeUpdate();
        }
    }


    private void createUsersTable(Connection conn) throws SQLException {
        conn.createStatement().executeUpdate("""
        CREATE TABLE users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT UNIQUE NOT NULL,
              email TEXT NOT NULL,
              role TEXT NOT NULL CHECK(role IN ('admin', 'employee', 'customer')),
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """);
    }

    private void createUserCredentialsTable(Connection conn) throws SQLException {
        conn.createStatement().executeUpdate("""
        CREATE TABLE user_credentials (
            user_id INTEGER PRIMARY KEY,
            password_hash BLOB NOT NULL,
            salt BLOB NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
        """);
    }

    private void createProductViews(Connection conn) throws SQLException {
        conn.createStatement().executeUpdate("""
        CREATE TABLE product_views (
              product_id INTEGER PRIMARY KEY,
              total_views INTEGER DEFAULT 0,
              latest_view_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
          );
        """);
    }

    public void createProducts(Connection conn) throws SQLException {
        conn.createStatement().executeUpdate("""
        CREATE TABLE products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT,
            price REAL NOT NULL,
            stock INTEGER NOT NULL,
            ean TEXT NOT NULL
        );
    """);
    }

    public void createOrders(Connection conn) throws SQLException{
        conn.createStatement().executeUpdate("""
        CREATE TABLE orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """);
    }
}
