import java.util.ArrayList;
import java.util.Iterator;

public class Players {
    private ArrayList<Player> players;

    public void AddPlayer(Player player) {
        players.add(player);
    }

    public void AddPlayer(int index, Player player) {
        players.add(index, player);
    }

    public Player GetPlayer(String name) {
        Iterator<Player> ite = players.iterator();
        while (ite.hasNext()) {
            Player player = ite.next(); ite.remove();
            if (player.GetName().equals(name)) return player;
        }
        return null;
    }

    public void RemovePlayer(Player player) {
        players.remove(player);
    }

    public ArrayList<Player> GetPlayers() {
        return players;
    }
}
