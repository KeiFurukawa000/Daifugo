class DisplayCard extends Card{
    String suit;
    int num;
    String getsuit(int suit){
        String s = new String();
        switch(suit){
            case 0: s = "spade"; break;
            case 1: s = "heart"; break;
            case 2: s = "dia";   break;
            case 3: s = "club";  break;
        }
        return s;
    }
}