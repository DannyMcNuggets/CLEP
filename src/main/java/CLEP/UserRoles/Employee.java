package CLEP.UserRoles;

import java.io.IOException;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Employee extends User{

    public Employee(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }

    @Override
    String getMenu() {
        return "\n=== UserRoles.Admin Menu ===\n1 - VIEW_ORDERS\n2 - IDK\n3 - LOGOUT\nEnter choice:";
    }

    @Override
    int handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "VIEW_ORDERS" -> {
                io.write("Display all orders");
                return 1;
            }
            case "IDK" -> {
                io.write("IDK, smthng");
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
