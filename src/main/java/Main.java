import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Main {
    private static Connection connection;
    private static final String DB_PATH = "database.db";

    public static void main(String[] args) throws SQLException, IOException {
        connection = initiateConnection(DB_PATH);

        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                try (Socket socket = ss.accept()) {
                    User user = handleClient(socket);
                    user.handleSession(socket);
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(new Packet(Queries.getAllOrders(connection))); // basic query for testing
                }
            }
        }
    }

    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }

    // TODO: handle client log in (provide username and password), then verify his role and create object
    private static User handleClient(Socket socket) throws RuntimeException {
        User user = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter wr = new PrintWriter(socket.getOutputStream(), true);

            wr.println("hello, must be working");
            String username = br.readLine();
            wr.println("no way it is working. give password");
            String password = br.readLine();
            System.out.println("wow: " + username + " " + password);

            // verify id, password, role and assign role to user

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return user;
    }


}