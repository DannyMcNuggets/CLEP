import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;

public class Customer extends User {
    public Customer(int userID, String username, Connection connection) {
        super(userID, username, connection);
    }

    @Override
    void handleSession(Socket socket) throws IOException {

    }
}
