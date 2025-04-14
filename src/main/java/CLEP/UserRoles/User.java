package CLEP.UserRoles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.Queries;


public abstract class User {
    protected int userID;
    protected Queries queries;
    protected Helpers helpers;

    public User(int userID, Queries queries, Helpers helpers){
        this.userID = userID;
        this.queries = queries;
        this.helpers = helpers;
    }

    public void handleSession(Socket socket) throws IOException, SQLException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        while (true) {
            output.writeUTF(getMenu());
            output.flush();

            String command = input.readUTF(); // Read command from client
            int code = handleCommand(command, input, output);
            output.flush();
            if (code == 3) {
                System.out.println("we are closing socket like right now");
                socket.close();
                break;
            }
            input.readUTF();
        }
    }

    abstract String getMenu();

    abstract int handleCommand(String command, DataInputStream in, DataOutputStream out) throws IOException, SQLException;
}
