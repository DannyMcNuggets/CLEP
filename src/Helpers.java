import org.apache.commons.validator.routines.EmailValidator;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

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

    public static boolean passwordValid(String password){
        String re = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,30}$";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // TODO: validate if entered password is equal to SALT+PASSWORD = HASH from DB

}
