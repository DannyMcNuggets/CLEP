import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

class Customer extends User {


    public Customer(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }


    @Override
    protected String getMenu() {
        return "\n=== Customer Menu. Type in the number of command ===\n1 - VIEW_ORDERS\n2 - LOOK_UP\n3 - LOGOUT\nEnter choice:";
    }


    @Override
    int handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                ResultSet rs = queries.getAllOrders();
                out.writeUTF(Helpers.rsToString(rs) + "\n press any key to exit to menu");
                return 1;
            }
            case "3" -> {
                out.writeUTF("Logging off...");
                out.flush();
                return 3;
            }
            case "2" -> {
                handleLookUp(in, out);
                return 2;
            }
            default -> {
                out.writeUTF("Invalid command");
                return 4;
            }
        }
    }

    // TODO: add some logic check: mb length, not empty, smthng else.
    private void handleLookUp(DataInputStream in, DataOutputStream out) throws IOException, SQLException {

        out.writeUTF("Type product name or ean.         you can try looking for 'TestProduct' or EAN: 56902716");

        String product = in.readUTF();

        ResultSet rs = queries.lookUpProduct(product);

        out.writeUTF(Helpers.rsToString(rs) + "\n press any key to exit to menu");
    }
}
