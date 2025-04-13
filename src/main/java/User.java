import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class User {
    protected int userID;
    protected Queries queries;
    protected Helpers helpers;

    public User(int userID, Queries queries, Helpers helpers){
        this.userID = userID;
        this.queries = queries;
        this.helpers = helpers;
    }

    void handleSession(Socket socket) throws IOException, SQLException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        while (true) {
            output.writeUTF(getMenu());
            output.flush();

            String command = input.readUTF(); // Read command from client
            handleCommand(command, input, output);
            if (command.equals("LOGOUT")) {
                socket.close();
                break;
            }
        }
    }

    abstract String getMenu();

    abstract void handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException;
}
