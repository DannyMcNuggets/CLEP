import java.io.Serializable;
import java.util.List;

public class Packet implements Serializable {
    private List<String> data;

    public Packet(List<String> data) {
        this.data = data;
    }

    public List<String> getData() {
        return data;
    }
}
