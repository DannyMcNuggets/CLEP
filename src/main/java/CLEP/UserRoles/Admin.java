package CLEP.UserRoles;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;

import CLEP.auth.Auth;
import CLEP.auth.Register;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import jakarta.mail.internet.AddressException;

public class Admin extends User{


    public Admin(int userID, Queries queries, Helpers helpers, IOUnit io) throws SQLException, AddressException {
        super(userID, queries, helpers, io);
    }


    @Override
    protected String getMenu() {
        return "\n=== Admin Menu ===\n1 - VIEW_ORDERS\n2 - ADD_EMPLOYEE\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    boolean handleCommand(String command) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        switch (command){
            case "1" -> {
                ResultSet rs = queries.getAllOrders();
                io.write(Helpers.rsToString(rs, false) + "\nPress any key to exit to menu");
                io.read();
            }
            case "2" -> {
                boolean registered = new Register(io, queries, helpers).register("employee");
                if (!registered) io.read();
            }
            case "3" -> {
                io.write("Logging off...");
                return false;
            }
            default -> {
                io.write("Wrong command, try again.");
                io.read();
            }
        }
        return true;
    }
}
