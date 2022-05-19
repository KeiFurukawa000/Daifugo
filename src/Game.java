import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Game implements IGame{
    private Party party;
    private CardBlock cardBlock;
    private int passCount;
    private Card[] stage;
    private Timer timer;

    Game(Party party) {
        this.party = party;
    }

    public void Turn() {
        while (true) {
            Player player = party.GetHead();
            Task task = new Task(player);
            timer = new Timer();
            timer.schedule(task, 60000);

            player.StartTurn();

            party.Rotate();
        }
    }

    public void AddPassCount() {
        passCount++;
    }

    public void ResetPassCount() {
        passCount = 0;
    }

    public void ResetTimer() {
        timer.cancel();
    }

    public void SetStage(Card[] cards) {
        stage = cards;
    }

    public Card[] GetStage() {
        return stage;
    }

    public Party GetParty() {
        return party;
    }
}

class Task extends TimerTask {
    private Player player;

    Task(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        player.Pass();
    }
}

interface IGame {
    void AddPassCount();
    void ResetPassCount();
    void ResetTimer();
}

class Party {
    private LinkedList<Player> queue;

    Party() {
        queue = new LinkedList<>();
    }

    public void Add(Player player) {
        queue.addLast(player);
    }

    public void Remove(Player player) {
        queue.remove(player);
    }

    public void Rotate() {
        Player p = queue.removeFirst();
        queue.addLast(p);
    }

    public Player GetHead() {
        return queue.getFirst();
    }

    public Player[] GetTail() {
        LinkedList<Player> clone = new LinkedList<Player>(queue);
        clone.removeFirst();
        return clone.toArray(new Player[clone.size()]);
    }
}

class Player {
    private String name;
    private IPlayerConnectable ipc;
    private boolean isWin;
    private IGame callback;

    Player(String name, SocketChannel sc, IGame callback) {
        this.name = name;
        this.callback = callback;
    }

    public String GetName() {
        return name;
    }

    public void StartTurn() {
        
    }

    public void Put(Card[] cards) {
        
    }

    public void Pass() {
        callback.ResetTimer();
        
    }

    public void Win() {
        isWin = true;
    }
}
