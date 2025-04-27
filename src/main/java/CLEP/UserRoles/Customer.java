package CLEP.UserRoles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;

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

    // TODO: handle "END" at any step!
    @Override
    boolean handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                ResultSet rs = queries.getAllOrders();
                io.write(Helpers.rsToString(rs, false) + "\n press any key to exit to menu");
            }
            case "3" -> {
                io.write("Logging off...");
                return false;
            }
            case "2" -> {
                handleLookUp(io);
            }
            case "4" -> {
                placeOrder(io);
            }
            default -> {
                io.write("Invalid command. Press any key to proceed to menu");
            }
        }
        return true;
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
        if (rs == null) {
            io.write("Ok, abandoning. Press any key to exit to menu");
            return;
        }

        int amount = promptAmount(io, rs);

        if (!confirmOrder(io, rs, amount)) {
            io.write("ok, abandoning. press any key to exit to menu");
            return;
        }
        int item_id = rs.getInt("id");

        askForCSC(io);

        if (!queries.deductStock(amount, item_id)){
            io.write("error during deducting your order from database");
            return;
        }

        ResultSet workerEmails = queries.executeQuery("SELECT email FROM users WHERE role = 'employee'");
        StringJoiner stringJoiner = new StringJoiner(",");
        while (workerEmails.next()) {
            stringJoiner.add(workerEmails.getString("email"));
        }

        Helpers.sendMail("Order placed", "Order placed: " + rs.getString("name"), "cleptest4@gmail.com", "cleptest4@gmail.com", stringJoiner.toString());

        io.write("and email should be sent from here. Check mailbox for confirmation. Press any key to exit to menu");
    }


    private ResultSet promptProductSelection(IOUnit io) throws IOException, SQLException {
        while (true) {
            String product = Helpers.promptString(io, "Provide product exact name or ean:");
            if (product == null) return null;

            ResultSet rs = queries.lookUpProduct(product);
            if (Helpers.promptYes(io, "This one?\n" + Helpers.rsToString(rs, true))) return rs;
        }
    }


    private int promptAmount(IOUnit io, ResultSet rs) throws IOException, SQLException {
        int stock = rs.getInt("stock");
        return Helpers.promptInt(io, "How many? Provide int. Available: " + stock, stock);
    }


    private boolean confirmOrder(IOUnit io, ResultSet rs, int amount) throws IOException, SQLException {
        String name = rs.getString("name");
        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(amount));
        return Helpers.promptYes(io, "Amount: " + amount + ". " + name + ". Total: " + totalCost + "€ ");
    }


    private void askForCSC(IOUnit io) throws IOException {
        Helpers.promptString(io, "Enter CSC number from your card as confirmation:");
    }
}
