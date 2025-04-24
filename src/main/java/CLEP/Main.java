package CLEP;

import CLEP.UserRoles.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

import CLEP.auth.Auth;
import CLEP.auth.Register;
import CLEP.util.ClientConnection;
import CLEP.util.Helpers;
import CLEP.util.IOUnit;
import CLEP.util.Queries;

public class Main {
    private static final String DB_PATH = "database.db";

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
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