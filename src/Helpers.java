import org.apache.commons.validator.routines.EmailValidator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.SQLException;
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

    public static boolean registerCustomer(Connection connection, String name, String password, String email) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        if(!Queries.checkIfNameFree(connection, name)){
            System.out.println("name is taken");
            return false;
        }

        if (!Helpers.passwordValid(password)){
            System.out.println("password invalid");
            return false;
        }

        if (!Helpers.emailValidate(email)){
            System.out.println("email invalid");
            return false;
        }

        if(!Queries.insertUser(connection, name, email)){
            return false;
        }

        int userID = Queries.getUserID(connection, name);
        if (userID == -1){
            System.out.println("could not find user with such name");
            return false;
        }

        byte [] salt = Helpers.generateSalt();
        byte [] passwordHash = Helpers.generateHash(salt, password);

        if(!Queries.insertSaltAndHash(connection, userID, salt, passwordHash)){
            System.out.println("could not insert credentials");
            return false;
        }

        return true;

    }

}
