package CLEP;

import java.io.*;
import java.net.ServerSocket;
import java.sql.*;
import CLEP.util.ClientConnection;

public class Main {
    private static final String DB_PATH = "database.db";

    public static void main(String[] args) throws SQLException, IOException {
        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                new Thread(new ClientConnection(ss.accept(), initiateConnection(DB_PATH))).start();
            }
        }
    }

    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }
}