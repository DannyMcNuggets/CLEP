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

    public Employee(int userID, Queries queries, Helpers helpers) throws SQLException, AddressException {
        super(userID, queries, helpers);
    }

    @Override
    String getMenu() {
        return "\n=== UserRoles.Employee Menu ===\n1 - VIEW_ORDERS\n2 - ADD PRODUCT\n3 - TOP_PRODUCTS\n4 - LOGOUT\nEnter choice:";
    }

    @Override
    boolean handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                io.write("Display all orders. Press any key to get to menu");
                io.read();
            }
            case "2" -> {
                addProduct(io);
            }
            case "3" -> {
                topProducts(io);
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

    private void topProducts(IOUnit io) throws SQLException, IOException {
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

    private void addProduct(IOUnit io) throws IOException, SQLException {
        while (true) {
            String productName = Helpers.promptString(io, "Provide product name: ");
            String productDescription = Helpers.promptString(io, "Provide product description: ");
            BigDecimal price = Helpers.promptBigDecimalPrices(io, "Provide the price: "); // mb if condition to check it was not "END"
            int stock = Helpers.promptInt(io, "Provide current stock: ", 100000); // this is stupid
            String ean = Helpers.promptString(io, "Provide product ean or code: ");

            // ask what is being added
            String confirmation = "\nYou are adding following product:\n" +
                    "Name: " + productName + "\n" +
                    "Description: " + productDescription + "\n" +
                    "Price: " + price + " €\n" +
                    "EAN: " + ean + "\n" +
                    "Stock: " + stock + "\n";
            boolean confirm = Helpers.promptYes(io, confirmation);

            if (!confirm) continue;

            // check if added
            boolean success = queries.addProduct(productName, productDescription, price, stock, ean);
            boolean stop = Helpers.promptYes(io, success ?
                    "All went good, product added! Do you want to exit?" :
                    "Something went wrong, product was not added! Do you want to exit?");
            if (stop) break;
        }

    }
}
