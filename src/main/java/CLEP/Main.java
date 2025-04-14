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
                    User user = handleClient(input, output, queries, helpers, socket);
                    if (user != null) user.handleSession(socket);
                }
            }
        }
    }


    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }


    // TODO: handle client log in (provide username and password), then verify his role and create object. Move to Helpers!
    private static User handleClient(DataInputStream input,  DataOutputStream output,  Queries queries, Helpers helpers, Socket socket) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        output.writeUTF("Welcome to CLEP!\n1 - Login\n2 - Register");

        String choiceStr = input.readUTF();

        // TODO: rework this mess
        int choice;
        try {
            choice = Integer.parseInt(choiceStr.trim());
        } catch (NumberFormatException e) {
            output.writeUTF("wrong choice, try again");
            socket.close();
            return null;
        }

        User user = null;

        switch (choice) {
            case 1 -> user = login(input, output, queries, helpers);
            case 2 -> {
                boolean success = register(input, output, queries, helpers);
                if (!success) {
                    output.writeUTF("registration fail");
                    return null;
                } else {
                    return login(input, output, queries, helpers);
                }
            }
            default -> {
                System.out.println("wrong input from user");
                output.writeUTF("EXIT");
                return null;
            }
        }

        return user;
    }



    private static User login(DataInputStream input, DataOutputStream output, Queries queries, Helpers helpers)
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        System.out.println("we are in login section");
        output.writeUTF("Enter username (or type END to cancel):");
        while (true) {
            String username = input.readUTF();
            if (username.equalsIgnoreCase("END")) return null;
            System.out.println("username is: " + username);

            int userID = queries.getUserID(username);
            System.out.println("user id is:" + userID);
            if (userID == -1) {
                output.writeUTF("Invalid username. Try again.");
                continue;
            }

            while (true) {
                output.writeUTF("Enter password:");
                String password = input.readUTF();

                if (!helpers.verifyPassword(userID, password)) {
                    output.writeUTF("Incorrect password. Try again or type END to re-enter username.");
                    String retry = input.readUTF();
                    if (retry.equalsIgnoreCase("END")) break; // break inner loop to ask for username again
                    else password = retry; // reuse input as new password
                }

                if (helpers.verifyPassword(userID, password)) {
                    String role = queries.getUserRole(userID);

                    return switch (role) {
                        case "customer" -> new Customer(userID, queries, helpers);
                        case "admin" -> new Admin(userID, queries, helpers);
                        case "employee" -> new Employee(userID, queries, helpers);
                        default -> null;
                    };
                }
            }
        }
    }



    private static boolean register(DataInputStream input, DataOutputStream output, Queries queries, Helpers helpers)
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        String username = null;
        String email = null;
        String password = null;
        int userID = 0;

        // STEP 1: Ask for username
        output.writeUTF("Enter username:");
        while (true) {
            username = input.readUTF();
            if (queries.checkIfNameTaken(username)) {
                output.writeUTF("Username already taken. Try again:");
            } else {
                break;
            }
        }

        // STEP 2: Ask for email
        output.writeUTF("Enter email:");
        while (true) {
            email = input.readUTF();

            if (Helpers.emailValidate(email)) {
                break;
            } else {
                output.writeUTF("Invalid email. Enter again: ");
            }
        }

        // STEP 3: Ask for password
        output.writeUTF("Enter password. Must include high and low case, a digit, a letter, 8 symbols length\":");
        while (true) {
            password = input.readUTF();
            if (Helpers.passwordValid(password)) {
               break;
            } else {
                output.writeUTF("Weak password. Must include high and low case, a digit, a letter, 8 symbols length. Try again");
            }
        }

        // TODO: STEP 4: repeat password

        // STEP 5: Finalize registration
        if (queries.insertUser(username, email, "customer")){
            userID = queries.getUserID(username);
            if (userID == -1) {
                output.writeUTF("Failed to insert user.");
                return false;
            }
        }

        byte[] salt = helpers.generateSalt();
        byte[] hash = helpers.generateHash(salt, password);

        if (!queries.insertSaltAndHash(userID, hash, salt)) {
            output.writeUTF("Failed to store credentials.");
            return false;
        }

        output.writeUTF("Registration successful! Press any key to proceed to login");
        input.readUTF();
        return true;
    }



}