

import java.util.Collections;
import java.util.Stack;

/** カードの山クラス */
public class CardBlock {
    private Stack<Card> block;

    public CardBlock() {
        block = new Stack<>(){{
            for (int i = 3; i <= 15; i++) push(new Card(Suit.Heart, i, "img/card_heart_" + i + ".png"));
            for (int i = 3; i <= 15; i++) push(new Card(Suit.Diamond, i, "img/card_diamond_" + i + ".png"));
            for (int i = 3; i <= 15; i++) push(new Card(Suit.Spade, i, "img/card_spade_" + i + ".png"));
            for (int i = 3; i <= 15; i++) push(new Card(Suit.Clover, i, "img/card_club_" + i + ".png"));
            //push(new Card(Suit.Joker, 0, "img/card_joker.png"));
        }};
        System.out.println(block.size());
    }

    public Card pop() {
        System.out.println(block.size());
        return block.pop();
    }

    public void put(Card card) {
        block.push(card);
    }

    public void shuffle() {
        Collections.shuffle(block);
        System.out.println(block.size());
    }

    public Card[] deal(int num) {
        Card[] cards = new Card[num];
        for (int i = 0; i < num; i++) cards[i] = pop();
        return cards;
    }
}
