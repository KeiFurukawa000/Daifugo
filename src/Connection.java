import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javafx.application.Platform;
public class Connection implements IConnectable {
    protected SocketChannel socket;

    @Override
    public SocketChannel getSocket() {
        return socket;
    }

    protected void send(String msg) {
        try {
            socket.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(msg);
    }

    protected void send(String msg, SocketChannel sc) {
        try {
            sc.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(msg);
    }

    protected String MakeCommand(String... strs) {
        return String.join(" ", strs);
    }
}

interface IConnectable {
    SocketChannel getSocket();

    String OK = "OK";
    String FAULT = "FAULT";

    String ACCOUNT = "ACCOUNT";
    String MEMBER = "MEMBER";
    String PLAYER = "PLAYER";

    String CREATEACCOUNT = "CREATEACCOUNT";
    String DELETEACCOUNT = "DELETEACCOUNT";
    String CREATELOBBY = "CREATELOBBY";
    String JOINLOBBY = "JOINLOBBY";
    String LEAVELOBBY = "LEAVELOBBY";
    String STARTGAME = "STARTGAME"; 
    String YOURTURN = "YOURTURN";
    String READY = "READY";
    String UNREADY = "UNREADY";
    String CHANGEHOST = "CHANGEHOST";
    String CHAT = "CHAT";
    String HAND = "HAND";
    String PLAYERTURN = "PLAYERTURN";
    String STAGE = "STAGE";
    String PUT = "PUT";
    String PASS = "PASS";
    String WIN = "WIN";
    String ENDGAME = "ENDGAME";
    String FINISHGAME = "FINISHGAME";
}

class ClientConnection extends Connection implements IServerConnectable, ILobbyConnectable, IGameConnectable {
    
    ClientConnection(String addr, int port, IDaifugoApp callback) throws IOException {
        this.socket = SocketChannel.open(new InetSocketAddress(addr, port));
    }

    /** Implements of IServerConnectable */
    @Override
    public void RequestCreateAccount(String name) {
        String msg = MakeCommand(name, ACCOUNT, CREATEACCOUNT);
        send(msg);
    }

    @Override
    public void RequestDeleteAccount(String name) {
        String msg = MakeCommand(name, ACCOUNT, DELETEACCOUNT);
        send(msg);
    }

    /** Implements of ILobbyConnectable */
    @Override
    public void RequestCreateLobby(String lobbyName, String hostName) {
        String msg = MakeCommand(hostName, ACCOUNT, CREATELOBBY, lobbyName);
        send(msg);
    }

    @Override
    public void RequestJoinLobby(String lobbyName, String password, String guestName) {
        String msg = MakeCommand(guestName, ACCOUNT, JOINLOBBY, lobbyName, password);
        send(msg);
    }

    @Override
    public void RequestLeaveLobby(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, LEAVELOBBY, lobbyName);
        send(msg);
    }

    @Override
    public void RequestReady(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, READY, lobbyName);
        send(msg);
    }

    @Override
    public void RequestUnready(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, UNREADY, lobbyName);
        send(msg);
    }

    @Override
    public void RequestStartGame(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, STARTGAME, lobbyName);
        send(msg);
    }

    /** Implements of IGameConnectable */
    @Override
    public void RequestPut(String lobbyName, String playerName, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = cards[i].toString();
        String cs = MakeCommand(css);
        String msg = MakeCommand(playerName, PLAYER, PUT, lobbyName, playerName, cs);
        send(msg);
    }

    @Override
    public void RequestPass(String lobbyName, String playerName) {
        String msg = MakeCommand(playerName, PLAYER, PUT, lobbyName, playerName, "NONE");
        send(msg);
    }

    @Override
    public void RequestWin(String lobbyName, String playerName) {
        String msg = MakeCommand(playerName, PLAYER, WIN, lobbyName, playerName);
        send(msg);
    }

    @Override
    public void SendChat(String lobbyName, String memberName, String content) {
        String msg = MakeCommand(memberName, MEMBER, CHAT, content);
        send(msg);
    }
}

class ClientListen implements Runnable {
    private SocketChannel sc;
    private ByteBuffer reader;
    private IDaifugoApp callback;

    ClientListen(SocketChannel sc, IDaifugoApp callback) {
        this.sc = sc;
        reader = ByteBuffer.allocate(512);
        this.callback = callback;
    }

    @Override
    public void run() {
        while (sc.isConnected()) {
            try {
                reader.clear();
                sc.read(reader);
                reader.flip();
                String msg = StandardCharsets.UTF_8.decode(reader).toString();
                Read(msg);
                Thread.sleep(100);
            } catch (IOException | InterruptedException e) { }
        }
    }

    private void Read(String msg) throws IOException {
        System.out.println("Read: " + msg);
        String[] cmd = msg.split(" ");
        Platform.runLater( () -> {
            if (cmd[0].equals(Connection.CREATEACCOUNT)) {
                if (cmd[1].equals(Connection.OK)) callback.showSelectHostOrJoinScene();
            }
            else if (cmd[0].equals(Connection.CREATELOBBY)) {
                String[] members = Arrays.copyOfRange(cmd, 3, cmd.length);
                if (cmd[1].equals(Connection.OK)) callback.showHostLobbyScene(cmd[2], members);
            }
            else if (cmd[0].equals(Connection.JOINLOBBY)) {
                if (cmd[1].equals(Connection.OK)) {
                    String[] members = Arrays.copyOfRange(cmd, 2, cmd.length);
                    callback.showGuestLobbyScene(members);
                }
                else if (cmd[1].equals(Connection.FAULT)) {

                }
                else {
                    String name = cmd[1];
                    System.out.println(name);
                    callback.addLobbyMember(name);
                }
            }
            else if (cmd[0].equals(Connection.READY)) {
                String readyName = cmd[1];
                callback.readyLobbyMember(readyName);
            }
            else if (cmd[0].equals(Connection.UNREADY)) {
                String unreadyName = cmd[1];
                callback.unreadyLobbyMember(unreadyName);
            }
            else if (cmd[0].equals(Connection.CHANGEHOST)) {
                String leaveHost = cmd[1];
                String nextHost = cmd[2];
                String password = cmd[3];
                String[] members = Arrays.copyOfRange(cmd, 4, cmd.length);
                if (nextHost.equals(callback.getName())) {
                    callback.showHostLobbyScene(password, members);
                }
                else {
                    callback.showGuestLobbyScene(members);
                }
            }
            else if (cmd[0].equals(Connection.LEAVELOBBY)) {
                callback.removeLobbyMember(cmd[1]);
            }
            else if (cmd[0].equals(Connection.CHAT)) {
                String[] contentArray = Arrays.copyOfRange(cmd, 2, cmd.length);
                String content = String.join(" ", contentArray);
                callback.addChatText(cmd[1], content);
            }
        });

    }
}

interface IServerConnectable {
    /**
     * アカウント作成をサーバーへ要求する
     * @param name 作成するアカウント名
     * @apiNote name ACCOUNT CREATEACCOUNT 
     */
    void RequestCreateAccount(String name);

    /**
     * アカウントの削除をサーバーへ要求する
     * @param name 削除するアカウント名
     * @apiNote name ACCOUNT DELETEACCOUNT
     */
    void RequestDeleteAccount(String name);
}

interface ILobbyConnectable {
    void RequestCreateLobby(String lobbyName, String hostName);
    void RequestJoinLobby(String lobbyName, String password, String guestName);
    void RequestLeaveLobby(String lobbyName, String memberName);
    void RequestReady(String lobbyName, String memberName);
    void RequestUnready(String lobbyName, String memberName);
    void RequestStartGame(String lobbyName, String memberName);
    void SendChat(String lobbyName, String memberName, String content);
}

interface IGameConnectable {
    void RequestPut(String lobbyName, String playerName, Card[] cards);
    void RequestPass(String lobbyName, String playerName);
    void RequestWin(String lobbyName, String playerName);
}

class ServerConnection extends Connection implements IClientConnectable, IMemberConnectable, IPlayerConnectable {

    ServerConnection(SocketChannel sc) {
        this.socket = sc;
    }

    /** Implements of IClientConnectable */
    @Override
    public void AnswerCreateAccount(boolean result) {
        String msg = MakeCommand(CREATEACCOUNT, (result ? OK : FAULT));
        send(msg);
    }

    @Override
    public void AnswerCreateLobby(boolean result, String password) {
        String msg = MakeCommand(CREATELOBBY, (result? OK : FAULT), password);
        send(msg);
    }

    @Override
    public void AnswerJoinLobby(boolean result, String lobbyName, Member[] joinedMember) {
        if (result) {
            String[] infos = new String[joinedMember.length];
            for (int i = 0; i < infos.length; i++) {
                String name = joinedMember[i].getName();
                boolean isHost = joinedMember[i].isHost();
                boolean isReady = joinedMember[i].isReady();
                infos[i] = name + "/" + (isHost ? "HOST" : (isReady ? "READY" : "UNREADY"));
            }
            String infosStr = MakeCommand(infos);
            System.out.println("DEBUG: " + infosStr);
            String msg = MakeCommand(JOINLOBBY, OK, infosStr); // JOINLOBBY OK PLAYER1 PLAYER2
            System.out.println("DEBUG " + msg);
            send(msg);
        } else {
            String msg = MakeCommand(JOINLOBBY, FAULT);
            send(msg);
        }
    }

    /** Implements of IMemberConnectable */
    @Override
    public void SendJoinMember(String name, Member[] members) {
        String msg = MakeCommand(JOINLOBBY, name);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }

    @Override
    public void SendLeaveMember(String name, Member[] members) {
        String msg = MakeCommand(LEAVELOBBY, name);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }

    @Override
    public void SendReadyMember(String name, Member[] members) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }

    @Override
    public void SendUnreadyMember(String name, Member[] members) {
        String msg = MakeCommand(UNREADY, name);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }

    @Override
    public void SendChangeHost(String leaveHost, String nextHost, Member[] members, String password) {
        String[] infos = new String[members.length];
        for (int i = 0; i < members.length; i++) {
            String name = members[i].getName();
            String state = members[i].isHost() ? "HOST" : (members[i].isReady() ? "READY" : "UNREADY");
            infos[i] = name + "/" + state;
        }
        String names = MakeCommand(infos);
        String msg = MakeCommand(CHANGEHOST, leaveHost, nextHost, password, names);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }
    
    /** Implements of IPlayerConnectable */
    @Override
    public void SendStartGame(Member[] members) {
        String msg = STARTGAME;
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket());
    }

    public void SendStartGame(Player[] players) {
        String msg = STARTGAME;
        for (int i = 0; i < players.length; i++) send(msg, players[i].getConnection().getSocket());
    }

    @Override
    public void SendPlayerTurn(Player[] players) {
        String[] playersStr = new String[players.length];
        for (int i = 0; i < players.length; i++) playersStr[i] = players[i].GetName() + players[i].getCardCount();
        String msg = MakeCommand(PLAYERTURN, MakeCommand(playersStr));
        for (int i = 0; i < players.length; i++) send(msg, players[i].getConnection().getSocket());
    }

    @Override
    public void SendHand(Card[] cards) {
        String[] cardsStr = new String[cards.length];
        for (int i = 0; i < cards.length; i++) {
            cardsStr[i] = cards[i].toString();
        }
        String msg = MakeCommand(HAND, MakeCommand(cardsStr));
        send(msg);
    }

    @Override
    public void SendYourTurn(Player player) {
        String msg = YOURTURN;
        send(msg, player.getConnection().getSocket());
    }

    @Override
    public void SendStage(Player[] players, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = cards[i].toString();
        String cs = MakeCommand(css);
        String msg = MakeCommand(STAGE, cs);
        for (int i = 0; i < players.length; i++) send(msg, players[i].getConnection().getSocket());
    }

    @Override
    public void SendEndGame() {
        
    }

    @Override
    public void SendFinishGame() {
        
    }

    @Override
    public void SendChat(String name, String text, Member[] members) {
        String msg = MakeCommand(CHAT, name, text);
        for (int i = 0; i < members.length; i++) send(msg, members[i].getConnection().getSocket()); 
    }
}

interface IClientConnectable extends IConnectable {
	void AnswerCreateAccount(boolean result);
    void AnswerCreateLobby(boolean result, String password);
    void AnswerJoinLobby(boolean result, String lobbyName, Member[] joinedMember);
}

interface IMemberConnectable extends IConnectable {
    void SendJoinMember(String name, Member[] members);
    void SendLeaveMember(String name, Member[] members);
    void SendReadyMember(String name, Member[] members);
    void SendUnreadyMember(String name, Member[] members);
    void SendChangeHost(String leaveHost, String nextHost, Member[] members, String password);
    void SendChat(String name, String msg, Member[] members);
}

interface IPlayerConnectable extends IConnectable {
    void SendStartGame(Member[] members);
    void SendStartGame(Player[] players);
    void SendPlayerTurn(Player[] players);
    void SendHand(Card[] cards);
    void SendYourTurn(Player player);
    void SendStage(Player[] players, Card[] cards);
    void SendEndGame();
    void SendFinishGame();
}