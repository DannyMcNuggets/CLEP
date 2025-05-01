package CLEP.UserRoles;

import java.io.IOException;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import jakarta.mail.internet.AddressException;

public class Employee extends User{

    public Employee(int userID, Queries queries, Helpers helpers) throws SQLException, AddressException {
        super(userID, queries, helpers);
    }

    @Override
    String getMenu() {
        return "\n=== UserRoles.Admin Menu ===\n1 - VIEW_ORDERS\n2 - IDK\n3 - LOGOUT\nEnter choice:";
    }

    @Override
    boolean handleCommand(String command, IOUnit io) throws IOException {
        switch (command) {
            case "1" -> {
                io.write("Display all orders");
            }
            case "2" -> {
                addProduct(io);
            }
            case "LOGOUT" -> {
                io.write("Logging off...");
                return false;
            }
            default -> {
               io.write("Wrong command, try again.");
            }
        }
        return true;
    }

    private void addProduct(IOUnit io){
        // getname
        // description
        // price
        // current stock
        // ean

        // queries.addProduct()
    }
}
