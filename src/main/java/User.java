import java.io.IOException;
import java.net.Socket;

public abstract class User {
    private int userID;

    public User(int userID, String username){
        this.userID = userID;
    }

    abstract void handleSession(Socket socket) throws IOException;

}
