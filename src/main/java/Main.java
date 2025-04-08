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
                    User user = handleClient(socket, connection);
                    if (user != null) user.handleSession(socket);
                }
            }
        }
    }


    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }


    // TODO: handle client log in (provide username and password), then verify his role and create object. Move to Helpers!
    private static User handleClient(Socket socket, Connection connection) throws RuntimeException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF("Type 1 for login, other options not done yet");
            int choice = input.readInt();

            return switch (choice){
                case 1 -> login(input, output);
                case 2 -> register(input, output);
                default -> throw new IllegalStateException("Unexpected value: " + choice); // cant fire?
            };

        } catch (IOException e) {
            throw new RuntimeException("Error handling client: " + e.getMessage(), e);
        }
    }


    private static User login(DataInputStream input, DataOutputStream output) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        output.writeUTF("Enter username:                  (you can try 'tester123')");
        String username = input.readUTF();

        output.writeUTF("Enter password:                        (tester123 password is 'Qwerty321'");
        String password = input.readUTF();

        // clumsy debugging
        System.out.println("Received login: " + username + " / " + password);

        int userID = Queries.getUserID(connection, username);
        System.out.println("userid is: " + userID);
        if (userID == -1) {
            System.out.println("user id is: " + userID);
            output.writeUTF("Invalid"); // REPLACE WITH PROTOCOL CODES LATER
            System.out.println("wrong login/server side");
            return null;}

        String role;
        if (Helpers.verifyPassword(connection, userID, password)){
            output.writeUTF("login good");
            role = Queries.getUserRole(connection, userID);
            System.out.println("the role is: " + role);
            return switch (role) {
                case "customer" -> new Customer(userID, connection);
                case "admin" -> new Admin(userID, connection);
                case "employee" -> new Employee(userID, connection);
                default -> null;
            };
        }
        output.writeUTF("wrong password");
        return null;
    }

    private static User register(DataInputStream input, DataOutputStream output){

        // while true()

            // ask username, proceed if free and valid

            // ask email, proceed if free and valid

            // ask password, proceed if valid

            // ask to repeat password, proceed if the same

            // if Helpers.registerUser(name, password, email, "customer") true, return new User

        return null;
    }

}