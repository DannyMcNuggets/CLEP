import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Admin extends User{


    public Admin(int userID, Connection connection) {
        super(userID, connection);
    }


    @Override
    protected String getMenu() {
        return "\n=== Admin Menu ===\n1 - VIEW_ORDERS\n2 - MANAGE_USERS\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    void handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException {
        switch (command){
            case "VIEW_ORDERS" -> {
                out.writeUTF("Display all orders");
            }
            case "MANAGE_USERS" -> {
                out.writeUTF("Manage and display all users");
            }
            case "LOGOUT" -> {
                out.writeUTF("Logging off...");
            }
            default -> {
                out.writeUTF("Wrong command, try again.");
            }
        }
        out.flush();
    }
}
