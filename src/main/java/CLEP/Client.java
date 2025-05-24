package CLEP;

import CLEP.util.IOUnit;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             IOUnit io = new IOUnit(socket))
            {
                while (true) {
                    String serverMessage = io.read();
                    System.out.println(serverMessage);

                    if (serverMessage.equalsIgnoreCase("Logging off...") || serverMessage.equalsIgnoreCase("EXIT")) { // rework to codes
                        break;
                    }

                    String userReply = userInput.readLine();
                    io.write(userReply);
                }
            }
    }

}
