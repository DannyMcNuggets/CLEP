package CLEP.util;

import org.apache.commons.validator.routines.EmailValidator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    private final Queries queries;

    public Helpers(Queries queries){
        this.queries = queries;
    }


    public byte [] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }


    public byte [] generateHash(byte [] salt, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
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

    /*
    public String login(String name, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        // check if userID exists and retrieve it
        int userID = queries.getUserID(name);
        if (userID == -1 ) return null;

        // verify password
        if (verifyPassword(userID, password)){
            // give role
            return queries.getUserRole(userID);
        } else {
            return null;
        }
    }
     */


    public boolean verifyPassword(int userID, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (ResultSet rs = queries.getSaltandHash(userID)){
            if (!rs.next()){
                System.out.println("no salt and/or hash found for user");
                return false;
            }

            System.out.println("salt and hash found");

            byte [] salt = rs.getBytes("salt");
            byte [] storedHash = rs.getBytes("password_hash");
            byte [] generateHash = generateHash(salt, password);
            return Arrays.equals(generateHash, storedHash);
        }
    }

    // TODO: break into small methods that return codes!
    /*
    public static boolean registerUser(Connection connection, String name, String password, String email, String role) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        if(Queries.checkIfNameTaken(connection, name)){
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

        if(!Queries.insertUser(connection, name, email, role)){
            return false;
        }

        int userID = Queries.getUserID(connection, name);
        if (userID == -1){
            System.out.println("could not find user with such name");
            return false;
        }

        byte [] salt = Helpers.generateSalt();
        byte [] passwordHash = Helpers.generateHash(salt, password);

        if(!Queries.insertSaltAndHash(connection, userID, passwordHash, salt)){
            System.out.println("could not insert credentials");
            return false;
        }

        return true;
    }

     */


    public static String productType(String product){
        try {
            Integer.parseInt(product);
            return "ean";
        } catch (NumberFormatException e) {
            return "name";
        }
    }

    public static String rsToString(ResultSet rs) throws SQLException {

        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Append column names
        for (int i = 1; i <= columnCount; i++) {
            sb.append(metaData.getColumnName(i)).append("\t");
        }
        sb.append("\n");

        // Append rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }


    // mb... not sure
    private static String resultSetToJson(ResultSet resultSet) {
        return null;
    }

}
