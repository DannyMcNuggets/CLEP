import org.apache.commons.validator.routines.EmailValidator;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HexFormat;

public class Helpers {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "qwerty321";
        byte [] salt = generateSalt();
        byte [] hashed = generateHash(salt, password);
    }

    public static byte [] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public static byte [] generateHash(byte [] salt, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    public static String byteToString(byte [] array){
        return HexFormat.of().formatHex(array);
    }

    public static boolean emailValidate(String EmailAddress){
        return EmailValidator.getInstance().isValid(EmailAddress);
    }

}
