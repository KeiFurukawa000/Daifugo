
class Player {
    protected boolean myturn = false;
    protected boolean finish = false;
    protected int playerrank;
    protected String name;
    protected int handcount;
    protected DispPlayer dispplayer;

    Player(String name, int handcount) {
        this.name = name;
        this.handcount = handcount;
        this.dispplayer = new DispPlayer(name);
    }
}
