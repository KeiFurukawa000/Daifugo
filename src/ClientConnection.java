import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientConnection extends Connection {

    private SocketChannel socket;

    ClientConnection(SocketChannel socket) {
        this.socket = socket;
    }

    public void Send(String... cmd) {
        String str = String.join(" ", cmd);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(str);
        try {
            socket.write(bb);
        } catch (IOException e) {} 
    }

    public void RequestCreateLobby(String roomName, String hostName) {
        Send(CREATELOBBY, roomName, hostName);
    }

    public void RequestJoinLobby(String roomName, String password, String guestName) {
        Send(JOINLOBBY, roomName, password, guestName);
    }

    public void RequestStartGame(String roomName, String hostName) {
        Send(STARTGAME, roomName, hostName);
    }
}
