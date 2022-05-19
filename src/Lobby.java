import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.UUID;

import javafx.scene.input.PickResult;

class LobbyList {
    private HashMap<String, Lobby> lobbyList;
    private int maxCount;

    LobbyList(int maxCount) {
        this.maxCount = maxCount;
    }

    private boolean CanAddLobby(String lobbyName) {
        return Contains(lobbyName);
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

    public Lobby GetLobby(String lobbyName) {
        return lobbyList.get(lobbyName);
    }

    public int GetMaxCount() {
        return maxCount;
    }

    public int GetCurrentCount() {
        return lobbyList.size();
    }

    public boolean Contains(String lobbyName) {
        return lobbyList.containsKey(lobbyName);
    }
}

public class Lobby {
    private String name;
    private String password;
    private Member host;
    private HashMap<String, Member> list;
    private Game game;

    private final int maxMemberCount = 4;

    Lobby(String lobbyName, String hostName, SocketChannel sc) {
        this.name = lobbyName;
        this.host = new Member(name, sc);
        this.password = CreatePassword();
    }

    private String CreatePassword() {
        return UUID.randomUUID().toString();
    }

    public String GetLobbyName() {
        return name;
    }

    public boolean CanJoin(String name, String password) {
        return list.size() < maxMemberCount && !list.containsKey(name) && this.password.equals(password);
    }

    public Member Add(String name, SocketChannel sc) {
        Member newMember = new Member(name, sc);
        list.put(name, newMember);
        return newMember;
    }

    public void Remove(String name) {
        Member member = list.remove(name);
        member.Leave();
    }

    public Member GetMember(String name) {
        return list.get(name);
    }

    public Member[] GetMemberList() {
        return list.values().toArray(new Member[list.size()]);
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

    public Game GetGame() {
        return game;
    }
}

class Member {
    private String name;
    private SocketChannel sc;
    private boolean isReady;

    Member(String name, SocketChannel sc) {
        this.name = name;
        this.sc = sc;
    }

    public void Leave() {
        
    }

    public void Ready() {
        isReady = true;
        
    }

    public void Unready() {
        isReady = false;
        
    }

    public boolean GetReady() {
        return isReady;
    }

    public String GetName() {
        return name;
    }

    public SocketChannel GetSocket() {
        return sc;
    }
}