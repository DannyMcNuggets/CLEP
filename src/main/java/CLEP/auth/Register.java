package CLEP.auth;

import CLEP.util.Helpers;
import CLEP.util.Queries;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Register {
    DataInputStream input;
    DataOutputStream output;
    Queries queries;
    Helpers helpers;

    public Register(DataInputStream input, DataOutputStream output, Queries queries, Helpers helpers){
        this.input = input;
        this.output = output;
        this.queries = queries;
        this.helpers = helpers;
    }

    public boolean register()
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        // STEP 1: Ask for username
        String username = promptUsername();

        // STEP 2: Ask for email
        String email = promptEmail();

        // STEP 3: Ask for password
        String password = promptPassword();

        // TODO: STEP 4: repeat password

        // STEP 5: Finalize registration
        int userID = insertUser(username, email);
        if (userID == -1){
            output.writeUTF("Failed to register");
            return false;
        }

        // STEP 6: Insert credentials
        if (!insertCredentials(userID, password)){
            output.writeUTF("Failed to insert credentials");
            return false;
        }

        output.writeUTF("Registration successful! Press any key to proceed to login");
        input.readUTF();
        return true;
    }


    private boolean insertCredentials(int userID, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException, IOException {
        byte[] salt = helpers.generateSalt();
        byte[] hash = helpers.generateHash(salt, password);

        if (!queries.insertSaltAndHash(userID, hash, salt)) {
            output.writeUTF("Failed to store credentials.");
            return false;
        }
        return true;
    }


    private int insertUser(String username, String email) throws SQLException {
        queries.insertUser(username, email, "customer");
        return queries.getUserID(username);
    }


    private String promptPassword() throws IOException, SQLException {
        return promptInput(
                "Enter password. Must include high and low case, a digit, a letter, 8 symbols length:",
                Helpers::passwordValid,
                "Weak password. Must include high and low case, a digit, a letter, 8 symbols length. Try again"
        );
    }


    private String promptUsername() throws IOException, SQLException {
        return promptInput(
                "Enter username:",
                queries::checkIfNameFree,
                "Username already taken. Try again:"
        );
    }


    private String promptEmail() throws IOException, SQLException {
        return promptInput(
                "Enter email:",
                Helpers::emailValidate,
                "Invalid email. Enter again: "
        );
    }


    @FunctionalInterface
    interface Validator {
        boolean isValid(String input) throws SQLException;
    }


    private String promptInput(String promptMessage, Validator validator, String errorMessage) throws IOException, SQLException {
        String inputStr = null;
        output.writeUTF(promptMessage);
        while (true) {
            inputStr = input.readUTF();
            if (validator.isValid(inputStr)) {
                break;
            } else {
                output.writeUTF(errorMessage);
            }
        }
        return inputStr;
    }

}
