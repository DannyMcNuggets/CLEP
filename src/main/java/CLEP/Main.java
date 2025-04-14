package CLEP;

import CLEP.UserRoles.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

import CLEP.auth.Auth;
import CLEP.auth.Register;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Main {
    private static Connection connection;
    private static final String DB_PATH = "database.db";
    private static final int MAX_ATTEMPTS = 4;

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        connection = initiateConnection(DB_PATH);

        Queries queries = new Queries(connection);
        Helpers helpers = new Helpers(queries);

        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                try (Socket socket = ss.accept()) {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    IOUnit io = new IOUnit(input, output);


                    User user = handleClient(io, queries, helpers);
                    if (user != null) user.handleSession(io);
                    else output.writeUTF("Logging off..."); // rework this
                }
            }
        }
    }


    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }


    // TODO: handle client log in (provide username and password), then verify his role and create object. Move to Helpers!
    private static User handleClient(IOUnit io,  Queries queries, Helpers helpers) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

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