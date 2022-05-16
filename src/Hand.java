import java.util.ArrayList;
import java.util.Iterator;

public class Hand {

    private ArrayList<Card> hand;

    Hand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public boolean Contains(String icon, int number) {
        Iterator<Card> ite = hand.iterator();
        while (ite.hasNext()) {
            Card card = ite.next(); ite.remove();
            if (icon.equals(card.GetIcon()) && number == card.GetNumber()) return true;
        }
        return false;
    }

    public boolean IsEmpty() {
        return hand.isEmpty();
    }

    public void RemoveCard(Card... card) {
        for (int i = 0; i < card.length; i++) {
            for (int j = 0; j < hand.size(); j++) {
                Card handCard = hand.get(j);
                Card putCard = card[i];
                if (handCard.GetIcon().equals(putCard.GetIcon()) && handCard.GetNumber() == putCard.GetNumber()) hand.remove(handCard);
            }
        }
    }
}
