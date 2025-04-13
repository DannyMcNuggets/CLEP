import java.io.*;
import java.net.Socket;

public class Client {

    // TODO: break into different methods!
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             DataInputStream dataInput = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                String serverMessage = dataInput.readUTF();
                System.out.println(serverMessage);

                if (serverMessage.equalsIgnoreCase("Logging off...")) { // rework to codes
                    break;
                }

                String userReply = userInput.readLine();
                dataOutput.writeUTF(userReply);
            }
        }
    }

}
