package CLEP.auth;

import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Register {
    IOUnit io;
    Queries queries;
    Helpers helpers;

    public Register(IOUnit io, Queries queries, Helpers helpers){
        this.io = io;
        this.queries = queries;
        this.helpers = helpers;
    }

    public boolean register()
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        // STEP 1: Ask for username
        String username = promptUsername();
        if (username == null) return false;

        // STEP 2: Ask for email
        String email = promptEmail();
        if (email == null) return false;

        // STEP 3: Ask for password
        String password = promptPassword();
        if (password == null) return false;

        // STEP 4: repeat password
        String repeatPassword = promptRepeatPassword(password);
        if (repeatPassword == null) return false;

        // STEP 5: Finalize registration
        int userID = queries.insertUser(username, email, "customer");
        if (userID == -1){
            io.write("Failed to register");
            return false;
        }

        // STEP 6: Insert credentials
        if (!insertCredentials(userID, password)){
           io.write("Failed to insert credentials");
            return false;
        }

        io.write("Registration successful! Press any key to proceed to login");
        io.read();
        return true;
    }


    private boolean insertCredentials(int userID, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException, IOException {
        byte[] salt = helpers.generateSalt();
        byte[] hash = helpers.generateHash(salt, password);

        if (!queries.insertSaltAndHash(userID, hash, salt)) {
            io.write("Failed to store credentials.");
            return false;
        }
        return true;
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


    private String promptRepeatPassword(String password) throws SQLException, IOException {
        return promptInput("Re-enter password: ", new Validator() {
            @Override
            public boolean isValid(String input) {
                return input.equals(password);
            }
        }, "Passwords do not match. Try again: ");
    }


    @FunctionalInterface
    interface Validator {
        boolean isValid(String input) throws SQLException;
    }


    private String promptInput(String promptMessage, Validator validator, String errorMessage) throws IOException, SQLException {
        String inputStr;
        io.write(promptMessage);
        while (true) {
            inputStr = io.read();
            if (inputStr.equals("END")) return null;
            if (validator.isValid(inputStr)) {
                break;
            } else {
                io.write(errorMessage);
            }
        }
        return inputStr;
    }

}
