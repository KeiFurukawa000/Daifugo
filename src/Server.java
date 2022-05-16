import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Server implements IServer {

    private ServerSocketChannel socket;
    private ServerConnection connect;
    private Selector selector;
    private LobbyList lobbyList;


    public static void main(String[] args) throws NumberFormatException, IOException {
        System.out.println("Server has been launched.");
        Server server = new Server(12);
        server.Open(args[0], Integer.parseInt(args[1]));
    }

    Server(int maxCount) {
        lobbyList = new LobbyList(maxCount);
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
                    else if (key.isReadable()) { connect.Read((SocketChannel)key.channel()); }
                }
            }
        }
    }

    private void onAccept() throws IOException {
        SocketChannel sc = socket.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public boolean CanCreateLobby(String lobbyName) {
        if (!lobbyList.Contains(lobbyName) && lobbyList.GetCurrentCount() < lobbyList.GetMaxCount()) return true;
        return false;
    }

    @Override
    public void CreateLobby(String lobbyName, String hostName, SocketChannel sc) {
        Lobby lobby = new Lobby(lobbyName, hostName);
        lobbyList.AddLobby(lobbyName, lobby);
    }

    @Override
    public boolean CanJoinLobby(String lobbyName, String password, String guestName) {
        if (lobbyList.Contains(lobbyName)) {
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            return lobby.CanAddPlayer(password, guestName);
        }
        return false;
    }

    @Override
    public void JoinLobby(String lobbyName, String guestName, SocketChannel sc) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        lobby.AddPlayer(guestName, sc);
    }

    @Override
    public boolean CanStartGame(String lobbyName, String hostName) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        return lobby.CanStartGame(); 
    }

    @Override
    public void StartGame(String lobbyName) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        lobby.StartGame();
    }

    @Override
    public ArrayList<SocketChannel> GetAllSocketInLobby(String lobbyName) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        return lobby.GetAllSocket();
    }

    @Override
    public void ReadyPlayer(String lobbyName, String playerName, SocketChannel sc) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        lobby.AddReadyPlayer(new Participant(playerName, sc));
    }

    @Override
    public void UnreadyPlayer(String lobbyName, String playerName) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        lobby.RemoveReadyPlayer(playerName);
    }

    @Override
    public void Put(String lobbyName, String playerName, String cards) {
        Lobby lobby = lobbyList.GetLobby(lobbyName);
        Game game = lobby.GetGame();
        game.Put(playerName, cards);
    }

    @Override
    public void Pass(String lobbyName, String playerName) {
        // TODO Auto-generated method stub
        
    }   
}

class LobbyList {
    private HashMap<String, Lobby> lobbyList;
    private int maxCount;

    LobbyList(int maxCount) {
        this.maxCount = maxCount;
    }

    public void AddLobby(String lobbyName, Lobby lobby) {
        lobbyList.put(lobbyName, lobby);
    }

    public void RemoveLobby(String lobbyName) {
        lobbyList.remove(lobbyName);
    }

    public Lobby GetLobby(String lobbyName) {
        return lobbyList.get(lobbyName);
    }

    public int GetMaxCount() {
        return maxCount;
    }

    public int GetCurrentCount() {
        return lobbyList.size();
    }

    public boolean Contains(String lobbyName) {
        return lobbyList.containsKey(lobbyName);
    }
}
