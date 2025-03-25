import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Packet p = (Packet)input.readObject();

            for (List<String> row : p.getData()){
                for (String value : row){
                    System.out.print(value + " | ");
                }
                System.out.println();
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
