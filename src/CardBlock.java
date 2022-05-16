import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class CardBlock {

    private Stack<Card> cardBlock = new Stack<Card>() {
        {
            for (int i = 1; i <= 13; i++) {
                push(new Card(Card.HEART, i));
            }

            for (int i = 1; i <= 13; i++) {
                push(new Card(Card.DIAMOND, i));
            }

            for (int i = 1; i <= 13; i++) {
                push(new Card(Card.SPADE, i));
            }

            for (int i = 1; i <= 13; i++) {
                push(new Card(Card.CLOVER, i));
            }

            push(new Card(Card.JOKER, 0));
        }
    };

    public void Shuffle() {
        Collections.shuffle(cardBlock);
    }

    public Hand Deal() {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 13; i++) {
            if (cardBlock.empty()) break;
            cards.add(cardBlock.pop());
        }
        return new Hand(cards);
    }

    public Card Pop() {
        return cardBlock.pop();
    }

    public void Put(Card card) {
        cardBlock.push(card);
    }
}