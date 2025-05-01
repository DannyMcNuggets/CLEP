package CLEP.UserRoles;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.MailSender;
import CLEP.util.Queries;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;


public abstract class User {
    protected int userID;
    protected Queries queries;
    protected Helpers helpers;
    protected MailSender mailSender;

    public User(int userID, Queries queries, Helpers helpers) throws AddressException, SQLException {
        this.userID = userID;
        this.queries = queries;
        this.helpers = helpers;
        mailSender = new MailSender("", "", new InternetAddress("cleptest4@gmail.com"), userID, queries);
    }

    public void handleSession(IOUnit io) throws IOException, SQLException {
        while (true) {
            io.write(getMenu());
            String command = io.read();
            if (!handleCommand(command, io)) {
                break;
            }
            io.read();
        }
    }

    abstract String getMenu();

    abstract boolean handleCommand(String command, IOUnit io) throws IOException, SQLException;
}
