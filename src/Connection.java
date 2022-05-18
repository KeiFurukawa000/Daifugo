import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Connection {
    private String name;
    protected SocketChannel socket;

    protected final String OK = "OK";
    protected final String FAULT = "FAULT";

    protected final String CREATELOBBY = "CREATELOBBY";
    protected final String JOINLOBBY = "JOINLOBBY";
    protected final String LEAVELOBBY = "LEAVELOBBY";
    protected final String STARTGAME = "STARTGAME"; 
    protected final String YOURTURN = "YOURTURN";
    protected final String READY = "READY";
    protected final String UNREADY = "UNREADY";
    protected final String STAGE = "STAGE";
    protected final String PUT = "PUT";
    protected final String PASS = "PASS";
    protected final String WIN = "WIN";

    public SocketChannel GetSocket() {
        return socket;
    }

    protected void Send(String msg) {
        try {
            socket.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}

class ClientConnection extends Connection implements ILobbyConnectable, IGameConnectable {

    /** Implements of ILobbyConnectable */
    @Override
    public void RequestCreateLobby(String lobbyName, String hostName) {
        String msg = MakeCommand(CREATELOBBY, lobbyName, hostName);
        Send(msg);
    }

    @Override
    public void RequestJoinLobby(String lobbyName, String password, String guestName) {
        String msg = MakeCommand(JOINLOBBY, lobbyName, password, guestName);
        Send(msg);
    }

    @Override
    public void RequestLeaveLobby(String lobbyName, String memberName) {
        String msg = MakeCommand(LEAVELOBBY, lobbyName, memberName);
        Send(msg);
    }

    @Override
    public void RequestReady(String lobbyName, String memberName) {
        String msg = MakeCommand(READY, lobbyName, memberName);
        Send(msg);
    }

    @Override
    public void RequestUnready(String lobbyName, String memberName) {
        String msg = MakeCommand(UNREADY, lobbyName, memberName);
        Send(msg);
    }

    @Override
    public void RequestStartGame(String lobbyName, String memberName) {
        String msg = MakeCommand(lobbyName, memberName);
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

    private void Send(String msg, SocketChannel sc) {
        try {
            sc.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Implements of IClientConnectable */
    @Override
    public void AnswerCreateLobby(boolean result, SocketChannel sc) {
        String msg = MakeCommand(CREATELOBBY, (result? OK : FAULT));
        Send(msg, sc);
    }

    @Override
    public void AnswerJoinLobby(Member[] joinedMember, SocketChannel sc) {
        String[] infos = new String[joinedMember.length];
        for (int i = 0; i < infos.length; i++) {
            String name = joinedMember[i].GetName();
            boolean isReady = joinedMember[i].GetReady();
            infos[i] = name + "/" + (isReady ? READY : UNREADY);
        }
        String infosStr = MakeCommand(infos);
        String msg = MakeCommand(JOINLOBBY, infosStr);
        Send(msg, sc);
    }

    /** Implements of IMemberConnectable */
    @Override
    public void SendJoinMember(String name, Member[] members) {
        String msg = MakeCommand(JOINLOBBY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }

    @Override
    public void SendLeaveMember(String name, SocketChannel[] scs) {
        String msg = MakeCommand(LEAVELOBBY, name);
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }

    @Override
    public void SendReadyMember(String name, SocketChannel[] scs) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }

    @Override
    public void SendUnreadyMember(String name, SocketChannel[] scs) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }
    
    /** Implements of IPlayerConnectable */
    @Override
    public void SendStartGame(SocketChannel[] scs) {
        String msg = STARTGAME;
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }

    @Override
    public void SendYourTurn(SocketChannel[] scs) {
        String msg = YOURTURN;
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }

    @Override
    public void SendStage(SocketChannel[] scs, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = CardToStr(cards[i]);
        String cs = MakeCommand(css);
        String msg = MakeCommand(STAGE, cs);
        for (int i = 0; i < scs.length; i++) Send(msg, scs[i]);
    }
}

interface IClientConnectable {
    void AnswerCreateLobby(boolean result, SocketChannel sc);
    void AnswerJoinLobby(Member[] joinedMember, SocketChannel sc);
}

interface IMemberConnectable {
    void SendJoinMember(String name, Member[] members);
    void SendLeaveMember(String name, SocketChannel[] scs);
    void SendReadyMember(String name, SocketChannel[] scs);
    void SendUnreadyMember(String name, SocketChannel[] scs);
}

interface IPlayerConnectable {
    void SendStartGame(SocketChannel[] scs);
    void SendYourTurn(SocketChannel[] scs);
    void SendStage(SocketChannel[] scs, Card[] cards);
}