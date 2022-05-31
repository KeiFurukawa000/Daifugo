


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/** 手札クラス */
public class Hand {
    private ArrayList<Card> list;

    public Hand(Card[] cards) {
        list = new ArrayList<Card>(Arrays.asList(cards));
    }

    public void sort() {
        Comparator<Card> cardComparator =
            Comparator.comparing(Card::getSuit).thenComparing(Card::getNumber);
        list.sort(cardComparator);
    }

    public void remove(Card card) {
        list.remove(card);
    }

    public Card remove(int index) {
        return list.remove(index);
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

    public Card getCard(int index) {
        return list.get(index);
    }

    public boolean contains(Suit suit, int num) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSuit().equals(suit) && list.get(i).getNumber() == num) return true;
        }
        return false;
    }

    public int getSize() {
        return list.size();
    }
}
