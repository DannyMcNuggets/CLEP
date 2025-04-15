import CLEP.util.Helpers;
import CLEP.util.Queries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelpersTest {

    private Queries queries;
    private Helpers helpers;

    @BeforeEach
    public void setUp() {
        queries = mock(Queries.class);
        helpers = new Helpers(queries);
    }


    @Test
    public void generateSaltTest() {
        byte[] salt = helpers.generateSalt();
        assertEquals(16, salt.length);
    }


    @Test
    public void generateHashTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = helpers.generateSalt();
        byte[] hash1 = helpers.generateHash(salt, "Password123");
        byte[] hash2 = helpers.generateHash(salt, "Password123");
        assertArrayEquals(hash1, hash2);
    }


    @Test
    public void validPasswordTest() {
        assertTrue(Helpers.passwordValid("Qwert321"));
        assertTrue(Helpers.passwordValid("!Ab1#¤(/!#)"));
        assertTrue(Helpers.passwordValid("Aa1" + "x".repeat(27)));
    }


    @Test
    public void invalidPasswordTest() {
        assertFalse(Helpers.passwordValid("12345678"));
        assertFalse(Helpers.passwordValid("abcdefgh"));
        assertFalse(Helpers.passwordValid("Abcdefgh"));
        assertFalse(Helpers.passwordValid("ABCDEF78"));
        assertFalse(Helpers.passwordValid("abcdef78"));
        assertFalse(Helpers.passwordValid("Aa34567"));
        assertFalse(Helpers.passwordValid("Aa1" + "x".repeat(28)));

    }


    @Test
    public void validEmailTest() {
        assertTrue(Helpers.emailValidate("valid@mail.com"));
        assertTrue(Helpers.emailValidate("\"/weird&stuff\"@example.com"));
        assertTrue(Helpers.emailValidate("あいうえお@example.com"));
        assertTrue(Helpers.emailValidate("!#$%&'*+-/=?^_{}|~@example.com"));
        assertTrue(Helpers.emailValidate("user@[192.168.1.1]"));
        assertTrue(Helpers.emailValidate("\"a@b\"@example.com"));
    }


    @Test
    public void invalidEmailTest() {
        assertFalse(Helpers.emailValidate("invalid@email"));
        assertFalse(Helpers.emailValidate("invalid@@mail.com"));
        assertFalse(Helpers.emailValidate("invalid.com"));
        assertFalse(Helpers.emailValidate("”(),:;<>[\\]@example.com"));
        assertFalse(Helpers.emailValidate("this\\ is\"really\"not\\allowed@example.com"));
        assertFalse(Helpers.emailValidate("#@%^%#$@#$@#.com"));
    }


    @Test
    public void productTypeEanTest() {
        assertEquals("ean", Helpers.productType("13482343453"));
        assertEquals("ean", Helpers.productType("1"));

    }


    @Test
    public void productTypeNameTest() {
        assertEquals("name", Helpers.productType("ThisIsAText"));
        assertEquals("name", Helpers.productType("A123456"));
        assertEquals("name", Helpers.productType("a"));
    }


    @Test
    void byteToStringTest() {
        byte[] byteArray = "testvalue".getBytes();
        String expectedHex = "7465737476616c7565"; // "testvalue" in hex
        assertEquals(expectedHex, Helpers.byteToString(byteArray));
    }


    @Test
    public void verifyPasswordSuccess() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        int userID = 1;
        String password = "ValidPassword123";
        byte[] salt = helpers.generateSalt();
        byte[] hash = helpers.generateHash(salt, password);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getBytes("salt")).thenReturn(salt);
        when(rs.getBytes("password_hash")).thenReturn(hash);

        when(queries.getSaltandHash(userID)).thenReturn(rs);

        assertTrue(helpers.verifyPassword(userID, password));
    }


    @Test
    public void verifyPasswordFail() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        int userID = 1;
        String correctPassword = "ValidAndCorrectPass123";
        String wrongPassword = "ValidAndWrongPass123";

        byte[] salt = helpers.generateSalt();
        byte[] hash = helpers.generateHash(salt, correctPassword);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getBytes("salt")).thenReturn(salt);
        when(rs.getBytes("password_hash")).thenReturn(hash);

        when(queries.getSaltandHash(userID)).thenReturn(rs);

        assertFalse(helpers.verifyPassword(userID, wrongPassword));
    }

    /* TODO:
    @Test rsToString(){
        mock result set
        when metadata then expected result
        <...>
    }   assertEqual("prepared string")
     */

}

