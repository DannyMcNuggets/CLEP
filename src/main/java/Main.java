import org.json.JSONObject;

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
                    if (user != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    user.handleSession(socket);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }).start();
                    }
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
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            JSONObject loginRequest = (JSONObject) input.readObject();
            String username = loginRequest.getString("username");
            String password = loginRequest.getString("password");

            // clumsy debuggins
            System.out.println("Received login: " + username + " / " + password);

            int userID = Queries.getUserID(connection, username);
            if (userID == -1) {
                System.out.println("user id is: " + userID);
                //output.writeUTF("Invalid"); // REPLACE WITH PROTOCOL CODES LATER
                System.out.println("wrong login/server side");
                JSONObject loginError = new JSONObject();
                loginError.put("Status", 1);
                loginError.put("Message", "wrong login/server side");
                output.writeObject(loginError);
                return null;
            }

            String role;

            if (Helpers.verifyPassword(connection, userID, password)){
                //output.writeUTF("login good");
                JSONObject loginResponse = new JSONObject();
                loginResponse.put("Status", 0);
                loginResponse.put("Message", "login good");
                output.writeObject(loginResponse);
                role = Queries.getUserRole(connection, userID);
                return switch (role) {
                    case "customer" -> new Customer(userID, username, connection);
                    //case "admin" -> new Admin(userID, connection);  more roles later
                    default -> null;
                };
            }
            //output.writeUTF("wrong password");
            JSONObject loginResponse = new JSONObject();
            loginResponse.put("Status", 2);
            loginResponse.put("Message", "wrong password");
            output.writeObject(loginResponse);
            return null;

        } catch (IOException e) {
            throw new RuntimeException("Error handling client: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}