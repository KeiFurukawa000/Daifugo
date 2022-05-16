import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ServerConnection extends Connection {

    private ByteBuffer reader;
    private IServer callback; 

    ServerConnection(IServer callback) {
        reader = ByteBuffer.allocate(512);
        this.callback = callback;
    }
    
    public void Send(SocketChannel socket, String... cmd) throws IOException {
        String str = String.join(" ", cmd);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(str);
        socket.write(bb);
    }

    public void Read(SocketChannel sc) throws IOException {
        reader.clear();
        sc.read(reader);
        reader.flip();
        String[] cmd = StandardCharsets.UTF_8.decode(reader).toString().split(" ");
        if (cmd[0].equals(CREATELOBBY)) {
            if (callback.CanCreateLobby(cmd[1])) {
                callback.CreateLobby(cmd[1], cmd[2], sc);
                Send(sc, CREATELOBBY, OK);
            }
            else Send(sc, CREATELOBBY, FAULT);
        }
        else if (cmd[0].equals(JOINLOBBY)) {
            if (callback.CanJoinLobby(cmd[1], cmd[2], cmd[3])) {
                callback.JoinLobby(cmd[1], cmd[3], sc);
                Send(sc, JOINLOBBY, OK);
            }
            else Send(sc, JOINLOBBY, FAULT);
        }
        else if (cmd[0].equals(STARTGAME)) {
            if (callback.CanStartGame(cmd[1], cmd[2])) {
                callback.StartGame(cmd[1]);
                SocketChannel[] scs = callback.GetAllSocketInLobby(cmd[1]);
                for (int i = 0; i < scs.length; i++) Send(scs[i], STARTGAME, OK);
            }
            else Send(sc, STARTGAME, FAULT);
        }
    }
}

interface IServer {
    boolean CanCreateLobby(String roomName);
    void CreateLobby(String roomName, String hostName, SocketChannel sc);
    boolean CanJoinLobby(String roomName, String password, String guestName);
    void JoinLobby(String roomName, String guestName, SocketChannel sc);
    boolean CanStartGame(String roomName, String hostName);
    void StartGame(String roomName);
    SocketChannel[] GetAllSocketInLobby(String roomName);
}
