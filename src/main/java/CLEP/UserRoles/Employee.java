package CLEP.UserRoles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Employee extends User{

    public Employee(int userID, Queries queries, Helpers helpers) {
        super(userID, queries, helpers);
    }

    @Override
    String getMenu() {
        return "\n=== UserRoles.Admin Menu ===\n1 - VIEW_ORDERS\n2 - ADD PRODUCT\n3 - LOGOUT\nEnter choice:";
    }

    @Override
    boolean handleCommand(String command, IOUnit io) throws IOException, SQLException {
        switch (command) {
            case "1" -> {
                io.write("Display all orders");
            }
            case "2" -> {
                addProduct(io);
            }
            case "LOGOUT" -> {
                io.write("Logging off...");
                return false;
            }
            default -> {
               io.write("Wrong command, try again.");
            }
        }
        return true;
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
            boolean success = true;// queries.addProduct(productName, productDescription, price, stock, ean);
            if (success){
                boolean stop = Helpers.promptYes(io, "All went good, product added! Do you want to exit?");
                if (stop) break;
            } else {
                boolean stop = Helpers.promptYes(io, "Something went wrong, product was not added! Do you want to exit?");
                if (stop) break;
            }
        }

    }
}
