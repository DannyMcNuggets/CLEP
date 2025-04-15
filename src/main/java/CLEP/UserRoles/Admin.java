package CLEP.UserRoles;

import java.io.IOException;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Admin extends User{


    public Admin(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }


    @Override
    protected String getMenu() {
        return "\n=== UserRoles.Admin Menu ===\n1 - VIEW_ORDERS\n2 - MANAGE_USERS\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    int handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command){
            case "VIEW_ORDERS" -> {
                io.write("Display all orders");
                return 1;
            }
            case "MANAGE_USERS" -> {
               io.write("Manage and display all users");
                return 2;
            }
            case "LOGOUT" -> {
                io.write("Logging off...");
                return 3;
            }
            default -> {
                io.write("Wrong command, try again.");
                return 4;
            }
        }
    }
}
