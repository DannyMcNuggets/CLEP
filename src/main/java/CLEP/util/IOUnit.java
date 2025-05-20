package CLEP.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IOUnit implements AutoCloseable{

    protected DataInputStream input;
    protected DataOutputStream output;

    public IOUnit(Socket socket) throws IOException {

        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public String read() throws IOException {
        return input.readUTF();
    }

    public void write(String message) throws IOException {
        if (message == null || message.isBlank()) {
            message = "0";
        }
        output.writeUTF(message);

    }

    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws Exception {
        input.close();
        output.close();
    }
}
