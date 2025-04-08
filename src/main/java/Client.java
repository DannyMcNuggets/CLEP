import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {

    // TODO: break into different methods!
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); // Read from console
             DataInputStream dataInput = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream())) {

            int option = makeAChoice(userInput, dataInput, dataOutput);

            switch(option){
                case 1 -> {
                    if (!login(userInput, dataInput, dataOutput)) return;
                }
                case 2 -> {
                    return; // nothing for now
                }
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

                if (choice.equals("3")) break;
            }

        }
    }

    public static int makeAChoice(BufferedReader userInput, DataInputStream dataInput, DataOutputStream dataOutput) throws IOException {
        while (true) {
            System.out.println(dataInput.readUTF());  // "Login or register"
            int choice = Integer.parseInt(userInput.readLine());

            if (choice == 1 || choice == 2){
                dataOutput.writeInt(choice);
                return choice;
            }
            System.out.print("just write 1 plz...");
        }
    }

    public static boolean login(BufferedReader userInput, DataInputStream dataInput, DataOutputStream dataOutput) throws IOException {
        System.out.println(dataInput.readUTF());  // "Enter username:"
        String username = userInput.readLine();
        dataOutput.writeUTF(username);

        System.out.println(dataInput.readUTF());  // "Enter password:"
        String password = userInput.readLine();
        dataOutput.writeUTF(password);

        // check if login was successful
        String loginResponse = dataInput.readUTF();
        System.out.println(loginResponse);
        if (loginResponse.contains("Invalid")) { // TODO: REWORK TO PROTOCOL CODES!!!!
            System.out.println("Exiting...");
            return false;
        }
        return true;
    }


    public static boolean register(BufferedReader userInput, DataInputStream dataInput, DataOutputStream dataOutput){

        return true;
    }
}
