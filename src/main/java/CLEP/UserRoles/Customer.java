package CLEP.UserRoles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.StringJoiner;

import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import jakarta.mail.internet.AddressException;

public class Customer extends User {

    public Customer(int userID, Queries queries, Helpers helpers, IOUnit io) throws SQLException, AddressException {
        super(userID, queries, helpers, io);
    }

    @Override
    protected String getMenu() {
        return "\n=== Customer Menu. Type in the number of command ===" +
                "\n1 - LOOK_UP" +
                "\n2 - PLACE ORDER" +
                "\n3 - VIEW MY ORDERS" +
                "\n4 - LOGOUT" +
                "\nEnter choice:";
    }


    @Override
    boolean handleCommand(String command) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                handleLookUp();
            }
            case "2" -> {
                placeOrder();
            }
            case "3" -> {
                getOrderHistory();
            }
            case "4" -> {
                io.write("Logging off...");
                return false;
            }
            default -> {
                io.write("Invalid command. Press any key to proceed to menu");
                io.read();
            }
        }
        return true;
    }


    private void handleLookUp() throws IOException, SQLException {

        HashSet<Integer> lookedProducts = new HashSet<>();

        while (true) {
            io.write("Type product name or ean.         you can try looking for 'TestProduct' or EAN: 56902716");

            String product;
            while (true){
                product = io.read();
                if (product.equals("END")) return;
                if (product.length() > 1) break;
                io.write("Product name or code must be longer than 2 symbols");
            }

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
                resultString.append("\n");
                lookedProducts.add(id);
            }

            boolean anotherOne = helpers.promptYes(resultString + "\nExit to menu?");
            if (anotherOne) break;
        }

        queries.updateViews(lookedProducts);
    }


    private String buildLine(int id, String name, String description, BigDecimal price, String ean, int stock){
        return id + " | " + name + " | " + description + " | " + price + " | " + ean + " | " + stock;
    }


    private void placeOrder() throws IOException, SQLException {
        while (true) {
            ResultSet rs = promptProductSelection();
            if (!rs.isBeforeFirst()) { // TODO: seems like it is never NULL. check for being empty.
                boolean abandon = helpers.promptYes("Ok abandoning. Would you like to exit to menu?");
                if (abandon) return;
                continue;
            }

            int amount = promptAmount(rs);

            if (!confirmOrder(rs, amount)) {
                return;
            }
            int item_id = rs.getInt("id");

            askForCSC();

            if (!queries.deductStock(amount, item_id)) {
                boolean tryAgain = helpers.promptYes("Something went wrong. Exit to menu?");
                if (tryAgain) return;
            }

            if (!inserted(item_id, amount, userID)) {
                System.out.println("insertion did not work");
                boolean tryAgain = helpers.promptYes("Something went wrong. Exit to menu?");
                if (tryAgain) return;
            }

            try (ResultSet workerEmails = queries.executeQuery("SELECT email FROM users WHERE role = 'employee'")) {
                StringJoiner stringJoiner = new StringJoiner(",");
                while (workerEmails.next()) {
                    stringJoiner.add(workerEmails.getString("email"));
                }

                try {
                    mailSender.sendMail("Order placed", "Order placed: " + rs.getString("name"), stringJoiner.toString());
                } catch (Exception e){
                    System.out.println(e); // TODO: some proper handling and user notification
                }
            }

            boolean anotherOne = helpers.promptYes("All good, email should be sent from here. Want to exit to menu?");
            if (anotherOne) return;
        }

    }


    private boolean inserted (int itemID, int amount, int userID) throws SQLException {
        int orderID = queries.insertOrder(userID);
        return (orderID !=0 && queries.insertOrderItem(orderID, itemID, amount));
    }


    private ResultSet promptProductSelection() throws IOException, SQLException {
        while (true) {
            String product = helpers.promptString("Provide product exact name or ean:");
            if (product == null) return null;

            ResultSet rs = queries.lookUpProduct(product);
            if (helpers.promptYes("This one?\n" + Helpers.rsToString(rs, true))) return queries.lookUpProduct(product);
        }
    }


    private int promptAmount(ResultSet rs) throws IOException, SQLException {
        int stock = rs.getInt("stock");
        return helpers.promptInt("How many? Provide int. Available: " + stock, stock);
    }


    private boolean confirmOrder(ResultSet rs, int amount) throws IOException, SQLException {
        String name = rs.getString("name");
        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(amount));
        return helpers.promptYes("Amount: " + amount + ". " + name + ". Total: " + totalCost + "€ ");
    }


    // TODO: VERIFICATION!
    private void askForCSC() throws IOException {
        String csc = helpers.promptString("Enter CSC number from your card as confirmation:");
    }


    private void getOrderHistory() {
        String query = "SELECT order_id, product_id, quantity FROM orders INNER JOIN order_items ON orders.id = order_items.order_id WHERE user_id = ?";
        try (ResultSet rs = queries.executeQuery(query, userID)) {
            String list = Helpers.rsToString(rs, false);
            io.write(list + "\n" + "Press any key to exit to menu: ");
            io.read(); // TODO: REMAKE TO PROMPT YES
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
