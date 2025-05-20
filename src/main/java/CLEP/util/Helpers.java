package CLEP.util;

import org.apache.commons.validator.routines.EmailValidator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
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
    private final IOUnit io;
    public Helpers(Queries queries, IOUnit io){
        this.queries = queries;
        this.io = io;
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


    public static String rsToString(ResultSet rs, boolean singleRowOnly) throws SQLException {

        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

       // column headers
        for (int i = 1; i <= columnCount; i++) {
            sb.append(metaData.getColumnName(i)).append("\t");
        }
        sb.append("\n");

        // rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
            if (singleRowOnly) break;
        }

        return sb.toString();
    }


    public String promptString(String message) throws IOException {
        io.write(message);
        String input = io.read();
        return input.equalsIgnoreCase("END") ? null : input;
    }


    public boolean promptYes(String message) throws IOException {
        io.write(message + "\n type yes or y to agree");
        String input = io.read().trim().toLowerCase();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }


    public int promptInt(String message, int border) throws IOException {
        io.write(message);
        while (true){
            String input = io.read();
            if(input.equalsIgnoreCase("END")) return -1;
            try {
                int value = Integer.parseInt(input);
                if (value <= border) return value;
                io.write("Invalid number. Try again: ");
            } catch (NumberFormatException e){
                io.write("Please provide an integer.");
            }
        }
    }


    public BigDecimal promptBigDecimalPrices(String message) throws IOException {
        io.write(message);
        while (true){
            try {
                String input = io.read();
                if(input.equalsIgnoreCase("END")) return BigDecimal.ZERO;
                BigDecimal price = new BigDecimal(input);
                if (price.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(new BigDecimal("100000")) < 0) {
                    return price;
                }
                io.write("Price cant be 0 or less, nor it can be more than '10000' (one hundred thousands)!");
            } catch (NumberFormatException e){
                io.write("Invalid format. Provide BigDeciaml, try again: ");
            }
        }
    }


    // mb... not sure
    private static String resultSetToJson(ResultSet resultSet) {
        return null;
    }

}
