public class Card {

    public static final String HEART = "HEART";
    public static final String DIAMOND = "DIAMOND";
    public static final String SPADE = "SPADE";
    public static final String CLOVER = "CLOVER";
    public static final String JOKER = "JOKER";

    public static final int _A = 1;
    public static final int _2 = 2;
    public static final int _3 = 3;
    public static final int _4 = 4;
    public static final int _5 = 5;
    public static final int _6 = 6;
    public static final int _7 = 7;
    public static final int _8 = 8;
    public static final int _9 = 9;
    public static final int _10 = 10;
    public static final int _J= 11;
    public static final int _Q = 12;
    public static final int _K = 13;

    private String icon;
    private int number;

    Card(String icon, int number) {
        this.icon = icon;
        this.number = number;
    }

    public String GetIcon() {
        return icon;
    }

    public int GetNumber() {
        return number;
    }
}
