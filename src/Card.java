import java.util.Collections;
import java.util.Stack;

/** カードの山クラス */
class CardBlock {
    private Stack<Card> block;

    CardBlock() {
        block = new Stack<>(){{
            for (int i = 1; i <= 13; i++) push(new Card(Suit.Heart, i));
            for (int i = 1; i <= 13; i++) push(new Card(Suit.Diamond, i));
            for (int i = 1; i <= 13; i++) push(new Card(Suit.Spade, i));
            for (int i = 1; i <= 13; i++) push(new Card(Suit.Clover, i));
        }};
    }

    public Card Pop() {
        return block.pop();
    }

    public void Put(Card card) {
        block.push(card);
    }

    public void Shuffle() {
        Collections.shuffle(block);
    }
}

/** カードクラス */
public class Card {
    private Suit suit;
    private int number;

    Card(Suit suit, int number) {
        this.suit = suit;
        this.number = number;
    }

    public Suit GetSuit() {
        return suit;
    }

    public int GetNumber() {
        return number;
    }
}

/** トランプの絵柄 */
enum Suit {
    Heart,
    Diamond,
    Spade,
    Clover,
    Joker
}