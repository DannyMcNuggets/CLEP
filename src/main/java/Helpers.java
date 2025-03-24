import org.apache.commons.validator.routines.EmailValidator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
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

    // NOT TESTED YET!
    public static boolean verifyPassword(Connection connection, String name, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        int userID = Queries.getUserID(connection, name);
        if (userID == -1){
            System.out.println("could not find user with such name");
            return false;
        }

        try (ResultSet rs = Queries.getSaltandHash(connection, userID)){
            if (!rs.next()){
                System.out.println("no salt and/or hash found for user");
                return false;
            }

            byte [] salt = rs.getBytes("salt");
            byte [] storedHash = rs.getBytes("password_hash");

            byte [] generateHash = generateHash(salt, password);
            return Arrays.equals(generateHash, storedHash);
        }
    }

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
