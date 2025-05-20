package CLEP.UserRoles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;
import jakarta.mail.internet.AddressException;

public class Employee extends User{

    public Employee(int userID, Queries queries, Helpers helpers, IOUnit io) throws SQLException, AddressException {
        super(userID, queries, helpers, io);
    }

    @Override
    String getMenu() {
        return "\n=== UserRoles.Employee Menu ===\n1 - VIEW_ORDERS\n2 - ADD PRODUCT\n3 - TOP_PRODUCTS\n4 - LOGOUT\nEnter choice:";
    }

    @Override
    boolean handleCommand(String command) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                io.write("Display all orders. Press any key to get to menu");
                io.read();
            }
            case "2" -> {
                addProduct();
            }
            case "3" -> {
                topProducts();
            }
            case "4" -> {
                io.write("Logging off...");
                return false;
            }
            default -> {
               io.write("Wrong command, try again.");
            }
        }
        return true;
    }

    private void topProducts() throws SQLException, IOException {
        ResultSet top = queries.viewTopProducts();
        StringBuilder output = new StringBuilder();

        while (top.next()) {
            int code = top.getInt("code");
            String name = top.getString("name");
            String ean = top.getString("ean");
            int totalOrdered = top.getInt("total_ordered");

            output.append("Code: ").append(code)
                    .append("| Name: ").append(name)
                    .append("| EAN: ").append(ean)
                    .append("| Total orders: ").append(totalOrdered)
                    .append("\n");
        }

        io.write(output + "\nPress any kee to exit to menu: ");
        io.read();
    }

    private void addProduct() throws IOException, SQLException {
        while (true) {
            String productName = helpers.promptString("Provide product name: ");
            String productDescription = helpers.promptString("Provide product description: ");
            BigDecimal price = helpers.promptBigDecimalPrices("Provide the price: "); // mb if condition to check it was not "END"
            int stock = helpers.promptInt("Provide current stock: ", 100000); // this is stupid
            String ean = helpers.promptString("Provide product ean or code: ");

            // ask what is being added
            String confirmation = "\nYou are adding following product:\n" +
                    "Name: " + productName + "\n" +
                    "Description: " + productDescription + "\n" +
                    "Price: " + price + " €\n" +
                    "EAN: " + ean + "\n" +
                    "Stock: " + stock + "\n";
            boolean confirm = helpers.promptYes(confirmation);

            if (!confirm) continue;

            // check if added
            boolean success = queries.addProduct(productName, productDescription, price, stock, ean);
            boolean stop = helpers.promptYes(success ?
                    "All went good, product added! Do you want to exit?" :
                    "Something went wrong, product was not added! Do you want to exit?");
            if (stop) break;
        }

    }
}
