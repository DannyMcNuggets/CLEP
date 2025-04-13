import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Admin extends User{


    public Admin(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }


    @Override
    protected String getMenu() {
        return "\n=== Admin Menu ===\n1 - VIEW_ORDERS\n2 - MANAGE_USERS\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    int handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException {
        switch (command){
            case "VIEW_ORDERS" -> {
                out.writeUTF("Display all orders");
                return 1;
            }
            case "MANAGE_USERS" -> {
                out.writeUTF("Manage and display all users");
                return 2;
            }
            case "LOGOUT" -> {
                out.writeUTF("Logging off...");
                return 3;
            }
            default -> {
                out.writeUTF("Wrong command, try again.");
                return 4;
            }
        }
    }
}
