package CLEP.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IOUnit {

    protected DataInputStream input;
    protected DataOutputStream output;

    public IOUnit(DataInputStream input, DataOutputStream output){
        this.input = input;
        this.output = output;
    }

    public String read() throws IOException {
        return input.readUTF();
    }

    public void write(String message) throws IOException {
        output.writeUTF(message);
    }

    public void flush() throws IOException {
        output.flush();
    }

}
