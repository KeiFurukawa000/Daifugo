
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Game implements IGame {
    private LinkedList<Member> members;
    private Party party;
    private Party turn;
    private CardBlock cardBlock;
    private Card[] stage;
    private int passCount;
    private Party winners;
    private int currentGameCount = 1;
    private int maxGameCount;

    Game(LinkedList<Member> members, int maxGameCount) throws InterruptedException {
        this.maxGameCount = maxGameCount;
        this.members = members;

        cardBlock = new CardBlock();
        cardBlock.shuffle();

        party = new Party();
        turn = new Party();
        winners = new Party();
        for (int i = 0; i < members.size(); i++) {
            Player player = new Player(members.get(i).getName(), members.get(i).getConnection().getSocket(),
             this, turn, cardBlock.deal(13));
            members.get(i).setPlayer(player);
            party.add(player);
            turn.add(player);
        }
        
        Player[] players = party.getPlayersAsArray();
        for (int i = 0; i < players.length; i++) {
            players[i].getConnection().SendStartGame(members.toArray(new Member[members.size()]), currentGameCount, maxGameCount);
        }
    
        Thread.sleep(1000);
        turn.getNowPlayer().getConnection().SendYourTurn(turn.getNowPlayer());
    }

    public void startTurn() {
        turn.getNowPlayer().getConnection().SendYourTurn(turn.getNowPlayer());
    }

    public void addPassCount() {
        passCount++;
    }

    public boolean isAllPlayerPassed() {
        if (passCount >= turn.size()-1) {
            passCount = 0;
            return true;
        }
        return false;
    }

    @Override
    public void endTurn(boolean rotate) {
        if (rotate) {
            turn.rotate();
            party.rotate();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (party.size()-1 == winners.size()) return;
        else startTurn();
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

    @Override
    public void Win(Player player) {
        winners.add(player);
        turn.remove(player);
        if (party.size()-1 == winners.size()) {
            winners.add(turn.getNowPlayer());
            ReGame();
        }
    }

    public void ReGame() {
        currentGameCount++;
        if (currentGameCount > maxGameCount) FinishGame();
        else {
            cardBlock = new CardBlock();
            cardBlock.shuffle();
    
            party = new Party();
            turn = new Party();
            Player[] players = winners.getPlayersAsArray();
            for (int i = 0; i < winners.size(); i++) {
                players[i].setCards(cardBlock.deal(13));
                players[i].setTurn(turn);
                party.add(players[i]);
                turn.add(players[i]);
            }
            
            for (int i = 0; i < players.length; i++) {
                players[i].getConnection().SendStartGame(players, currentGameCount, maxGameCount);
            }
        
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            turn.getNowPlayer().getConnection().SendYourTurn(turn.getNowPlayer());
            winners.removeAll();
        }
    }

    public void FinishGame() {
        Player[] players = party.getPlayersAsArray();
        for (int i = 0; i < players.length; i++) {
            players[i].getConnection().SendEndGame();
        }
    }

    @Override
    public Party getParty() {
        return party;
    }

    @Override
    public void ResetPassCount() {
        passCount = 0;
    }
}

interface IGame {
    void setStage(Card[] cards);
    Card[] getStage();
    void endTurn(boolean rotate);
    Party getParty();
    void Win(Player player);
    void addPassCount();
    boolean isAllPlayerPassed();
    void ResetPassCount();
}

class Party implements IParty {
    private LinkedList<Player> queue;

    Party() {
        queue = new LinkedList<>();
    }

    public Party clone() {
        return (Party) queue.clone();
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
        return queue.peek();
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
}

interface IParty {
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

    public void setTurn(IParty turn) {
        this.party = turn;
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

    public void Action(String[] cmd) {
        String action = cmd[0];
        if (action.equals(Connection.PLAYERTURN)) {
            ipc.SendPlayerTurn(party.getPlayersAsArray());
        }
        else if (action.equals(Connection.HAND)) {
            ipc.SendHand(cards);
        }
        else if (action.equals(Connection.PUT)) {
            String[] cardsStr = Arrays.copyOfRange(cmd, 1, cmd.length);
            if (cmd[1].equals("NONE")) Pass();
            else {
                Card[] cards = new Card[cardsStr.length];
                for (int i = 0; i < cards.length; i++) {
                    String[] meta = cardsStr[i].split(",");
                    String suit = meta[0];
                    String num = meta[1];
                    cards[i] = Card.strToCard(suit, num);
                }
                Put(cards);
            }
        }
        else if (action.equals(Connection.WIN)) {
            Win();
        }
    }

    public void Put(Card[] cards) {
        game.ResetPassCount();
        for (int i = 0; i < cards.length; i++) {
            if (!cards[i].equalsNum(8)) {
                game.setStage(cards);
                ipc.SendStage(game.getParty().getPlayersAsArray(), cards, cards.length);
                game.endTurn(true);
                return;
            }
        }
        game.setStage(new Card[0]);
        ipc.SendStage(game.getParty().getPlayersAsArray(), new Card[0], cards.length);
        game.endTurn(false);
    }

    public void Pass() {
        game.addPassCount();
        if (game.isAllPlayerPassed()) {
            ipc.SendStage(game.getParty().getPlayersAsArray(), new Card[0], 0);
            game.setStage(new Card[0]);
        }
        else {
            ipc.SendStage(game.getParty().getPlayersAsArray(), null, 0);
        }
        game.endTurn(true);
    }

    public void Win() {
        game.Win(this);
    }

    public IPlayerConnectable getConnection() {
        return ipc;
    }
}
