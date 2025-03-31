import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;

public abstract class User {
    private int userID;

    public User(int userID, String username, Connection connection){
        this.userID = userID;
    }

    abstract void handleSession(Socket socket) throws IOException;

}
