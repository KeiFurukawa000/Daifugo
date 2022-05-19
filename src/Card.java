import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import javafx.beans.property.ReadOnlyStringPropertyBase;

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

/** 手札クラス */
class Hand {
    private ArrayList<Card> list;

    Hand(Card[] cards) {
        list = new ArrayList<Card>(Arrays.asList(cards));
    }

    public void Remove(Suit suit, int num) {
        list.remove(GetCard(suit, num));
    }

    private Card GetCard(Suit suit, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).GetSuit().equals(suit) && list.get(i).GetNumber() == num) return list.get(i);
        }
        return null;
    }

    public boolean Contains(Suit suit, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).GetSuit().equals(suit) && list.get(i).GetNumber() == num) return true;
        }
        return false;
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