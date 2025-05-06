package CLEP.util;

import CLEP.UserRoles.User;
import CLEP.auth.Auth;
import CLEP.auth.Register;
import jakarta.mail.internet.AddressException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;

public class ClientConnection implements Runnable {
    private static final int MAX_ATTEMPTS = 4;

    private final Socket socket;
    private final Connection dbConnection;
    private final Queries queries;
    private final Helpers helpers;

    public ClientConnection(Socket socket, Connection dbConnection) {
        this.socket = socket;
        this.dbConnection = dbConnection;

        queries = new Queries(dbConnection);
        helpers = new Helpers(queries);
    }

    @Override
    public void run() {
        try (socket) {
            DataInputStream input = null;
            DataOutputStream output = null;
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            IOUnit io = new IOUnit(input, output);

            // TODO: rework this horror
            User user = null;
            while (user == null) {
                try {
                    user = handleClient(io, queries, helpers);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                user.handleSession(io);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //else output.writeUTF("Logging off..."); // rework this
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                System.err.println("Could not close db connection properly");
                e.printStackTrace();
            }
        }
    }

    private static User handleClient(IOUnit io,  Queries queries, Helpers helpers) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, AddressException {

        Auth auth = new Auth(io, queries, helpers);
        Register register = new Register(io, queries, helpers);

        io.write("Welcome to CLEP!\n1 - Login\n2 - Register");

        switch (getUserChoice(io)) {
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
}
