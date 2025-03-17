import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {

    private static final String TEST_DB_PATH ="test_database.db";
    private static final String scriptSQL = "dbscript.sql";
    private static Connection connection;

    @BeforeAll
    static void setUp() throws SQLException, IOException {
        File dbFile = new File(TEST_DB_PATH);

        if (dbFile.exists()) {
            dbFile.delete();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB_PATH);

        Statement statement = connection.createStatement();
        String script = Files.readString(Path.of(scriptSQL));
        statement.execute(script);
        System.out.println("Script executed successfully");
    }

    @Test
    public void dbExists() {
        File dbFile = new File(TEST_DB_PATH);
        Assertions.assertTrue(dbFile.exists());
    }

    @Test
    public void firstTest() {
        Assertions.assertTrue(true);
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        connection.close();
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }
}