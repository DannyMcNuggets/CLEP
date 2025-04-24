package CLEP.auth;

import CLEP.UserRoles.Admin;
import CLEP.UserRoles.Customer;
import CLEP.UserRoles.Employee;
import CLEP.UserRoles.User;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Auth {
    IOUnit io;
    Queries queries;
    Helpers helpers;

    public Auth(IOUnit io, Queries queries, Helpers helpers){
        this.io = io;
        this.queries = queries;
        this.helpers = helpers;
    }


    public User login() throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        while (true) {
            int userID = promptUsername();
            if (userID == 0) return null;
            int authenticated = promptPassword(userID);
            if(authenticated == 2) return null;
            if(authenticated == 1) return createUserByRole(userID);
        }
    }


    private int promptUsername() throws IOException, SQLException {
        io.write("Enter username (or type END to cancel).         you can try tester123:");
        while (true){
            String username = io.read();
            if (username.equals("END")) return 0;
            int userID = queries.getUserID(username);
            if (userID == -1) {
                io.write("Wrong username, try again.         you can try tester123:");
                continue;
            }
            return userID;
        }
    }


    private int promptPassword(int userID) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        io.write("Enter password:           tester123 password is Qwerty321");
        while (true) {
            String password = io.read();
            if (helpers.verifyPassword(userID, password)) return 1;
            if (password.equalsIgnoreCase("END")) return 2;
            io.write("Incorrect password. Try again or type END to re-enter username.");
        }
    }


    private User createUserByRole(int userID) throws SQLException {
        String role = queries.getUserRole(userID);
        return switch (role) {
            case "customer" -> new Customer(userID, queries, helpers);
            case "admin" -> new Admin(userID, queries, helpers);
            case "employee" -> new Employee(userID, queries, helpers);
            default -> null;
        };
    }


}
