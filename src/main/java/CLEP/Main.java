package CLEP;

import CLEP.UserRoles.Admin;
import CLEP.UserRoles.Customer;
import CLEP.UserRoles.Employee;
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
import CLEP.util.Queries;

public class Main {
    private static Connection connection;
    private static final String DB_PATH = "database.db";

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        connection = initiateConnection(DB_PATH);

        Queries queries = new Queries(connection);
        Helpers helpers = new Helpers(queries);

        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                try (Socket socket = ss.accept()) {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    User user = handleClient(input, output, queries, helpers);
                    if (user != null) user.handleSession(socket);
                    else output.writeUTF("Logging off..."); // rework this
                }
            }
        }
    }


    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }


    // TODO: handle client log in (provide username and password), then verify his role and create object. Move to Helpers!
    private static User handleClient(DataInputStream input,  DataOutputStream output,  Queries queries, Helpers helpers) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        Auth auth = new Auth(input, output, queries, helpers);
        Register register = new Register(input, output, queries, helpers);

        output.writeUTF("Welcome to CLEP!\n1 - Login\n2 - Register");

        switch (getUserChoice(input, output)) {
            case 1 -> {
                return auth.login();
            }
            case 2 -> {
                if (!register.register()) {
                    output.writeUTF("registration fail");
                    return null;
                } else {
                    return auth.login();
                }
            }
            default -> {
                output.writeUTF("Logging off...");
                return null;
            }
        }
    }


    private static int getUserChoice(DataInputStream input, DataOutputStream output) throws IOException {
        int attempts = 0;
        while (true){
            String choiceStr = input.readUTF();
            try {
                if (attempts >= 4) return 0;
                return Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException e) {
                attempts++;
                output.writeUTF("Provide a digit for a choice: ");
            }
        }
    }
}