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


    public static boolean emailValidate(String email){
        return EmailValidator.getInstance().isValid(email);
        //String re = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
    }


    public static boolean passwordValid(String password){
        String re = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,30}$";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    public boolean verifyPassword(int userID, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (ResultSet rs = queries.getSaltandHash(userID)){
            if (!rs.next()){
                return false;
            }

            byte [] salt = rs.getBytes("salt");
            byte [] storedHash = rs.getBytes("password_hash");
            byte [] generateHash = generateHash(salt, password);
            return Arrays.equals(generateHash, storedHash);
        }
    }


    public static String productType(String product){
        if (product != null && product.matches("\\d+")) return "ean";
        else return "name";
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
