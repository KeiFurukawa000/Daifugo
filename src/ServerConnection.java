import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
        if (cmd[0].equals(CREATELOBBY)) onReceivedRequest_CREATELOBBY(cmd, sc);
        else if (cmd[0].equals(JOINLOBBY)) onReceivedRequest_JOINLOBBY(cmd, sc);
        else if (cmd[0].equals(STARTGAME)) onReceivedRequest_STARTGAME(cmd, sc);
        else if (cmd[0].equals(READY)) onReceivedRequest_READY(cmd, sc);
        else if (cmd[0].equals(UNREADY)) onReceivedRequest_UNREADY(cmd, sc);
        else if (cmd[0].equals(PUT)) onReceivedRequest_PUT(cmd, sc);
        else if (cmd[0].equals(PASS)) onReceivedRequest_PASS(cmd, sc);
    }

    private void onReceivedRequest_PASS(String[] cmd, SocketChannel sc) throws IOException {
        callback.Pass(cmd[1], cmd[2]);
    }

    private void onReceivedRequest_PUT(String[] cmd, SocketChannel sc) throws IOException {
        callback.Put(cmd[1], cmd[2], cmd[3]);
    }

    private void onReceivedRequest_UNREADY(String[] cmd, SocketChannel sc) throws IOException {
        callback.UnreadyPlayer(cmd[1], cmd[2]);
        Send(sc, UNREADY, OK);
    }

    private void onReceivedRequest_READY(String[] cmd, SocketChannel sc) throws IOException {
        callback.ReadyPlayer(cmd[1], cmd[2], sc);
        Send(sc, READY, OK);
    }

    private void onReceivedRequest_CREATELOBBY(String[] cmd, SocketChannel sc) throws IOException {
        if (callback.CanCreateLobby(cmd[1])) {
            callback.CreateLobby(cmd[1], cmd[2], sc);
            Send(sc, CREATELOBBY, OK);
        }
        else Send(sc, CREATELOBBY, FAULT);
    }

    private void onReceivedRequest_JOINLOBBY(String[] cmd, SocketChannel sc) throws IOException {
        if (callback.CanJoinLobby(cmd[1], cmd[2], cmd[3])) {
            callback.JoinLobby(cmd[1], cmd[3], sc);
            Send(sc, JOINLOBBY, OK);
        }
        else Send(sc, JOINLOBBY, FAULT);
    }

    private void onReceivedRequest_STARTGAME(String[] cmd, SocketChannel sc) throws IOException {
        if (callback.CanStartGame(cmd[1], cmd[2])) {
            callback.StartGame(cmd[1]);
            ArrayList<SocketChannel> scs = callback.GetAllSocketInLobby(cmd[1]);
            for (int i = 0; i < scs.size(); i++) Send(scs.get(i), STARTGAME, OK);
        }
        else Send(sc, STARTGAME, FAULT);
    }
}

interface IServer {
    boolean CanCreateLobby(String roomName);
    void CreateLobby(String roomName, String hostName, SocketChannel sc);
    boolean CanJoinLobby(String roomName, String password, String guestName);
    void JoinLobby(String roomName, String guestName, SocketChannel sc);
    boolean CanStartGame(String roomName, String hostName);
    void StartGame(String roomName);
    ArrayList<SocketChannel> GetAllSocketInLobby(String roomName);
    void ReadyPlayer(String roomName, String playerName, SocketChannel sc);
    void UnreadyPlayer(String roomName, String playerName);
    void Put(String roomName, String playerName, String Cards);
    void Pass(String roomName, String playerName);
}
