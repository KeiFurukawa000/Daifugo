import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Lobby {
    private String lobbyName;
    private String hostName;
    private String password;
    private HashMap<String, SocketChannel> playerList;
    private Party readyList;

    private int maxPlayerCount;
    private int canStartPlayerCount;

    private Game game;

    Lobby(String lobbyName, String hostName) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
        password = UUID.randomUUID().toString();
        maxPlayerCount = 4;
        canStartPlayerCount = 2;
    }

    Lobby(String lobbyName, String hostName, int maxPlayerCount, int canStartPlayerCount) {
        this.lobbyName = lobbyName;
        this.hostName = hostName;
        password = UUID.randomUUID().toString();
        this.maxPlayerCount = maxPlayerCount;
        this.canStartPlayerCount = canStartPlayerCount;
    }

    public void StartGame() {
        Game game = new Game();
        game.Start(readyList);
    }

    public String GetLobbyName() {
        return lobbyName;
    }

    public String GetHostName() {
        return hostName;
    }

    public String GetPassword() {
        return password; 
    }

    public boolean ContainsPlayer(String playerName) {
        return playerList.containsKey(playerName);
    }

    public SocketChannel GetSocket(String playerName) {
        return playerList.get(playerName);
    }

    public boolean CanAddPlayer(String password, String name) {
        return this.password.equals(password) && !playerList.containsKey(name) && playerList.size() < maxPlayerCount;
    }

    public void AddPlayer(String name, SocketChannel sc) {
        playerList.put(name, sc);
    }

    public void RemovePlayer(String name) {
        playerList.remove(name);
    }

    public void AddReadyPlayer(Participant p) {
        readyList.AddParticipant(p);
    }

    public void RemoveReadyPlayer(String name) {
        readyList.RemoveParticipant(name);
    }

    public boolean CanStartGame() {
        return readyList.GetAllParticipants().size() >= canStartPlayerCount;
    }

    public ArrayList<SocketChannel> GetAllSocket() {
        return new ArrayList<SocketChannel>(playerList.values());
    }

    public Game GetGame() {
        return game;
    }
}
