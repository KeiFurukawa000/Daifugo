import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class DaifugoServer {

    private ServerSocketChannel socket;
    private Selector selector;
    private ByteBuffer reader;

    private ServerConnection connection;
    private LobbyList lobbyList;
    

    public static void main(String[] args) throws NumberFormatException, IOException {
        System.out.println("Server has been launched.");
        DaifugoServer server = new DaifugoServer(12);
        server.Open(args[0], Integer.parseInt(args[1]));
    }

    DaifugoServer(int maxCount) {
        lobbyList = new LobbyList(maxCount);
        reader = ByteBuffer.allocate(512);
        connection = new ServerConnection();
    }

    public void Open(String addr, int port) throws IOException {
        socket = ServerSocketChannel.open();
        socket.bind(new InetSocketAddress(addr, port));
        socket.configureBlocking(false);
        selector = Selector.open();
        socket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server has been opened. (IP: " + addr + ", PORT: " + port + ")");
    }

    public void Listen() throws IOException {
        while (socket.isOpen()) {
            while (selector.selectNow() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) { onAccept(); }
                    else if (key.isReadable()) {
                        // データの読み取り
                        SocketChannel sc = (SocketChannel)key.channel();
                        String[] cmd = Read(sc);

                        Action(cmd, sc);
                    }
                }
            }
        }
    }

    private void onAccept() throws IOException {
        SocketChannel sc = socket.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    private String[] Read(SocketChannel sc) throws IOException {
        reader.clear();
        sc.read(reader);
        reader.flip();
        return StandardCharsets.UTF_8.decode(reader).toString().split(" ");
    }

    private void Action(String[] cmd, SocketChannel sc) {
        String action = cmd[0];
        if (action.equals(connection.CREATELOBBY)) {
            String lobbyName = cmd[1];
            String hostName = cmd[2];
            if (lobbyList.Contains(lobbyName)) {
                Lobby newLobby = new Lobby(lobbyName, hostName, sc);
                lobbyList.AddLobby(newLobby);
                connection.AnswerCreateLobby(true, sc);
            }
            else {
                connection.AnswerCreateLobby(false, sc);
            }
        }
        else if (action.equals(connection.JOINLOBBY)) {
            String lobbyName = cmd[1];
            String password = cmd[2];
            String guestName = cmd[3];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            if (lobby.CanJoin(guestName, password)) {
                lobby.Add(guestName, sc);
                Member[] members = lobby.GetMemberList();
                connection.AnswerJoinLobby(members, sc);
                connection.SendJoinMember(guestName, members);
            }
        }
    }
}