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

            output.writeUTF("Enter username:");
            String username = input.readUTF();

            output.writeUTF("Enter password:");
            String password = input.readUTF();

            // clumsy debuggins
            System.out.println("Received login: " + username + " / " + password);

            int userID = Queries.getUserID(connection, username);
            if (userID == -1) {
                System.out.println("user id is: " + userID);
                output.writeUTF("Invalid"); // REPLACE WITH PROTOCOL CODES LATER
                System.out.println("wrong login/server side");
                return null;}

            String role;

            if (Helpers.verifyPassword(connection, userID, password)){
                output.writeUTF("login good");
                role = Queries.getUserRole(connection, userID);
                return switch (role) {
                    case "customer" -> new Customer(userID, connection);
                    //case "admin" -> new Admin(userID, connection);  more roles later
                    default -> null;
                };
            }
            output.writeUTF("wrong password");
            return null;

        } catch (IOException e) {
            throw new RuntimeException("Error handling client: " + e.getMessage(), e);
        }
    }

}