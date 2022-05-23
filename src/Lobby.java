import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

/**
 * ロビーをまとめるクラス
 */
class LobbyList {
    private HashMap<String, Lobby> lobbyList;
    private int maxCount;

    LobbyList(int maxCount) {
        lobbyList = new HashMap<>();
        this.maxCount = maxCount;
    }

    private boolean CanAddLobby(String lobbyName) {
        return !Contains(lobbyName) && lobbyList.size() < maxCount;
    }

    public boolean Add(Lobby lobby) {
        if (CanAddLobby(lobby.GetLobbyName())) {
            lobbyList.put(lobby.GetLobbyName(), lobby);
            return true;
        }
        return false;
    }

    public void Remove(String lobbyName) {
       lobbyList.remove(lobbyName); 
    }

    public Lobby Get(String lobbyName) {
        return lobbyList.get(lobbyName);
    }

    private boolean Contains(String lobbyName) {
        return lobbyList.containsKey(lobbyName);
    }
}

public class Lobby implements ILobby {
    private String name;
    private String password;
    private Member host;
    private HashMap<String, Member> list;
    private LinkedList<Member> members;
    private Game game;

    private final int maxMemberCount = 5;

    Lobby(String lobbyName, String hostName, SocketChannel sc) {
        list = new HashMap<>();
        members = new LinkedList<>();

        this.name = lobbyName;
        this.host = new Member(hostName, true, sc, this);
        this.host.Ready();
        this.password = CreatePassword();

        Add(host);
    }

    private String CreatePassword() {
        return UUID.randomUUID().toString();
    }

    public String GetLobbyName() {
        return name;
    }

    public String GetPassword() {
        return password;
    }

    public boolean CanJoin(String name, String password) {
        return list.size() < maxMemberCount && !list.containsKey(name) && this.password.equals(password);
    }

    public void Add(Member member) {
        member.GetConnection().SendJoinMember(member.GetName(), GetMemberArray());
        list.put(member.GetName(), member);
        members.add(member);
    }

    public void Remove(Member member) {
       list.remove(member.GetName());
       members.remove(member);
    }

    public Member GetMember(String name) {
        return list.get(name);
    }

    public Member GetHost() {
        return host;
    }

    public boolean IsAllReady() {
        Member[] all = list.values().toArray(new Member[list.size()]);
        for (int i = 0; i < all.length; i++) if (!all[i].IsReady()) return false;
        return true;
    }

    public void StartGame() {
        
    }

    @Override
    public Member[] GetMemberArray() {
        return members.toArray(new Member[members.size()]);
    }

    @Override
    public void Leave(Member member) {
        list.remove(member.GetName());
        members.remove(member);
    }
}

interface ILobby {
    Member[] GetMemberArray();
    void Leave(Member member);
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

    public boolean IsHost() {
        return isHost;
    }

    public void Action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.LEAVELOBBY)) {
            LeaveLobby();
        }
        else if (action.equals(Connection.READY)) {
            Ready();
        }
        else if (action.equals(Connection.UNREADY)) {
            Unready();
        }
        else if (action.equals(Connection.STARTGAME)) {
            
        }
    }

    public IMemberConnectable GetConnection() {
        return connection;
    }

    private void LeaveLobby() {
        callback.Leave(this);
        connection.SendLeaveMember(name, callback.GetMemberArray());
    }

    public void Ready() {
        isReady = true;
        connection.SendReadyMember(name, callback.GetMemberArray());
    }

    public void Unready() {
        isReady = false;
        connection.SendUnreadyMember(name, callback.GetMemberArray());
    }

    public boolean IsReady() {
        return isReady;
    }

    public String GetName() {
        return name;
    }
}