

class MyPlayer extends Player{
    MyCard[] mycards;
    final int inithandcount;
    int cards_of_num;
    int num_of_cards;
    MyPlayer(String name, int handcount) {
        super(name, handcount);
        inithandcount = handcount;
    }
    void createMyCards(String[] distributedsuit, int[] distributednum){
        this.mycards = new MyCard[handcount];
        for(int i=0;i<handcount;i++){
            this.mycards[i] = new MyCard(distributedsuit[i], distributednum[i]);
        }
    }        
    void setMyCardX(){//カードのX座標の設定
        int n = 0;
        int len = this.inithandcount;
        for(int i=0;i<len;i++){
            if(this.mycards[i].removed == false) n++;
        }
        int interval = 50;
        int cardwidth = 90;
        int locate = 480 - (interval*(n-1) + cardwidth)/2;
        for(int i=0;i<len;i++){
            if(this.mycards[i].removed == false){
                this.mycards[i].rectgp.setLayoutX(locate);
                locate += 50;
            }            
        }
    }
    void setMyCardY(){//カードのY座標の設定
        for(int i=0;i<this.handcount;i++){
            this.mycards[i].rectgp.setLayoutY(400);
        }
    }
}