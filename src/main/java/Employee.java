import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Employee extends User{

    public Employee(int userID, Connection connection) {
        super(userID, connection);
    }

    @Override
    String getMenu() {
        return "\n=== Admin Menu ===\n1 - VIEW_ORDERS\n2 - IDK\n3 - LOGOUT\nEnter choice:";
    }

    @Override
    void handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException {
        switch (command) {
            case "VIEW_ORDERS" -> {
                out.writeUTF("Display all orders");
            }
            case "IDK" -> {
                out.writeUTF("IDK, smthng");
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
