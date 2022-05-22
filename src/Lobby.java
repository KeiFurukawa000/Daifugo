import java.nio.channels.SocketChannel;
import java.util.HashMap;
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
    private Game game;

    private final int maxMemberCount = 4;

    Lobby(String lobbyName, String hostName, SocketChannel sc) {
        this.name = lobbyName;
        this.host = new Member(name, sc, this);
        this.password = CreatePassword();
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
        list.put(member.GetName(), member);
    }

    public void Remove(Member member) {
       list.remove(member.GetName());
    }

    public Member GetMember(String name) {
        return list.get(name);
    }

    public Member GetHost() {
        return host;
    }

    public boolean IsAllReady() {
        Member[] all = list.values().toArray(new Member[list.size()]);
        for (int i = 0; i < all.length; i++) if (!all[i].GetReady()) return false;
        return true;
    }

    public void StartGame() {
        
    }

    @Override
    public Member[] GetMemberList() {
        return list.values().toArray(new Member[list.size()]);
    }

    @Override
    public void Leave(Member member) {
        list.remove(member.GetName());
    }
}

interface ILobby {
    Member[] GetMemberList();
    void Leave(Member member);
}

class Member {
    private String name;
    private boolean isReady;
    private IMemberConnectable connection;
    private ILobby callback;

    Member(String name, SocketChannel sc, ILobby callback) {
        this.name = name;
        this.connection = new ServerConnection(sc);
        this.callback = callback;
    }

    public void Action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.LEAVELOBBY)) {
            LeaveLobby();
        }
        else if (action.equals(Connection.READY)) {
            Ready();
        }
    }

    private void LeaveLobby() {
        callback.Leave(this);
        connection.SendLeaveMember(name, callback.GetMemberList());
    }

    public void Ready() {
        isReady = true;
        connection.SendReadyMember(name, callback.GetMemberList());
    }

    public void Unready() {
        isReady = false;
        connection.SendUnreadyMember(name, callback.GetMemberList());
    }

    public boolean GetReady() {
        return isReady;
    }

    public String GetName() {
        return name;
    }
}