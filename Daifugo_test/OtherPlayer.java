import javafx.scene.Group;

class OtherPlayer extends Player{
    PlayerCard[] playercards;
    Group cardsgroup = new Group();
    OtherPlayer(String name, int handcount){
        super(name,handcount);
    }
    void createPlayerCards(){
        playercards = new PlayerCard[this.handcount];
        for(int i=0;i<this.handcount;i++){
            playercards[i] = new PlayerCard();
            this.cardsgroup.getChildren().add(this.playercards[i].rectgp);
        }
        setPlayerCardsLayoutX();
    }
    void removePlayersCards(){
        this.cardsgroup.getChildren().clear();
    }
    void setPlayerCardsLayoutX(){
        //int n = 0;
        int n = this.handcount;
        int interval = 10;
        int locate = -5*n-10;
        for(int i=0;i<n;i++){
                this.playercards[i].rectgp.setLayoutX(locate);
                locate += interval;
        }
    }    
}