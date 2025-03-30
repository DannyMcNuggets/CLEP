import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;

public class ConnectionHandler implements Runnable {
    final Socket socket;
    Connection connection;

    public ConnectionHandler(Socket socket, Connection connection) {
        this.socket = socket;
        this.connection = connection;
    }

    @Override
    public void run() {
        try (socket) {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(new Packet(Queries.getAllOrders(connection))); // basic query for testing
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
