import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {

    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Client.class);

    // TODO: break into different methods!
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); // Read from console
             ObjectInputStream dataInput = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream dataOutput = new ObjectOutputStream(socket.getOutputStream())) {

            System.out.println("Enter username: ");
            String username = userInput.readLine();
            System.out.println("Enter password: ");
            String password = userInput.readLine();

            JSONObject loginRequest = new JSONObject();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            dataOutput.writeObject(loginRequest);

            JSONObject loginResponse = (JSONObject) dataInput.readObject();

            // Login Process
            /*System.out.println(dataInput.readUTF());  // "Enter username:"
            String username = userInput.readLine();
            dataOutput.writeUTF(username);

            System.out.println(dataInput.readUTF());  // "Enter password:"
            String password = userInput.readLine();
            dataOutput.writeUTF(password);

            // check if login was successful
            String loginResponse = dataInput.readUTF();*/
            System.out.println(loginResponse.get("Message"));
            if (Integer.valueOf(loginResponse.get("Status").toString()) != 0) { // TODO: REWORK TO PROTOCOL CODES!!!!
                System.out.println("Exiting...");
                return;
            }

            // handle session menu
            while (true) {
                String menu = dataInput.readUTF(); // Receive menu from server
                System.out.println(menu);

                String choice = userInput.readLine();
                dataOutput.writeUTF(choice); // Send command
                dataOutput.flush();

                String response = dataInput.readUTF(); // Server response
                System.out.println(response);

                if (choice.equals("LOGOUT")) break;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // not much of point keeping it...
    public void tester(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
        Packet p = (Packet)objectInput.readObject();

        for (List<String> row : p.getData()){
            for (String value : row){
                System.out.print(value + " | ");
            }
            System.out.println();
        }
    }
}
