import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Connection {
    private String name;
    protected SocketChannel socket;

    protected final String OK = "OK";
    protected final String FAULT = "FAULT";

    protected final String MEMBER = "MEMBER";
    protected final String PLAYER = "PLAYER";

    protected final String CREATEACCOUNT = "CREATEACCOUNT";
    protected final String DELETEACCOUNT = "DELETEACCOUNT";
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

class ClientConnection extends Connection implements IServerConnectable, ILobbyConnectable, IGameConnectable {
    /** Implements of IServerConnectable */
    @Override
    public void RequestCreateAccount(String name) {
        String msg = MakeCommand(CREATEACCOUNT, name);
        Send(msg);
    }

    @Override
    public void RequestDeleteAccount(String name) {
        String msg = MakeCommand(DELETEACCOUNT, name);
        Send(msg);
    }

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

interface IServerConnectable {
    void RequestCreateAccount(String name);
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

    private void Send(String msg, SocketChannel sc) {
        try {
            sc.write(StandardCharsets.UTF_8.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Implements of IClientConnectable */
    @Override
    public void AnswerCreateAccount(boolean result, SocketChannel sc) {
        String msg = MakeCommand(CREATEACCOUNT, (result ? OK : FAULT));
        Send(msg, sc);
    }

    @Override
    public void AnswerCreateLobby(boolean result, SocketChannel sc) {
        String msg = MakeCommand(CREATELOBBY, (result? OK : FAULT));
        Send(msg, sc);
    }

    @Override
    public void AnswerJoinLobby(boolean result, Member[] joinedMember, SocketChannel sc) {
        if (result) {
            String[] infos = new String[joinedMember.length];
            for (int i = 0; i < infos.length; i++) {
                String name = joinedMember[i].GetName();
                boolean isReady = joinedMember[i].GetReady();
                infos[i] = name + "/" + (isReady ? READY : UNREADY);
            }
            String infosStr = MakeCommand(infos);
            String msg = MakeCommand(JOINLOBBY, OK, infosStr);
            Send(msg, sc);
        } else {
            String msg = MakeCommand(JOINLOBBY, FAULT);
            Send(msg, sc);
        }
    }

    /** Implements of IMemberConnectable */
    @Override
    public void SendJoinMember(String name, Member[] members) {
        String msg = MakeCommand(JOINLOBBY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }

    @Override
    public void SendLeaveMember(String name, Member[] members) {
        String msg = MakeCommand(LEAVELOBBY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }

    @Override
    public void SendReadyMember(String name, Member[] members) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }

    @Override
    public void SendUnreadyMember(String name, Member[] members) {
        String msg = MakeCommand(READY, name);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }
    
    /** Implements of IPlayerConnectable */
    @Override
    public void SendStartGame(Member[] members) {
        String msg = STARTGAME;
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }

    @Override
    public void SendYourTurn(Member member) {
        String msg = YOURTURN;
        Send(msg, member.GetSocket());
    }

    @Override
    public void SendStage(Member[] members, Card[] cards) {
        String[] css = new String[cards.length];
        for (int i = 0; i < css.length; i++) css[i] = CardToStr(cards[i]);
        String cs = MakeCommand(css);
        String msg = MakeCommand(STAGE, cs);
        for (int i = 0; i < members.length; i++) Send(msg, members[i].GetSocket());
    }
}

interface IClientConnectable {
    void AnswerCreateAccount(boolean result, SocketChannel sc);
    void AnswerCreateLobby(boolean result, SocketChannel sc);
    void AnswerJoinLobby(boolean result, Member[] joinedMember, SocketChannel sc);
}

interface IMemberConnectable {
    void SendJoinMember(String name, Member[] members);
    void SendLeaveMember(String name, Member[] members);
    void SendReadyMember(String name, Member[] members);
    void SendUnreadyMember(String name, Member[] members);
}

interface IPlayerConnectable {
    void SendStartGame(Member[] members);
    void SendYourTurn(Member member);
    void SendStage(Member[] members, Card[] cards);
}