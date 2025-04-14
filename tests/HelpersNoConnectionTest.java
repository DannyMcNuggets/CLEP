import CLEP.util.Helpers;
import org.junit.jupiter.api.Test;

import static org.testng.AssertJUnit.*;

public class HelpersNoConnectionTest {

    @Test
    void testGenerateSalt(){
        byte [] salt = Helpers.generateSalt();
        assertNotNull(salt);
        assertEquals(16, salt.length);
    }

    @Test
    void testGenerateSaltRandomness() {
        byte[] salt1 = Helpers.generateSalt();
        byte[] salt2 = Helpers.generateSalt();
        assertFalse(java.util.Arrays.equals(salt1, salt2));
    }

    @Test
    void testByteToString() {
        byte[] byteArray = "testvalue".getBytes();
        String expectedHex = "7465737476616c7565"; // "testvalue" in hex
        assertEquals(expectedHex, Helpers.byteToString(byteArray));
    }

    @Test
    void testEmailValidateValidCases() {
        assertTrue(Helpers.emailValidate("test@mail.com"));
        assertTrue(Helpers.emailValidate("example.ofreally+weird_email@uk.com"));
    }

    @Test
    void testEmailValidateInvalidCases() {
        assertFalse(Helpers.emailValidate("invalidemail"));
        assertFalse(Helpers.emailValidate("@emptyuser.com"));
        assertFalse(Helpers.emailValidate("emptydomain@.com"));
        assertFalse(Helpers.emailValidate("missing@com"));
    }

    @Test
    void testPasswordValidCases() {
        assertTrue(Helpers.passwordValid("StrongPass1"));
        assertTrue(Helpers.passwordValid("V@l!d123Päšs"));
    }

    @Test
    void testPasswordInvalidCases() {
        assertFalse(Helpers.passwordValid("short"));
        assertFalse(Helpers.passwordValid("nouppercase1"));
        assertFalse(Helpers.passwordValid("NOLOWERCASE1"));
        assertFalse(Helpers.passwordValid("NoNumber"));
        assertFalse(Helpers.passwordValid("12345678"));
    }

    @Test
    void productTypeEan(){
        assertEquals("ean", Helpers.productType("56902716"));
    }

    @Test
    void productTypeText(){
        assertEquals("name", Helpers.productType("Product Name"));
    }

}
