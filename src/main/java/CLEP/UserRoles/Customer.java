package CLEP.UserRoles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
                io.read();
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

        HashSet<Integer> lookedProducts = new HashSet<>();

        while (true) {
            io.write("Type product name or ean.         you can try looking for 'TestProduct' or EAN: 56902716");

            String product = io.read();
            // TODO: verify provided length is at least 3 symbols. Add look up counters on each product mentioned
            ResultSet rs = queries.lookUpProduct(product);

            StringBuilder resultString = new StringBuilder();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                BigDecimal price = rs.getBigDecimal("price");
                String ean = rs.getString("ean");
                int stock = rs.getInt("stock");

                resultString.append(buildLine(id, name, description, price, ean, stock));
                lookedProducts.add(id);
            }

            boolean anotherOne = Helpers.promptYes(io, resultString + "\nExit to menu?");
            if (anotherOne) break;
        }

        queries.updateViews(lookedProducts);
    }

    private String buildLine(int id, String name, String description, BigDecimal price, String ean, int stock){
        return id + " | " + name + " | " + description + " | " + price + " | " + ean + " | " + stock;
    }


    private void placeOrder(IOUnit io) throws IOException, SQLException {
        while (true) {
            ResultSet rs = promptProductSelection(io);
            if (rs == null) { // TODO: seems like it is never NULL. check for being empty.
                boolean abandon = Helpers.promptYes(io, "Ok abandoning. Would you like to exit to menu?");
                if(abandon) return;
                continue;
            }

            int amount = promptAmount(io, rs);

            if (!confirmOrder(io, rs, amount)) {
                return;
            }
            int item_id = rs.getInt("id");

            askForCSC(io);

            if (!queries.deductStock(amount, item_id)) {
                boolean tryAgain = Helpers.promptYes(io, "Something went wrong. Exit to menu?");
                if(tryAgain) return;
            }

            if (!inserted(item_id, amount, userID)){
                System.out.println("insertion did not work");
                boolean tryAgain = Helpers.promptYes(io, "Something went wrong. Exit to menu?");
                if(tryAgain) return;
            }

            boolean anotherOne = Helpers.promptYes(io, "All good, email should be sent from here. Want to exit to menu?");
            if (anotherOne) return;
        }
    }


    private boolean inserted (int itemID, int amount, int userID) throws SQLException {
        int orderID = queries.insertOrder(userID);
        return (orderID !=0 && queries.insertOrderItem(orderID, itemID, amount));
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
