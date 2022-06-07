
class Game{
    String playernames[] = new String[4];
    int handcounts[] = new int[4];
    MyPlayer myplayer;
    OtherPlayer[] otherplayers = new OtherPlayer[3];
    CenterStage centerstage2;
    int rank;
    Game(){
        rank = 0;
    }
    void setplayernames(String[] names){
        this.playernames = names;
    }
    void sethandcounts(int[] handcounts){
        this.handcounts = handcounts;
    }

    void createPlayers(String myname){
        int k=0;
        for(int i=0;i<4;i++){
            if(myname.equals(this.playernames[i])){
                k = i;
                this.myplayer = new MyPlayer(this.playernames[i],this.handcounts[i]);
                k++;
                break;
            }
        }            
        for(int i=0;i<3;i++){
                k = k % 4;
                this.otherplayers[i] = new OtherPlayer(this.playernames[k], this.handcounts[k]);
                k++;
        }
    }
    void setCardSelected(){
        for(int i=0;i<this.myplayer.inithandcount;i++){
            preCardSelected(i);
        }
    }
    void preCardSelected(int i){
        this.myplayer.mycards[i].rectgp.setOnMouseClicked(e->this.cardSelected(i));
    }
    void cardSelected(int i){
        if(this.myplayer.mycards[i].selected == false){
            //if((centerstage.num!=0)&&cards_of_num==centerstage.num) return;
            this.myplayer.mycards[i].selected = true;
            this.myplayer.num_of_cards=this.myplayer.mycards[i].num;
            if(this.myplayer.cards_of_num==0){
                for(int j=0;j<this.myplayer.inithandcount;j++){
                    if(this.myplayer.mycards[j].num!=this.myplayer.num_of_cards){
                        this.myplayer.mycards[j].transParentMyCard();
                    }
                }
            }
            this.myplayer.cards_of_num++;
        }else{
            this.myplayer.mycards[i].selected = false;
            this.myplayer.cards_of_num--;
            if(this.myplayer.cards_of_num==0){
                int j=this.myplayer.inithandcount;
                for(int k=0;k<this.myplayer.inithandcount;k++){
                    if(this.centerstage2.num<this.myplayer.mycards[k].num){
                        j=k;
                        break;
                    }
                }
                for(int k=j;k<this.myplayer.inithandcount;k++){
                    this.myplayer.mycards[k].transParentMyCardExit();
                }
            }
        }
    }
    void initGame() {
        rank = 0;
    }
}
