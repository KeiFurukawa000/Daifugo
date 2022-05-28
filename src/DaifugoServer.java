import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class DaifugoServer implements IDaifugoServer {

    private ServerSocketChannel socket;
    private Selector selector;
    private ByteBuffer reader;

    private AccountList accountList;
    private LobbyList lobbyList;
    

    public static void main(String[] args) throws NumberFormatException, IOException {
        System.out.println("Server has been launched.");
        DaifugoServer server = new DaifugoServer(12);
        server.Open(args[0], Integer.parseInt(args[1]));
        server.Listen();
    }

    DaifugoServer(int maxCount) {
        lobbyList = new LobbyList(maxCount);
        accountList = new AccountList();
        reader = ByteBuffer.allocate(512);
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
                        String accountName = cmd[0];
                        Account account = accountList.Get(accountName);
                        if (account == null) {
                            account = new Account(accountName, sc, this);
                        }
                        account.Select(Arrays.copyOfRange(cmd, 1, cmd.length));
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
        if (!sc.isConnected()) sc.close();
        reader.clear();
        sc.read(reader);
        reader.flip();
        return StandardCharsets.UTF_8.decode(reader).toString().split(" ");
    }

    /** Implements of IDaifugoServer */
    @Override
    public boolean CreateAccount(String name, SocketChannel sc) {
        if (name.isEmpty() && name.isBlank()) return false;
        boolean result = accountList.Add(new Account(name, sc, this));
        return result;
    }

    @Override
    public void DeleteAccount(Account account) {
        accountList.Remove(account);
    }

    @Override
    public Lobby CreateLobby(String lobbyName, String hostName, SocketChannel sc) {
        Lobby newLobby = new Lobby(lobbyName, hostName, sc);
        boolean result = lobbyList.add(newLobby);
        return result ? newLobby : null;
    }

    @Override
    public Lobby JoinLobby(String lobbyName, String password, String guestName, SocketChannel sc) {
        Lobby lobby = lobbyList.get(lobbyName);
        if (lobby != null && lobby.canJoin(guestName, password)) {
            lobby.add(new Member(guestName, false, sc, lobby));
            return lobby;
        }
        return null;
    }
}

interface IDaifugoServer {
    boolean CreateAccount(String name, SocketChannel sc);
    void DeleteAccount(Account account);
    Lobby CreateLobby(String lobbyName, String hostName, SocketChannel sc);
    Lobby JoinLobby(String lobbyName, String password, String guestName, SocketChannel sc);
}

/**
 * アカウントをまとめるクラス
 */
class AccountList {
    private HashMap<String, Account> list;

    AccountList() {
        list = new HashMap<>();
    }

    /**
     * アカウントリストにアカウントを追加する
     * @param account 追加するアカウント
     * @return boolean アカウントを追加できたかどうか
     */
    public boolean Add(Account account) {
        if (Contains(account.GetName()) || account.GetName().equals(Connection.CREATEACCOUNT)) {
            return false;
        }
        list.put(account.GetName(), account);
        return true;
    }

    /**
     * アカウントを削除する
     * @param account 削除するアカウント
     */
    public void Remove(Account account) {
        list.remove(account.GetName());
    }

    /**
     * アカウントを取得する
     * @param name 取得したいアカウント名
     * @return Account 取得するアカウント
     */
    public Account Get(String name) {
        return list.get(name);
    }

    /**
     * アカウントがアカウントリストに存在しているかを調べる
     * @param name 存在しているか知りたいアカウント名
     * @return boolean 存在しているかどうか
     */
    private boolean Contains(String name) {
        return list.containsKey(name);
    }
}

class Account {
    private String name;
    private SocketChannel sc;
    private IDaifugoServer callback;
    private IClientConnectable connection;

    private Member member;
    private Player player;

    Account(String name, SocketChannel sc, IDaifugoServer callback) {
        this.name = name;
        this.sc = sc;
        this.callback = callback;
        connection = new ServerConnection(this.sc);
    }

    public String GetName() {
        return name;
    }

    public void Select(String[] cmd) {
        String type = cmd[0];

        if (type.equals(Connection.ACCOUNT)) {
            Action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
        else if (type.equals(Connection.MEMBER)) {
            if(member != null) member.action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
        else if (type.equals(Connection.PLAYER)) {
            player.Action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
    }

    private void Action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.CREATEACCOUNT)) {
            CreateAccount();
            System.out.println(String.join(" ", Connection.ACCOUNT, Connection.CREATEACCOUNT));
        }
        else if (action.equals(Connection.DELETEACCOUNT)) {
            DeleteAccount();
        }
        else if (action.equals(Connection.CREATELOBBY)) {
            CreateLobby(cmd[1]);
        }
        else if (action.equals(Connection.JOINLOBBY)) {
            JoinLobby(cmd[1], cmd[2]);
        }
    }

    public void CreateAccount() {
        boolean result = callback.CreateAccount(name, sc);
        connection.AnswerCreateAccount(result);
    }

    public void DeleteAccount() {
        callback.DeleteAccount(this);
    }

    public void CreateLobby(String lobbyName) {
        Lobby lobby = callback.CreateLobby(lobbyName, name, sc);
        if (lobby != null) connection.AnswerCreateLobby(true, lobby.getPassword());
        else connection.AnswerCreateLobby(false, null);
        this.member = lobby.get(name);
    }

    public void JoinLobby(String lobbyName, String password) {
       Lobby lobby = callback.JoinLobby(lobbyName, password, name, sc);
        if (lobby == null) {
            connection.AnswerJoinLobby(false, null, null);
        }
        else {
            this.member = lobby.get(name);
            Member[] members = lobby.getMembersAsArray();
            connection.AnswerJoinLobby(true, lobby.getName(), members);
        }
    }
}