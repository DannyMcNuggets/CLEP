import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

class Customer extends User {


    public Customer(int userID, Connection connection) {
        super(userID, connection);
    }


    @Override
    protected String getMenu() {
        return "\n=== Customer Menu ===\n1 - VIEW_ORDERS\n2 - LOOK_UP\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    void handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException {
        System.out.println("Recieved command!");
        switch (command) {
            case "VIEW_ORDERS" -> {
                System.out.println("Recieved VIEW ORDERS!!!"); // replace rs to string later
                /*
                ResultSet rs = Queries.getAllOrders(connection);
                Packet packet = new Packet(rs);
                ObjectOutputStream objectOutput = new ObjectOutputStream(out);
                objectOutput.writeObject(packet);
                objectOutput.flush();
                 */

                out.writeUTF("well we got where needed, we just need to rework the packet...");
            }
            case "LOGOUT" -> {
                out.writeUTF("Logging off...");
                out.flush();
            }
            case "LOOK_UP" -> {
                out.writeUTF("Okay! let's look up!");
                out.flush();
                handleLookUp(in, out);
            }
            default -> out.writeUTF("Invalid command");
        }
    }

    // TODO: add some logic check: mb length, not empty, smthng else.
    private void handleLookUp(DataInputStream in, DataOutputStream out) throws IOException, SQLException {

        out.writeUTF("Type product name or ean");

        String product = in.readUTF();

        ResultSet rs = Queries.lookUpProduct(connection, product);

        out.writeUTF(Helpers.rsToString(rs));
    }
}
