import java.nio.channels.SocketChannel;

public class Participant {

    private String name;
    private SocketChannel socket;

    Participant(String name, SocketChannel socket) {
        this.name = name;
        this.socket = socket;
    }

    public String GetName() {
        return name;
    }

    public SocketChannel GetSocket() {
        return socket;
    }
}
