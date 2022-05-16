import java.nio.channels.SocketChannel;

public class Player {

    private String name;
    private Hand hand;
    private SocketChannel socket;

    Player(String name, Hand hand) {
        this.name = name;
        this.hand = hand;
    }

    public String GetName() {
        return name;
    }

    public Hand GetHand() {
        return hand;
    }

    public boolean IsMaster() {
        return hand.Contains(Card.DIAMOND, Card._3);
    }

    public boolean IsHandEmpty() {
        return hand.IsEmpty();
    }

    public Card[] Put(Card... card) {
        hand.RemoveCard(card);
        return card;
    }
}
