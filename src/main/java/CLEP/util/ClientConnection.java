package CLEP.util;

import CLEP.UserRoles.User;
import CLEP.auth.Auth;
import CLEP.auth.Register;
import jakarta.mail.internet.AddressException;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;

public class ClientConnection implements Runnable {
    private final Connection dbConnection;
    private final Queries queries;
    private final Helpers helpers;
    private final IOUnit io;

    public ClientConnection(Socket socket, Connection dbConnection) throws IOException {
        this.dbConnection = dbConnection;
        this.io = new IOUnit(socket);
        this.queries = new Queries(dbConnection);
        this.helpers = new Helpers(queries, io);
    }


    @Override
    public void run() {
        try {
            User user = getUserOrExit();
            if (user == null) return;

            try (io; dbConnection) {
                runUserSession(user);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private User getUserOrExit() throws Exception {
        User user = getUser();
        if (user == null) {
            io.write("EXIT");
        }
        return user;
    }


    private void runUserSession(User user) throws IOException {
        int triesLeft = 5;

        while (true) {
            try {
                user.handleSession();
                break; // success
            } catch (SQLException e) {
                if (shouldRetry(e, triesLeft)) {
                    triesLeft--;
                    sleepBriefly();
                    continue;
                }
                io.write("Could not access database. Try again later.");
                break;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private boolean shouldRetry(SQLException e, int triesLeft) {
        return triesLeft > 0 && e.getMessage() != null && e.getMessage().contains("database is locked");
    }


    private void sleepBriefly() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrying DB access", ie);
        }
    }


    private User getUser() throws SQLException, AddressException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        int attempts = 5;
        while (true) {
            int choice = helpers.promptInt(
                    "Welcome to CLEP! Provide number to make a choice:\n1 - Login\n2 - Register\n3 - Exit", 4);
            switch (choice) {
                case 1:
                    return new Auth(io, queries, helpers).login();
                case 2:
                    boolean registered = new Register(io, queries, helpers).register("customer");
                    if (registered) return new Auth(io, queries, helpers).login();
                    break;
                case 3:
                    return null;
            }
            if (attempts-- < 0) return null;
        }
    }


    /*
    private User handleClient() throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, AddressException {
        int choice = helpers.promptInt("Welcome to CLEP! Provide number to make a choice:\n1 - Login\n2 - Register", 3);
        return switch (choice) {
            case 1 -> new Auth(io, queries, helpers).login();
            case 2 -> new Register(io, queries, helpers).register()
                    ? new Auth(io, queries, helpers).login()
                    : null;
            case 3 -> null;
            default -> null;
        };
    }



    private static int getUserChoice(IOUnit io) throws IOException {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS){
            String choiceStr = io.read();

            try {
                return Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException e) {
                attempts++;
                io.write("Provide a digit for a choice: ");
            }
        }
        return 0;
    }
     */
}
