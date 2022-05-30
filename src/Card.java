import java.util.ArrayList;
import java.util.Arrays;
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

    public Card pop() {
        return block.pop();
    }

    public void put(Card card) {
        block.push(card);
    }

    public void shuffle() {
        Collections.shuffle(block);
    }

    public Card[] deal(int num) {
        Card[] cards = new Card[num];
        for (int i = 0; i < num; i++) cards[i] = pop();
        return cards;
    }
}

/** 手札クラス */
class Hand {
    private ArrayList<Card> list;

    Hand(Card[] cards) {
        list = new ArrayList<Card>(Arrays.asList(cards));
    }

    public void remove(Suit suit, int num) {
        list.remove(getCard(suit, num));
    }

    private Card getCard(Suit suit, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSuit().equals(suit) && list.get(i).getNumber() == num) return list.get(i);
        }
        return null;
    }

    public boolean contains(Suit suit, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSuit().equals(suit) && list.get(i).getNumber() == num) return true;
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

    public Suit getSuit() {
        return suit;
    }

    public int getNumber() {
        return number;
    }

    /**
     * カード情報を文字列に変換します
     * @return 文字列に変換されたカード情報 ex) Clover, 5 -> C/5
     */
    public String toString() {
        String s = suit.getName();
        String n = Integer.toString(number);
        return s + "/" + n;
    }

    /**
     * 文字列をカードに変換します
     * @param suit 
     * @param num
     * @return
     */
    public static Card strToCard(String suit, String num) {
        Suit s = null;
        switch (suit) {
            case "H": s = Suit.Heart; break;
            case "D": s = Suit.Diamond; break;
            case "C": s = Suit.Clover; break;
            case "S": s = Suit.Spade; break;
            case "J": s = Suit.Joker; break;
        }
        int n = Integer.parseInt(num);
        return new Card(s, n);
    }
}

/** トランプの絵柄 */
enum Suit {
    Heart("H"),
    Diamond("D"),
    Spade("S"),
    Clover("C"),
    Joker("J");

    private String name;
    private Suit(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}