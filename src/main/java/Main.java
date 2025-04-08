import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class Main {
    private static Connection connection;
    private static final String DB_PATH = "database.db";

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        connection = initiateConnection(DB_PATH);

        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                try (Socket socket = ss.accept()) {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    System.out.println("ffs work");
                    User user = handleClient(socket, connection, input, output);
                    if (user != null) user.handleSession(socket, input, output);
                }
            }
        }
    }


    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }


    // TODO: handle client log in (provide username and password), then verify his role and create object. Move to Helpers!
    private static User handleClient(Socket socket, Connection connection,  DataInputStream input,  DataOutputStream output) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        output.writeUTF("Welcome to Store CLI!\n1 - Login\n2 - Register");

        String choiceStr = input.readUTF();
        int choice = Integer.parseInt(choiceStr.trim());

        User user = null;

        switch (choice) {
            case 1 -> user = login(input, output);
            case 2 -> {
                boolean success = register(input, output);
                if (!success) {
                    output.writeUTF("registration fail");
                    return null;
                } else {
                    output.writeUTF("registration success");
                    return login(input, output);
                }
            }
            default -> {
                output.writeUTF("Invalid choice. Disconnecting.");
                output.writeUTF("EXIT");
                return null;
            }
        }

        return user;
    }



    private static User login(DataInputStream input, DataOutputStream output)
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        System.out.println("we are in login section");
        output.writeUTF("Enter username (or type END to cancel):");
        while (true) {
            String username = input.readUTF();
            if (username.equalsIgnoreCase("END")) return null;
            System.out.println("username is: " + username);

            int userID = Queries.getUserID(connection, username);
            System.out.println("user id is:" + userID);
            if (userID == -1) {
                output.writeUTF("Invalid username. Try again.");
                continue;
            }

            while (true) {
                output.writeUTF("Enter password:");
                String password = input.readUTF();

                if (!Helpers.verifyPassword(connection, userID, password)) {
                    output.writeUTF("Incorrect password. Try again or type END to re-enter username.");
                    String retry = input.readUTF();
                    if (retry.equalsIgnoreCase("END")) break; // break inner loop to ask for username again
                    else password = retry; // reuse input as new password
                }

                if (Helpers.verifyPassword(connection, userID, password)) {
                    String role = Queries.getUserRole(connection, userID);

                    return switch (role) {
                        case "customer" -> new Customer(userID, connection);
                        case "admin" -> new Admin(userID, connection);
                        case "employee" -> new Employee(userID, connection);
                        default -> null;
                    };
                }
            }
        }
    }



    private static boolean register(DataInputStream input, DataOutputStream output)
            throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

        String username = null;
        String email = null;
        String password = null;
        int userID = 0;

        // STEP 1: Ask for username
        while (true) {
            output.writeUTF("Enter username:");
            username = input.readUTF();

            if (Queries.checkIfNameTaken(connection, username)) {
                output.writeUTF("Username already taken.");
            } else {
                output.writeUTF("OK");
                break;
            }
        }

        // STEP 2: Ask for email
        while (true) {
            output.writeUTF("Enter email:");
            email = input.readUTF();

            if (!Helpers.emailValidate(email)) {
                output.writeUTF("Invalid email.");
            } else {
                output.writeUTF("OK");
                break;
            }
        }

        // STEP 3: Ask for password
        while (true) {
            output.writeUTF("Enter password:");
            password = input.readUTF();

            if (!Helpers.passwordValid(password)) {
                output.writeUTF("Weak password.");
            } else {
                output.writeUTF("OK");
                break;
            }
        }

        // STEP 4: Finalize registration
        if (Queries.insertUser(connection, username, email, "customer")){
            if (Queries.getUserID(connection, username) == -1) {
                output.writeUTF("Failed to insert user.");
                return false;
            }
        }


        byte[] salt = Helpers.generateSalt();
        byte[] hash = Helpers.generateHash(salt, password);

        if (!Queries.insertSaltAndHash(connection, userID, hash, salt)) {
            output.writeUTF("Failed to store credentials.");
            return false;
        }

        output.writeUTF("Registration successful!");
        return true;
    }



}