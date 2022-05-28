import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

/**
 * ロビーをまとめるクラス
 * @author Kei Furukawa
 * @version 0.0.1
 */
class LobbyList implements ILobbyList {
    private HashMap<String, Lobby> lobbyList;
    private int maxCount;

    /**
     * コンストラクタ
     * @param maxCount ロビーリストに格納できる最大ロビー数
     */
    LobbyList(int maxCount) {
        lobbyList = new HashMap<>();
        this.maxCount = maxCount;
    }

    public boolean add(Lobby lobby) {
        if (canAdd(lobby.getName())) {
            lobby.setCallback(this);
            lobbyList.put(lobby.getName(), lobby);
            return true;
        }
        return false;
    }

    private boolean canAdd(String lobbyName) {
        return !contains(lobbyName) && lobbyList.size() < maxCount;
    }

    @Override
    public void remove(String lobbyName) {
       lobbyList.remove(lobbyName);
    }

    public Lobby get(String lobbyName) {
        return lobbyList.get(lobbyName);
    }

    private boolean contains(String lobbyName) {
        return lobbyList.containsKey(lobbyName);
    }
}

interface ILobbyList {
    void remove(String lobbyName);
}

public class Lobby implements ILobby {
    private String name;
    private String password;
    private Member host;
    private HashMap<String, Member> list;
    private LinkedList<Member> members;
    private ILobbyList lobbyList;
    private Game game;

    private final int maxMemberCount = 4;

    Lobby(String lobbyName, String hostName, SocketChannel sc) {
        list = new HashMap<>();
        members = new LinkedList<>();

        this.name = lobbyName;
        this.host = new Member(hostName, true, sc, this);
        this.host.ready();
        this.password = createPassword();

        add(host);
    }

    public void setCallback(ILobbyList lobbyList) {
        this.lobbyList = lobbyList;
    }

    private String createPassword() {
        return UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public boolean canJoin(String name, String password) {
        return list.size() < maxMemberCount && !list.containsKey(name) && this.password.equals(password);
    }

    public void add(Member member) {
        member.getConnection().SendJoinMember(member.getName(), getMembersAsArray());
        list.put(member.getName(), member);
        members.add(member);
    }

    public void remove(Member member) {
       list.remove(member.getName());
       members.remove(member);
    }

    public Member get(String name) {
        return list.get(name);
    }

    public Member getHost() {
        return host;
    }

    public boolean isAllReady() {
        Member[] all = list.values().toArray(new Member[list.size()]);
        for (int i = 0; i < all.length; i++) if (!all[i].isReady()) return false;
        return true;
    }

    public void startGame() {
        game = new Game(members);
    }

    @Override
    public Member[] getMembersAsArray() {
        return members.toArray(new Member[members.size()]);
    }

    @Override
    public void leave(Member member) {
        list.remove(member.getName());
        members.remove(member);
        if (list.size() <= 0) {
            lobbyList.remove(getName());
        }
    }

    @Override
    public String changeHost() {
        members.get(0).setHost(true);
        host = members.get(0);
        return host.getName();
    }
}

interface ILobby {
    Member[] getMembersAsArray();
    void leave(Member member);
    String changeHost();
    String getPassword();
}

class Member {
    private String name;
    private boolean isHost;
    private boolean isReady;
    private IMemberConnectable connection;
    private ILobby callback;

    Member(String name, boolean isHost, SocketChannel sc, ILobby callback) {
        this.name = name;
        this.isHost = isHost;
        this.connection = new ServerConnection(sc);
        this.callback = callback;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean arg) {
        isHost = arg;
    }

    public void action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.LEAVELOBBY)) {
            leave();
        }
        else if (action.equals(Connection.READY)) {
            ready();
        }
        else if (action.equals(Connection.UNREADY)) {
            unready();
        }
        else if (action.equals(Connection.STARTGAME)) {
            
        }
    }

    public IMemberConnectable getConnection() {
        return connection;
    }

    private void leave() {
        callback.leave(this);

        if (isHost) {
            String nextHost = callback.changeHost();
            connection.SendChangeHost(name, nextHost, callback.getMembersAsArray(), callback.getPassword());
        }
        else {
            connection.SendLeaveMember(name, callback.getMembersAsArray());    
        }
    }

    public void ready() {
        isReady = true;
        connection.SendReadyMember(name, callback.getMembersAsArray());
    }

    public void unready() {
        isReady = false;
        connection.SendUnreadyMember(name, callback.getMembersAsArray());
    }

    public boolean isReady() {
        return isReady;
    }

    public String getName() {
        return name;
    }
}