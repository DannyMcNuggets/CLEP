import org.junit.jupiter.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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


    /* UNUSED

    private static final String TEST_DB_PATH ="test_database.db";
    private static final File dbFile = new File(TEST_DB_PATH);
    private static final String scriptSQL = "dbscript.sql";

    @BeforeClass
    static void setUp() throws SQLException, IOException {

        if (dbFile.exists()) { dbFile.delete();}
        
        boolean created = dbFile.createNewFile();
        if (created){
            System.out.println("Database created with file: " + dbFile.getAbsolutePath());
        } else {
            System.out.println("Failed to created DB.");
        }
    }

    @Test
    public void assertTrueTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void dbFileExists() {
        assert dbFile.exists();
    }

    @Test
    public void establishConnection() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB_PATH)) {
            Assert.assertTrue(connection.isValid(3));
        }
    }

    @Test
    public void establishConnectionInvalidPath() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:/invalid/path/to/database.db")) {
            Assert.fail("Expected SQLException to be thrown, but connection was established.");
        } catch (SQLException e) {
            Assert.assertTrue(true);
        }
    }

    @AfterClass
    static void tearDown() throws SQLException {
        System.out.println("we are in afterclass");
        dbFile.delete();
    }

     */
}