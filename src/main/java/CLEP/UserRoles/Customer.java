package CLEP.UserRoles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Customer extends User {


    public Customer(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }


    @Override
    protected String getMenu() {
        return "\n=== UserRoles.Customer Menu. Type in the number of command ===\n1 - VIEW_ORDERS\n2 - LOOK_UP\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    int handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                ResultSet rs = queries.getAllOrders();
                io.write(Helpers.rsToString(rs) + "\n press any key to exit to menu");
                return 1;
            }
            case "3" -> {
                io.write("Logging off...");
               io.flush();
                return 3;
            }
            case "2" -> {
                handleLookUp(io);
                return 2;
            }
            default -> {
                io.write("Invalid command. Press any key to proceed to menu");
                return 4;
            }
        }
    }

    // TODO: add some logic check: mb length, not empty, smthng else.
    private void handleLookUp(IOUnit io) throws IOException, SQLException {

        io.write("Type product name or ean.         you can try looking for 'TestProduct' or EAN: 56902716");

        String product = io.read();

        ResultSet rs = queries.lookUpProduct(product);

        io.write(Helpers.rsToString(rs) + "\n press any key to exit to menu");
    }
}
