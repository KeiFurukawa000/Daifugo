import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javafx.application.Platform;

public class Connection implements IConnectable {
    private String name;
    protected SocketChannel socket;

    @Override
    public SocketChannel GetSocket() {
        return socket;
    }

    protected void Send(String msg) {
        try {
            socket.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(msg);
    }

    protected void Send(String msg, SocketChannel sc) {
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

    protected String CardToStr(Card card) {
        String suit = "";
        switch (card.GetSuit()) {
            case Heart:
                suit = "H";
                break;
            case Diamond:
                suit = "D";
                break;
            case Spade:
                suit = "S";
                break;
            case Clover:
                suit = "C";
                break;
            case Joker:
                suit = "J";
                break;
        }
        String num = Integer.toString(card.GetNumber());
        return suit + "/" + num;
    }

    protected Card[] StrToCard(String[] str) {
        Card[] cards = new Card[str.length];
        for (int i = 0; i < cards.length; i++) {
            String[] element = str[i].split("/");
            Suit suit = null;
            switch (element[0]) {
                case "H":
                    suit = Suit.Heart;
                    break;
                case "D":
                    suit = Suit.Diamond;
                    break;
                case "S":
                    suit = Suit.Spade;
                    break;
                case "C":
                    suit = Suit.Clover;
                    break;
                case "J":
                    suit = Suit.Joker;
                    break;
            }
            int number = Integer.parseInt(element[1]);
            cards[i] = new Card(suit, number);
        }
        return cards;
    }
}

interface IConnectable {
    SocketChannel GetSocket();

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
    String STAGE = "STAGE";
    String PUT = "PUT";
    String PASS = "PASS";
    String WIN = "WIN";
}

class ClientConnection extends Connection implements IServerConnectable, ILobbyConnectable, IGameConnectable {
    
    ClientConnection(String addr, int port, IDaifugoApp callback) throws IOException {
        this.socket = SocketChannel.open(new InetSocketAddress(addr, port));
    }

    /** Implements of IServerConnectable */
    @Override
    public void RequestCreateAccount(String name) {
        String msg = MakeCommand(name, ACCOUNT, CREATEACCOUNT);
        Send(msg);
    }

    @Override
    public void RequestDeleteAccount(String name) {
        String msg = MakeCommand(name, ACCOUNT, DELETEACCOUNT);
        Send(msg);
    }

    /** Implements of ILobbyConnectable */
    @Override
    public void RequestCreateLobby(String lobbyName, String hostName) {
        String msg = MakeCommand(hostName, ACCOUNT, CREATELOBBY, lobbyName);
        Send(msg);
    }

    @Override
    public void RequestJoinLobby(String lobbyName, String password, String guestName) {
        String msg = MakeCommand(guestName, ACCOUNT, JOINLOBBY, lobbyName, password);
        Send(msg);
    }

    @Override
    public void RequestLeaveLobby(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, LEAVELOBBY, lobbyName);
        Send(msg);
    }

    @Override
    public void RequestReady(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, READY, lobbyName);
        Send(msg);
    }

    @Override
    public void RequestUnready(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, UNREADY, lobbyName);
        Send(msg);
    }

    @Override
    public void RequestStartGame(String lobbyName, String memberName) {
        String msg = MakeCommand(memberName, MEMBER, STARTGAME, lobbyName);
        Send(msg);
    }

    /** Implements of IGameConnectable */
    @Override
    public void RequestPut(String lobbyName, String playerName, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = CardToStr(cards[i]);
        String cs = MakeCommand(css);
        String msg = MakeCommand(PUT, lobbyName, playerName, cs);
        Send(msg);
    }

    @Override
    public void RequestPass(String lobbyName, String playerName) {
        String msg = MakeCommand(PUT, lobbyName, playerName, "NONE");
        Send(msg);
    }

    @Override
    public void RequestWin(String lobbyName, String playerName) {
        String msg = MakeCommand(WIN, lobbyName, playerName);
        Send(msg);
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
                if (cmd[1].equals(Connection.OK)) callback.ShowSelectHostOrJoinScene();
            }
            else if (cmd[0].equals(Connection.CREATELOBBY)) {
                if (cmd[1].equals(Connection.OK)) callback.ShowHostLobbyScene(cmd[2]);
            }
            else if (cmd[0].equals(Connection.JOINLOBBY)) {
                if (cmd[1].equals(Connection.OK)) {
                    String[] members = Arrays.copyOfRange(cmd, 2, cmd.length);
                    callback.ShowGuestLobbyScene(members);
                }
                else if (cmd[1].equals(Connection.FAULT)) {

                }
                else {
                    String name = cmd[1];
                    System.out.println(name);
                    callback.AddLobbyMember(name);
                }
            }
            else if (cmd[0].equals(Connection.READY)) {
                String readyName = cmd[1];
                callback.ReadyLobbyMember(readyName);
            }
            else if (cmd[0].equals(Connection.UNREADY)) {
                String unreadyName = cmd[1];
                callback.UnreadyLobbyMember(unreadyName);
            }
            else if (cmd[0].equals(Connection.LEAVELOBBY)) {
                callback.RemoveLobbyMember(cmd[1]);
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
        Send(msg);
    }

    @Override
    public void AnswerCreateLobby(boolean result, String password) {
        String msg = MakeCommand(CREATELOBBY, (result? OK : FAULT), password);
        Send(msg);
    }

    @Override
    public void AnswerJoinLobby(boolean result, String lobbyName, Member[] joinedMember) {
        if (result) {
            String[] infos = new String[joinedMember.length];
            for (int i = 0; i < infos.length; i++) {
                String name = joinedMember[i].GetName();
                boolean isHost = joinedMember[i].IsHost();
                boolean isReady = joinedMember[i].IsReady();
                infos[i] = name + "/" + (isHost ? "HOST" : (isReady ? "READY" : "UNREADY"));
            }
            String infosStr = MakeCommand(infos);
            System.out.println("DEBUG: " + infosStr);
            String msg = MakeCommand(JOINLOBBY, OK, infosStr); // JOINLOBBY OK PLAYER1 PLAYER2
            System.out.println("DEBUG " + msg);
            Send(msg);
        } else {
            String msg = MakeCommand(JOINLOBBY, FAULT);
            Send(msg);
        }
    }

    /** Implements of IMemberConnectable */
    @Override
    public void SendJoinMember(String name, Member[] members) {
        String msg = MakeCommand(JOINLOBBY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
    }

    @Override
    public void SendLeaveMember(String name, Member[] members) {
        String msg = MakeCommand(LEAVELOBBY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
    }

    @Override
    public void SendReadyMember(String name, Member[] members) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
    }

    @Override
    public void SendUnreadyMember(String name, Member[] members) {
        String msg = MakeCommand(UNREADY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
    }
    
    /** Implements of IPlayerConnectable */
    @Override
    public void SendStartGame(Member[] members) {
        String msg = STARTGAME;
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
    }

    @Override
    public void SendYourTurn(Member member) {
        String msg = YOURTURN;
        Send(msg, member.GetConnection().GetSocket());
    }

    @Override
    public void SendStage(Member[] members, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = CardToStr(cards[i]);
        String cs = MakeCommand(css);
        String msg = MakeCommand(STAGE, cs);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetConnection().GetSocket());
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
}

interface IPlayerConnectable extends IConnectable {
    void SendStartGame(Member[] members);
    void SendYourTurn(Member member);
    void SendStage(Member[] members, Card[] cards);
}