

/** カードクラス */
public class Card {
    private Suit suit;
    private int number;
    private String imageURL;

    Card(Suit suit, int number, String imageURL) {
        this.suit = suit;
        this.number = number;
        this.imageURL = imageURL;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getNumber() {
        return number;
    }

    public String getImageURL() {
        return imageURL;
    }

    /**
     * カード情報を文字列に変換します
     * @return 文字列に変換されたカード情報 ex) Clover, 5 -> C/5
     */
    public String toString() {
        String s = suit.getName();
        String n = Integer.toString(number);
        return s + "," + n;
    }

    /**
     * 文字列をカードに変換します
     * @param suit 
     * @param num
     * @return
     */
    public static Card strToCard(String suit, String num) {
        Suit s = null;
        String fileURL = null;
        switch (suit) {
            case "H": s = Suit.Heart; fileURL = "img/card_heart_" + num + ".png"; break;
            case "D": s = Suit.Diamond; fileURL = "img/card_diamond_" + num + ".png"; break;
            case "C": s = Suit.Clover; fileURL = "img/card_club_" + num + ".png"; break;
            case "S": s = Suit.Spade; fileURL = "img/card_spade_" + num + ".png"; break;
            case "J": s = Suit.Joker; fileURL = "img/card_joker.png"; break;
        }
        int n = Integer.parseInt(num);
        return new Card(s, n, fileURL);
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