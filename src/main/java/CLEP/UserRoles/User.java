package CLEP.UserRoles;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
    protected IOUnit io;

    public User(int userID, Queries queries, Helpers helpers, IOUnit io) throws AddressException, SQLException {
        this.userID = userID;
        this.queries = queries;
        this.helpers = helpers;
        this.io = io;
        mailSender = new MailSender("cleptest4@gmail.com", "", new InternetAddress("cleptest4@gmail.com"), userID, queries);
    }

    public void handleSession() throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        while (true) {
            io.write(getMenu());
            String command = io.read();
            if (!handleCommand(command)) {
                break;
            }
        }
    }

    abstract String getMenu();

    abstract boolean handleCommand(String command) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
}
