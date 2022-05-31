
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
    private final int READER_BUFSIZE = 512;

    private AccountList accountList;
    private LobbyList lobbyList;
    

    /**
     * メイン関数
     * @param args IPアドレス ポート番号 ex) localhost 8765
     * @throws NumberFormatException
     * @throws IOException
     */
    public static void main(String[] args) throws NumberFormatException, IOException {
        System.out.println("Server has been launched.");
        DaifugoServer server = new DaifugoServer(12);
        server.Open(args[0], Integer.parseInt(args[1]));
        server.Listen();
    }

    /**
     * コンストラクタ
     * インスタンスの生成と読み取り用バッファの容量割当
     * @param maxCount 最大ロビー数
     */
    DaifugoServer(int maxCount) {
        lobbyList = new LobbyList(maxCount);
        accountList = new AccountList();
        reader = ByteBuffer.allocate(READER_BUFSIZE);
    }

    /**
     * サーバーの開放
     * @param addr 開放するサーバーのIPアドレス
     * @param port 開放するポート番号
     * @throws IOException 例外
     */
    public void Open(String addr, int port) throws IOException {
        socket = ServerSocketChannel.open();
        socket.bind(new InetSocketAddress(addr, port));
        socket.configureBlocking(false);
        selector = Selector.open();
        socket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server has been opened. (IP: " + addr + ", PORT: " + port + ")");
    }

    /**
     * クライアント送信の読み取り
     * @throws IOException
     */
    public void Listen() throws IOException {
        while (socket.isOpen()) {
            while (selector.selectNow() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) { onAccept(); }
                    else if (key.isReadable()) {
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

    /**
     * クライアントへの通信許可
     * 初めてクライアントからの接続が行われた際に呼び出される
     * @throws IOException
     */
    private void onAccept() throws IOException {
        SocketChannel sc = socket.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    /**
     * クライアントから送られたバッファをコマンドへ変換する
     * @param sc 送信したクライアントのソケットチャンネル
     * @return コマンド
     * @throws IOException
     */
    private String[] Read(SocketChannel sc) throws IOException {
        if (!sc.isConnected()) sc.close();
        reader.clear();
        sc.read(reader);
        reader.flip();
        return StandardCharsets.UTF_8.decode(reader).toString().split(" ");
    }

    /** Implements of IDaifugoServer */
    @Override
    public boolean onReceiveCreateAccountRequest(String name, SocketChannel sc) {
        if (name.isEmpty() && name.isBlank()) return false;
        boolean result = accountList.Add(new Account(name, sc, this));
        return result;
    }

    @Override
    public void onReceiveDeleteAccountRequest(Account account) {
        accountList.Remove(account);
    }

    @Override
    public Lobby onReceiveCreateLobbyRequest(String lobbyName, String hostName, SocketChannel sc) {
        Lobby newLobby = new Lobby(lobbyName, hostName, sc);
        boolean result = lobbyList.add(newLobby);
        return result ? newLobby : null;
    }

    @Override
    public Lobby onReceiveJoinLobbyRequest(String lobbyName, String password, String guestName, SocketChannel sc) {
        Lobby lobby = lobbyList.get(lobbyName);
        if (lobby != null && lobby.canJoin(guestName, password)) {
            lobby.add(new Member(guestName, false, sc, lobby));
            return lobby;
        }
        return null;
    }
}

interface IDaifugoServer {
    /**
     * クライアントからアカウント作成要求があったときに呼び出される
     * @param name クライアントの名前
     * @param sc クライアントのソケットチャンネル
     * @return 要求が承認されたかどうか
     */
    boolean onReceiveCreateAccountRequest(String name, SocketChannel sc);

    /**
     * クライアントからアカウント削除要求があったときに呼び出される
     * @param account クライアントのアカウント
     */
    void onReceiveDeleteAccountRequest(Account account);

    /**
     * クライアントからロビー作成要求があったときに呼び出される
     * @param lobbyName 作成するロビーの名前
     * @param hostName 作成要求をしたクライアントの名前
     * @param sc クライアントのソケットチャンネル
     * @return 作成したロビー. 要求が棄却された場合はnullを返す
     */
    Lobby onReceiveCreateLobbyRequest(String lobbyName, String hostName, SocketChannel sc);

    /**
     * クライアントからロビー入室要求があったときに呼び出される
     * @param lobbyName 入室するロビーの名前
     * @param password 入室するロビーのパスワード
     * @param guestName 入室するクライアントの名前
     * @param sc クライアントのソケットチャンネル
     * @return 入室するロビーの名前
     */
    Lobby onReceiveJoinLobbyRequest(String lobbyName, String password, String guestName, SocketChannel sc);
}

/**
 * アカウントをまとめるクラス
 */
class AccountList {
    private HashMap<String, Account> list;

    /**
     * コンストラクタ
     * ハッシュマップの生成
     */
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

class Account implements IAccount {
    private String name;
    private SocketChannel sc;
    private IDaifugoServer callback;
    private IClientConnectable connection;

    private Member member;
    private Player player;

    /**
     * コンストラクタ
     * @param name アカウントの名前
     * @param sc アカウントのソケットチャンネル
     * @param callback クライアント要求コールバック
     */
    Account(String name, SocketChannel sc, IDaifugoServer callback) {
        this.name = name;
        this.sc = sc;
        this.callback = callback;
        connection = new ServerConnection(this.sc);
    }

    /**
     * アカウントの名前を取得
     * @return アカウントの名前
     */
    public String GetName() {
        return name;
    }

    /**
     * コマンドを読み取り、どのアクタでコマンドを実行するかを決める
     * @param cmd コマンド
     */
    public void Select(String[] cmd) {
        String type = cmd[0];

        if (type.equals(Connection.ACCOUNT)) {
            Action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
        else if (type.equals(Connection.MEMBER)) {
            if(member != null) member.action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
        else if (type.equals(Connection.PLAYER)) {
            member.getPlayer().Action(Arrays.copyOfRange(cmd, 1, cmd.length));
        }
    }

    /**
     * アカウントとしてのアクションを行う
     * アカウントではアカウント作成、アカウント削除、ロビー作成、ロビー入室が行える
     * @param cmd コマンド
     */
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

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * アカウント作成を行う
     * <p>大富豪サーバークラスへアカウント作成をコールバックし、クライアントへ其の結果を返す
     */
    public void CreateAccount() {
        boolean result = callback.onReceiveCreateAccountRequest(name, sc);
        connection.AnswerCreateAccount(result);
    }

    /**
     * アカウント削除を行う
     * <p>大富豪サーバークラスへアカウント削除をコールバックするが、クライアントへその結果は返さない
     */
    public void DeleteAccount() {
        callback.onReceiveDeleteAccountRequest(this);
    }

    /**
     * ロビー作成を行う
     * <p>大富豪サーバークラスへロビー作成をコールバックし、返ってきた結果をクライアントへ返す
     * @param lobbyName 作成するロビーの名前
     */
    public void CreateLobby(String lobbyName) {
        Lobby lobby = callback.onReceiveCreateLobbyRequest(lobbyName, name, sc);
        if (lobby != null) { 
            connection.AnswerCreateLobby(true, lobby.getPassword());
            this.member = lobby.get(name);
        }
        else connection.AnswerCreateLobby(false, null);
    }

    /**
     * ロビー入室を行う
     * <p>大富豪サーバークラスへロビー入室をコールバックし、返ってきた結果をクライアントへ返す
     * <p>ロビー入室が承認されたら、結果とロビーの名前、すでに入室しているメンバーの名前/状態を返す
     * @param lobbyName
     * @param password
     */
    public void JoinLobby(String lobbyName, String password) {
       Lobby lobby = callback.onReceiveJoinLobbyRequest(lobbyName, password, name, sc);
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

interface IAccount {
    void setPlayer(Player player);
}