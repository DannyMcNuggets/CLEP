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
        queries = new Queries(dbConnection);
        helpers = new Helpers(queries);
        io = new IOUnit(socket);
    }

    @Override
    public void run() throws RuntimeException {

        int maxTries = 5;
        User user;
        try {
            user = getUser(io);
        } catch (SQLException | AddressException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        while (true) { //TODO: rework this horror
            try (io) {
                user.handleSession(io);
                break;
            } catch (SQLException e){
                if (e.getMessage().contains("database is locked") && maxTries-- > 0){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    continue;
                }
                try {
                    io.write("Could not access database. Try again later.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            closeConnection(dbConnection);
        }
    }


    private void closeConnection(Connection dbConnection) {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private User getUser(IOUnit io) throws SQLException, AddressException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user;
        do {
            user = handleClient(io, queries, helpers);
        } while (user == null);
        return user;
    }

    private User handleClient(IOUnit io,  Queries queries, Helpers helpers) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, AddressException {

        // TODO: move to constructor
        Auth auth = new Auth(io, queries, helpers);
        Register register = new Register(io, queries, helpers);

        switch (Helpers.promptInt(io, "Welcome to CLEP!\n1 - Login\n2 - Register", 3)
            ){
            case 1 -> {
                return auth.login();
            }
            case 2 -> {
                if (register.register()) {
                    return auth.login();
                } else {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

    /*
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
