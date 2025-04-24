package CLEP.UserRoles;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
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

    public void handleSession(IOUnit io) throws IOException, SQLException {

        while (true) {
            io.write(getMenu());

            String command = io.read();
            int code = handleCommand(command, io);
            if (code == 3) {
                System.out.println("we are closing socket like right now");
                break;
            }
            io.read();
        }
    }

    abstract String getMenu();

    abstract int handleCommand(String command, IOUnit io) throws IOException, SQLException;
}
