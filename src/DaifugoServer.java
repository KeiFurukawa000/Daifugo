import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

public class DaifugoServer implements IDaifugoServer {

    private ServerSocketChannel socket;
    private Selector selector;
    private ByteBuffer reader;

    private ServerConnection connection;
    private AccountList accountList;
    private LobbyList lobbyList;
    

    public static void main(String[] args) throws NumberFormatException, IOException {
        System.out.println("Server has been launched.");
        DaifugoServer server = new DaifugoServer(12);
        server.Open(args[0], Integer.parseInt(args[1]));
    }

    DaifugoServer(int maxCount) {
        lobbyList = new LobbyList(maxCount);
        accountList = new AccountList();
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
        if (cmd[0].equals(connection.CREATEACCOUNT)) {
            boolean result = accountList.Add(new Account(cmd[1], sc, this, connection));
            connection.AnswerCreateAccount(result, sc);
        }
        else {
            Account account = accountList.GetAccount(cmd[0]);
            
        }
    }
     /*
    private void Action(String[] cmd, SocketChannel sc) {
        String action = cmd[0];
        if (action.equals(connection.CREATELOBBY)) {
            String lobbyName = cmd[1];
            String hostName = cmd[2];
            boolean result = lobbyList.AddLobby(new Lobby(lobbyName, hostName, sc));
            connection.AnswerCreateLobby(result, sc);
        }
        else if (action.equals(connection.JOINLOBBY)) {
            String lobbyName = cmd[1];
            String password = cmd[2];
            String guestName = cmd[3];
            if (lobbyList.Contains(lobbyName)) {
                Lobby lobby = lobbyList.GetLobby(lobbyName);
                if (lobby.CanJoin(guestName, password)) {
                    Member[] joinedmembers = lobby.GetMemberList();
                    lobby.Add(guestName, sc);
                    connection.AnswerJoinLobby(true, joinedmembers, sc);
                    connection.SendJoinMember(guestName, joinedmembers);
                    return;
                }
            }
            connection.AnswerJoinLobby(false, null, sc);
        }
        else if (action.equals(connection.LEAVELOBBY)) {
            String lobbyName = cmd[1];
            String memberName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            lobby.Remove(memberName);
            Member[] joinedmembers = lobby.GetMemberList();
            connection.SendLeaveMember(memberName, joinedmembers);
        }
        else if (action.equals(connection.READY)) {
            String lobbyName = cmd[1];
            String memberName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Member member = lobby.GetMember(memberName);
            member.Ready();
            Member[] members = lobby.GetMemberList();
            connection.SendReadyMember(memberName, members);
        }
        else if (action.equals(connection.UNREADY)) {
            String lobbyName = cmd[1];
            String memberName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Member member = lobby.GetMember(memberName);
            member.Unready();
            Member[] members = lobby.GetMemberList();
            connection.SendUnreadyMember(memberName, members);
        }
        else if (action.equals(connection.STARTGAME)) {
            String lobbyName = cmd[1];
            String memberName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Member[] members = lobby.GetMemberList();
            connection.SendStartGame(members);
            lobby.StartGame();
        }
        else if (action.equals(connection.PUT)) {
            String lobbyName = cmd[1];
            String playerName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Game game = lobby.GetGame();
            if (!cmd[3].equals("NONE")) {
                String[] cardStr = Arrays.copyOfRange(cmd, 3, cmd.length);
                Card[] cards = connection.StrToCard(cardStr);
                game.SetStage(cards);
            }
            Member[] members = lobby.GetMemberList();
            connection.SendStage(members, game.GetStage());
        }
        else if (action.equals(connection.WIN)) {
            String lobbyName = cmd[1];
            String playerName = cmd[2];
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Game game = lobby.GetGame();
            Party party = game.GetParty();
            Player player = party.GetHead();
            player.Win();
        }
    }
    */

    /** Implements of IDaifugoServer */
    @Override
    public boolean CreateAccount(String name, SocketChannel sc) {
        boolean result = accountList.Add(new Account(name, sc, this, connection));
        connection.AnswerCreateAccount(result, sc);
        return result;
    }

    @Override
    public void DeleteAccount(Account account) {
        accountList.Remove(account);
    }

    @Override
    public boolean CreateLobby(String lobbyName, String hostName, SocketChannel sc) {
        boolean result = lobbyList.Add(new Lobby(lobbyName, hostName, sc));
        return result;
    }

    @Override
    public Lobby JoinLobby(String lobbyName, String guestName, SocketChannel sc) {
        if (lobbyList.Contains(lobbyName)) {
            Lobby lobby = lobbyList.GetLobby(lobbyName);
            Member member = lobby.Add(guestName, sc);
            return lobby;
        }
        return null;
    }
}

interface IDaifugoServer {
    boolean CreateAccount(String name, SocketChannel sc);
    void DeleteAccount(Account account);
    boolean CreateLobby(String lobbyName, String hostName, SocketChannel sc);
    Lobby JoinLobby(String lobbyName, String guestName, SocketChannel sc);
}

class AccountList {
    private HashMap<String, Account> list;

    AccountList() {
        list = new HashMap<>();
    }

    public boolean Add(Account account) {
        if (Contains(account.GetName()) || account.GetName().equals("CREATEACCOUNT")) {
            return false;
        }
        list.put(account.GetName(), account);
        return true;
    }

    public void Remove(Account account) {
        list.remove(account.GetName());
    }

    public boolean Contains(String name) {
        return list.containsKey(name);
    }

    public Account GetAccount(String name) {
        return list.get(name);
    }
}

class Account {
    private String name;
    private SocketChannel sc;
    private IDaifugoServer callback;
    private IClientConnectable connection;

    private Member member;
    private Player player;

    Account(String name, SocketChannel sc, IDaifugoServer callback, IClientConnectable connection) {
        this.name = name;
        this.sc = sc;
        this.callback = callback;
        this.connection = connection;
    }

    public String GetName() {
        return name;
    }

    public void DeleteAccount() {
        callback.DeleteAccount(this);
    }

    public void CreateLobby(String lobbyName) {
        boolean result = callback.CreateLobby(lobbyName, name, sc);
        connection.AnswerCreateAccount(result, sc);
    }

    public void JoinLobby(String lobbyName) {
       Lobby lobby = callback.JoinLobby(lobbyName, name, sc);
        if (member == null) {
            connection.AnswerJoinLobby(false, null, sc);
        }
        else {
            this.member = lobby.GetMember(name);
            Member[] members = lobby.GetMemberList();
            connection.AnswerJoinLobby(true, members, sc);
        }
    }

    public void ActionMember() {

    }

    public void ActionPlayer() {

    }
}