
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Game implements IGame {
    private Party party;
    private CardBlock cardBlock;
    private Card[] stage;
    private Timer timer;
    private Party winners;
    private int currentGameCount;
    private int maxGameCount;

    Game(LinkedList<Member> members, int maxGameCount) {
        this.maxGameCount = maxGameCount;

        cardBlock = new CardBlock();
        cardBlock.shuffle();

        party = new Party(members, this);
        for (int i = 0; i < members.size(); i++) {
            Player player = new Player(members.get(i).getName(), members.get(i).getConnection().getSocket(),
             this, party, i != 3 ? cardBlock.deal(13) : cardBlock.deal(14));
            members.get(i).setPlayer(player);
            party.add(player);
        }

        Player[] players = party.getPlayersAsArray();
        for (int i = 0; i < players.length; i++) {
            players[i].getConnection().SendStartGame(members.toArray(new Member[members.size()]));
        }

        startTurn();
    }

    public void startTurn() {
        Player player = party.getNowPlayer();
        Task task = new Task(player);
        timer = new Timer();
        timer.schedule(task, 60000);
    }

    @Override
    public void endTurn() {
        timer.cancel();
        party.rotate();
        startTurn();
    }

    @Override
    public void setStage(Card[] cards) {
        stage = cards;
    }

    @Override
    public Card[] getStage() {
        return stage;
    }

    public Party GetParty() {
        return party;
    }

    public void Win(Player player) {
        winners.add(player);
        if (party.size() == winners.size()) ReGame();
    }

    public void ReGame() {
        currentGameCount++;
        if (currentGameCount >= maxGameCount) FinishGame();
        else {
            Player[] nextParty = winners.getPlayersAsArray();
            CardBlock cardBlock = new CardBlock();
            cardBlock.shuffle();
            for (int i = 0; i < winners.size(); i++) {
                nextParty[i].getConnection().SendStartGame(nextParty);
                nextParty[i].setCards(i == 3 ? cardBlock.deal(13) : cardBlock.deal(14));
            }
            party = winners;
            winners.removeAll();
        }
    }

    public void FinishGame() {

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
    void setStage(Card[] cards);
    Card[] getStage();
    void endTurn();
}

class Party implements IParty {
    private LinkedList<Player> queue;

    Party(LinkedList<Member> members, IGame callback) {
        queue = new LinkedList<>();
    }

    public void add(Player player) {
        queue.addLast(player);
    }

    public void remove(Player player) {
        queue.remove(player);
    }

    public void removeAll() {
        queue.clear();
    }

    public void rotate() {
        Player p = queue.removeFirst();
        queue.addLast(p);
    }

    public Player getNowPlayer() {
        return queue.getFirst();
    }

    public Player getPlayer(String name) {
        for (int i = 0; i < queue.size(); i++) {
            if (name.equals(queue.get(i).GetName())) return queue.get(i);
        }
        return null;
    }

    @Override
    public Player[] getAnotherPlayers() {
        LinkedList<Player> clone = new LinkedList<Player>(queue);
        clone.removeFirst();
        return clone.toArray(new Player[clone.size()]);
    }

    @Override
    public Player[] getPlayersAsArray() {
        return queue.toArray(new Player[queue.size()]);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    @Override
    public void onWin() {
        queue.pop();
    }
}

interface IParty {
    void onWin();
    Player[] getPlayersAsArray();
    Player[] getAnotherPlayers();
}

class Player {
    private String name;
    private IPlayerConnectable ipc;
    private IGame game;
    private IParty party;
    private Card[] cards;

    Player(String name, SocketChannel sc, IGame game, IParty party, Card[] cards) {
        this.name = name;
        this.game = game;
        this.party = party;
        this.ipc = new ServerConnection(sc);
        this.cards = cards;
    }

    public Card[] getCards() {
        return cards;
    }

    public void setCards(Card[] cards) {
        this.cards = cards;
    }

    public String GetName() {
        return name;
    }

    public int getCardCount() {
        return cards.length;
    }

    public void startTurn() {
        ipc.SendYourTurn(this);
    }

    public void Action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.PLAYERTURN)) {
            ipc.SendPlayerTurn(party.getPlayersAsArray());
        }
        else if (action.equals(Connection.HAND)) {
            ipc.SendHand(cards);
        }
    }

    public void Put(Card[] cards) {
        ipc.SendStage(party.getAnotherPlayers(), cards);
        game.setStage(cards);
        game.endTurn();
    }

    public void Pass() {
        ipc.SendStage(party.getAnotherPlayers(), game.getStage());
        game.endTurn();
    }

    public void Win() {
        party.onWin();
    }

    public IPlayerConnectable getConnection() {
        return ipc;
    }
}
