package CLEP.UserRoles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
        return "\n=== UserRoles.Customer Menu. Type in the number of command ===" +
                "\n1 - VIEW_ORDERS" +
                "\n2 - LOOK_UP" +
                "\n3 - LOGOUT" +
                "\n4 - PLACE ORDER\n" +
                "Enter choice:";
    }


    @Override
    int handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                ResultSet rs = queries.getAllOrders();
                io.write(Helpers.rsToString(rs, false) + "\n press any key to exit to menu");
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
            case "4" -> {
                placeOrder(io);
                return 4;
            }
            default -> {
                io.write("Invalid command. Press any key to proceed to menu");
                return 4;
            }
        }
    }


    private void handleLookUp(IOUnit io) throws IOException, SQLException {

        io.write("Type product name or ean.         you can try looking for 'TestProduct' or EAN: 56902716");

        String product = io.read();
        // TODO: verify provided length is at least 3 symbols. Add look up counters on each product mentioned
        ResultSet rs = queries.lookUpProduct(product);

        io.write(Helpers.rsToString(rs, false) + "\n press any key to exit to menu");
    }


    private void placeOrder(IOUnit io) throws IOException, SQLException {
        ResultSet rs = promptProductSelection(io);
        if (rs == null) return;

        int stock = Integer.parseInt(rs.getString("stock"));
        int amount = promptAmount(io, stock);

        String productName = rs.getString("name");
        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal totalCost = price.multiply(new BigDecimal(amount));
        int item_id = rs.getInt("id");

        if (!confirmOrder(io, productName, totalCost, amount)) return;

        askForCSC(io);

        if (!queries.deductStock(amount, item_id)){
            io.write("error during deducting your order from database");
            return;
        }

        io.write("and email should be sent from here. Check mailbox for confirmation. Press any key to exit to menu");
    }


    private ResultSet promptProductSelection(IOUnit io) throws IOException, SQLException {
        while (true) {
            io.write("Provide product exact name or ean: ");
            String product = io.read();
            ResultSet rs = queries.lookUpProduct(product);
            io.write("this one?" + "\n" + Helpers.rsToString(rs, true) + "\n" + "type y for yes, n for no");
            String choice = io.read();
            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) return rs;
            if (choice.equalsIgnoreCase("END")) return null;
        }
    }


    private int promptAmount(IOUnit io, int stock) throws IOException {
        while (true) {
            io.write("How many? Provide int. Available: " + stock);
            int amount = Integer.parseInt(io.read());
            if (amount <= stock) return amount;
            io.write("Not enough stock. Try again.");
        }
    }


    private boolean confirmOrder(IOUnit io, String name, BigDecimal totalCost, int amount) throws IOException {
        io.write("Amount: " + amount + ". " + name + ". Total sum: " + totalCost + "€"
                + "\n Type y for confirm, n for not confirm");
        String choice = io.read();
        return choice.equalsIgnoreCase("y");
    }


    private void askForCSC(IOUnit io) throws IOException {
        io.write("Enter CSC number from ur card as confirmation");
        // TODO: validation!
        io.read();
    }
}
