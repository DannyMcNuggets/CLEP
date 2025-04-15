import CLEP.util.Helpers;
import CLEP.util.Queries;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.AssertJUnit.*;

public class HelpersConnectionTest {

    /* UNUSED ANYMORE
    private static final String TEST_DB_PATH ="test_database.db";
    private static final File dbFile = new File(TEST_DB_PATH);
    private static final String scriptSQL = "dbscript.sql";
    private static Connection connection;
    private static Queries queries;

    @BeforeClass
    static void setUp() throws SQLException, IOException {

        if (dbFile.exists()) {
            dbFile.delete();
        }

        boolean created = dbFile.createNewFile();
        if (created){
            System.out.println("Database created with file: " + dbFile.getAbsolutePath());
        } else {
            System.out.println("Failed to created DB.");
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB_PATH);
        if (connection == null){
            System.out.println("damn");
        }

        queries = new Queries(connection);

        executeScript(connection, scriptSQL);
    }


    @AfterClass
    static void tearDown() throws SQLException {
        System.out.println("we are in afterclass");
        connection.close();
        dbFile.delete();
    }


    private static void executeScript(Connection connection, String scriptPath) throws SQLException, IOException {
        String sqlScript = new String(Files.readAllBytes(Paths.get(scriptPath)));

        // Split the script into individual SQL statements by the delimiter (typically semicolon)
        String[] statements = sqlScript.split(";");

        try (Statement stmt = connection.createStatement()) {
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    stmt.execute(statement);
                }
            }
            System.out.println("SQL script executed successfully.");
        }
    }

     */

}
