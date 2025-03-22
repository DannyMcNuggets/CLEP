import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Connection connection;
    private static final String DB_PATH = "database.db";
    private static final String getOrdersList = "SELECT * FROM orders";

    public static void main(String[] args) throws SQLException, IOException {
        connection = initiateConnection(DB_PATH);

        for (String line : getSELECTasSTRING(getOrdersList)){ // just an example
            System.out.println(line);
        }

        try (ServerSocket ss = new ServerSocket(8080)) {
            while (true) {
                try (Socket socket = ss.accept()) {
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(new Packet(getSELECTasSTRING(getOrdersList)));
                }
            }
        }
    }

    public static Connection initiateConnection(String pathname) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + pathname);
    }

    private static List<String> getSELECTasSTRING(String query) throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        List<String> allRows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();

        while (rs.next()) {
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) line.append(", ");
                line.append(rs.getString(i));
            }
            allRows.add(line.toString());
        }

        return allRows;

        // once clients implemented:
        //out.writeInt(allRows.length());
        //out.writeUTF(*by bytes, or mb List<String>, or mb line by line*));
    }


}