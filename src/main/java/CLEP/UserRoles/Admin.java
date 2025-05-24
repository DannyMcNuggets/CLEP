package CLEP.UserRoles;

import java.io.IOException;
import java.sql.SQLException;
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
        return "\n=== UserRoles.Admin Menu ===\n1 - VIEW_ORDERS\n2 - ADD_EMPLOYEE\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    boolean handleCommand(String command) throws IOException {
        switch (command){
            case "1" -> {
                io.write("Display all orders");
            }
            case "2" -> {
               io.write("Add employee");
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
