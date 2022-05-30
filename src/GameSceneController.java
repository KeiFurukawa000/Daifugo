import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class GameSceneController {

    Group root = new Group();
    TextField txtf;
    TextArea txta;

    public void Start(Stage stage) throws Exception {

        stage.setWidth(960);
        stage.setHeight(540);
        stage.setResizable(false);

        String myname = "MyName";
        int[][] distributed;
        int[] handcount = new int[4];
        MyPlayer myplayer;
        OtherPlayer p0;
        OtherPlayer p1;
        OtherPlayer p2;

        Room room = new Room(myname);

        receivePlayers(room);               //他プレイヤーの名前もらってroom.playersname代入
        recieveStartGame(room);             //ゲームの開始待ち
        Game game = room.game;
        game.centerstage2 = new CenterStage();
        recieveOrder(game);                     //順番を受けとってroom.game.orderに代入
        recievePlayersHandcounts(handcount);    //プレイヤーのカードの枚数を受け取ってhandcountに代入
        game.createPlayers(myname, handcount);  

        p0 = game.otherplayers[0];
        p1 = game.otherplayers[1];
        p2 = game.otherplayers[2];
        myplayer = game.myplayer;


        int n = myplayer.handcount;
        distributed = new int[n][2];
        recieveMyCards(distributed);            //自分のカードを受け取ってdistributedに代入
        myplayer.createMyCards(distributed);    //自分のカードをつくる
        game.setCardSelected();                 //自分のカードのクリック設定
        p0.createPlayerCards();
        p1.createPlayerCards();
        p2.createPlayerCards();

        Rectangle bg1 = new Rectangle(140,50,680,320);
        Rectangle bg2 = new Rectangle(150,60,660,300);
        bg1.setFill(Color.WHEAT);
        bg2.setFill(Color.GREEN);

        Button passbtn = new Button("MyPass");
        Button receivepassbtn = new Button("ReceivePass");
        Button clbutton = new Button("Clear");
        Button sendbtn = new Button("send");
        Button receivebtn = new Button("receive");
        Button comeonbaby = new Button("kumon");
        comeonbaby.setOnMouseClicked(e->finishGame());
        comeonbaby.setPrefWidth(80);
        txtf = new TextField();
        txta = new TextArea();

        txtf.setPrefWidth(160);
        txta.setPrefColumnCount(10);
        txta.setPrefRowCount(3);
        txta.setEditable(false);

        passbtn.setOnMouseClicked(e->MyPass(game));
        receivepassbtn.setOnMouseClicked(e->Pass(game));
        clbutton.setOnMouseClicked(e->clear(game));
        sendbtn.setOnMouseClicked(e->send());
        receivebtn.setOnMouseClicked(e->receive(game));

        passbtn.setMinWidth(80);
        passbtn.setMinHeight(80);
        receivepassbtn.setMinWidth(80);
        receivepassbtn.setMinHeight(80);
        clbutton.setMinWidth(80);
        clbutton.setMinHeight(80);
        sendbtn.setMinWidth(80);
        sendbtn.setMinHeight(20);
        receivebtn.setMinWidth(80);
        receivebtn.setMinHeight(20); 
         

        root.getChildren().addAll(bg1,bg2);

        root.getChildren().addAll(p0.dispplayer.circ,p1.dispplayer.circ,p2.dispplayer.circ);//プレイヤーの表示
        root.getChildren().addAll(p0.cardsgroup,p1.cardsgroup,p2.cardsgroup);
        root.getChildren().addAll(passbtn,receivepassbtn,clbutton,sendbtn,receivebtn,txta,txtf,comeonbaby);
        for(int i=0;i<n;i++){
            root.getChildren().add(myplayer.mycards[i].rectgp);
        }
        passbtn.setLayoutX(150);
        passbtn.setLayoutY(200);
        receivepassbtn.setLayoutX(150);
        receivepassbtn.setLayoutY(100);
        clbutton.setLayoutX(730);
        clbutton.setLayoutY(100);
        sendbtn.setLayoutX(730);
        sendbtn.setLayoutY(220);
        receivebtn.setLayoutX(730);
        receivebtn.setLayoutY(200);
        txta.setLayoutX(730);
        txta.setLayoutY(0);
        txtf.setLayoutX(730);
        txtf.setLayoutY(80);

        displayCenterStage(game);
        setPlayersNameLocation(game);
        setMyCardsLocation(myplayer);
        setPlayer0CardsLocation(p0);
        setPlayer1CardsLocation(p1);
        setPlayer2CardsLocation(p2);

        p0.myturn = true;
        p0.dispplayer.playerturn();
        exitMyTurn(myplayer);
        //myplayer.setPossible1();

        stage.setScene(new Scene(root));
        stage.show();
    }

    void setPlayersNameLocation(Game game){
        game.otherplayers[0].dispplayer.circ.setCenterX(100);
        game.otherplayers[0].dispplayer.circ.setCenterY(120);
        game.otherplayers[1].dispplayer.circ.setCenterX(480);
        game.otherplayers[1].dispplayer.circ.setCenterY(40);
        game.otherplayers[2].dispplayer.circ.setCenterX(860);
        game.otherplayers[2].dispplayer.circ.setCenterY(120);
    }
    void setPlayer0CardsLocation(OtherPlayer p0){
        p0.cardsgroup.setLayoutX(100);
        p0.cardsgroup.setLayoutY(200);
        p0.cardsgroup.setRotate(270);
    }
    void setPlayer1CardsLocation(OtherPlayer p1){
        p1.cardsgroup.setLayoutX(480);
        p1.cardsgroup.setLayoutY(50);
        p1.cardsgroup.setRotate(180);
    }
    void setPlayer2CardsLocation(OtherPlayer p2){
        p2.cardsgroup.setLayoutX(860);
        p2.cardsgroup.setLayoutY(200);
        p2.cardsgroup.setRotate(90);
    }
    void displayCenterStage(Game game){
        root.getChildren().add(game.centerstage2.field);
        game.centerstage2.field.setLayoutX(420);
        game.centerstage2.field.setLayoutY(220);
        game.centerstage2.field.setOnMouseClicked(e->transmit(game));
    }

    void transmit(Game game){
        int n = game.myplayer.inithandcount;
        int j = 0;
        int[][] sendnums;
        for(int i=0;i<n;i++){
            if(game.myplayer.mycards[i].selected == true){
                j++;
            }
        }
        if(game.centerstage2.mode==0){
            if(j==0) return;
            game.centerstage2.mode = j;//selectcardmodeの変更
            //myplayer.setPossibleCard();//選択できるカードの枚数の下限を設定
        }else{
            if(j!=game.centerstage2.mode) return;
        }
        sendnums = new int[game.centerstage2.mode][2];
        int k=0;
        for(int i=0;i<n;i++){
            if(game.myplayer.mycards[i].selected == true){
                root.getChildren().remove(game.myplayer.mycards[i].rectgp);
                sendnums[k][0] =  game.myplayer.mycards[i].suit;
                sendnums[k][1] =  game.myplayer.mycards[i].num;
                k++;
                 game.myplayer.mycards[i].selected = false;
                 game.myplayer.mycards[i].removed = true;
            }
        }
        Pass(game);
        game.myplayer.cards_of_num = 0;
        game.myplayer.setMyCardX();
        game.myplayer.handcount -= game.centerstage2.mode;
        printCenterCards(game,sendnums);//カードを表示する
        sendCards(game, sendnums);//カードを送る
        if(game.myplayer.handcount==0){
            game.myplayer.finish = true;
            game.rank++;
            if(game.rank==3) finishGame();
        }
    }

    void setMyCardsLocation(MyPlayer myplayer){
        myplayer.setMyCardY();
        myplayer.setMyCardX();
    }
    void clear(Game game){
        game.centerstage2.initCenterStage(); 
        game.centerstage2.mode = 0;
        displayCenterStage(game);
    }

    class Room{
        int room_id;
        Game game;
        String myname;
        String playersnames[] = new String[4];
        Room(String myname){
            this.myname = myname;
        }
        
        void setplayersnames(String[] strings){
            for(int i=0;i<4;i++){
                this.playersnames[i] = strings[i];
            }
        }
        void startGame(){
            this.game = new Game(this.playersnames);
        }
    }

    class Game{
        static int gamescounter;
        int[] order = new int[4];
        String playernames[] = new String[4];
        MyPlayer myplayer;
        OtherPlayer[] otherplayers = new OtherPlayer[3];
        CenterStage centerstage2;
        int rank;
        Game(String[] playernames){
            this.playernames = playernames;
        }
        void setOrder(int[] order){
            this.order = order;
        }
        void createPlayers(String myname, int[] handcount){
            int k=0;
            for(int i=0;i<4;i++){
                if(myname == this.playernames[i]){
                    k = this.order[i];
                    this.myplayer = new MyPlayer(this.playernames[i],handcount[i]);
                    k++;
                    break;
                }
            }            
            for(int i=0;i<3;i++){
                k = k % 4;
                for(int j=0;j<4;j++){
                    if(k==this.order[j]){
                        this.otherplayers[i] = new OtherPlayer(this.playernames[j], handcount[j], i);
                        k++;
                        break;
                    }
                }
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
    }
    class CenterStage{
        int num;//場に出ているカードの数字
        int mode = 0;
        Group field;
        CenterStage(){
            setup();
        }
        void setup(){
            num = 0;
            field = new Group();
            Rectangle rect = new Rectangle(120,120);
            rect.setFill(Color.LIGHTGREEN);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1);
            field.getChildren().add(rect);
        }
        void initCenterStage(){
            this.field.getChildren().clear();
            setup();
        }
        void setCenterStageNum(int num){
            this.num = num;
        }
    }

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
    class OtherPlayer extends Player {
        int location;
        PlayerCard[] playercards;
        Group cardsgroup = new Group();
        OtherPlayer(String name, int handcount,int location){
            super(name,handcount);
            this.location = location;
        }
        void createPlayerCards() {
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
    class MyPlayer extends Player{
        MyCard[] mycards;
        final int inithandcount;
        int cards_of_num;
        int num_of_cards;
        MyPlayer(String name, int handcount) {
            super(name, handcount);
            inithandcount = handcount;
        }
        void createMyCards(int distributed[][]){
            this.mycards = new MyCard[handcount];
            for(int i=0;i<handcount;i++){
                this.mycards[i] = new MyCard(distributed[i][0], distributed[i][1]);
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

    class DispPlayer extends Group{
        Circle circ = new Circle(30);//icon
        Label lbl = new Label();

        DispPlayer(String name){
            circ.setFill(Color.WHEAT);
            lbl.setText(name);
            this.getChildren().addAll(circ,lbl);
        }
        void playerturn(){
            circ.setFill(Color.RED);
        }
        void exitplayerturn(){
            circ.setFill(Color.WHEAT);
        }
    }

    class Card {
        Group rectgp = new Group();
    }

    class PlayerCard extends Card{
        PlayerCard(){
            Rectangle rect = new Rectangle(30,42);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            rect.setArcHeight(7);
            rect.setArcWidth(7);
            this.rectgp.getChildren().add(rect);
        }
    }

    class DisplayCard extends Card{
        int suit;
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
    class CenterCard extends DisplayCard{
        CenterCard(int suit,int num){
            this.suit = suit;
            this.num = num;
            Rectangle rect = new Rectangle(45,70);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            rect.setArcHeight(10);
            rect.setArcWidth(10);

            String s0 = String.format("%d", this.num);
            String s1 = getsuit(this.suit);
            Label lbl = new Label();
            Label lbl2 = new Label();
            Label lbl3 = new Label();
            lbl.setFont(new Font(6));
            lbl2.setFont(new Font(6));
            lbl3.setFont(new Font(6));
            lbl.setText(s0);
            lbl2.setText(s0);
            lbl3.setText(s1);

            this.rectgp.getChildren().addAll(rect,lbl,lbl2,lbl3);
            lbl.setLayoutX(5);
            lbl.setLayoutX(10);
            lbl2.setLayoutX(35);
            lbl2.setLayoutY(60);
            lbl2.setRotate(180);
            lbl3.setLayoutX(5);
            lbl3.setLayoutY(20);
        }
    }

    class MyCard extends DisplayCard{
        boolean selected = false;
        boolean removed = false;
        boolean possible = false;
        MyCard(int suit, int num){
            this.suit = suit;
            this.num = num;
            Rectangle rect = new Rectangle(90,140);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            rect.setArcHeight(20);
            rect.setArcWidth(20);

            String s0 = String.format("%d", num);
            String s1 = getsuit(suit);
            Label lbl = new Label();
            Label lbl2 = new Label();
            Label lbl3 = new Label();
            lbl.setText(s0);
            lbl2.setText(s0);
            lbl3.setText(s1);

            this.rectgp.getChildren().addAll(rect,lbl,lbl2,lbl3);
            lbl.setLayoutX(5);
            lbl.setLayoutX(10);
            lbl2.setLayoutX(70);
            lbl2.setLayoutY(125);
            lbl2.setRotate(180);
            lbl3.setLayoutX(5);
            lbl3.setLayoutY(20);
            this.rectgp.setOnMouseEntered(e -> mouseOnCard());
            this.rectgp.setOnMouseExited(e -> mouseOffCard());
        }
        void mouseOnCard(){
            if(this.selected == false)
                this.rectgp.setLayoutY(this.rectgp.getLayoutY()-10);
        }
        void mouseOffCard(){
            if(this.selected == false)
                this.rectgp.setLayoutY(this.rectgp.getLayoutY()+10);
        }
        void transParentMyCard(){
            this.rectgp.setMouseTransparent(true);
        }
        void transParentMyCardExit(){
            this.rectgp.setMouseTransparent(false);
        }
    }

    void Pass(Game game){
        if(game.otherplayers[0].myturn){
            game.otherplayers[0].myturn = false;
            game.otherplayers[0].dispplayer.exitplayerturn();
            PassTo1(game);
        }
        else if(game.otherplayers[1].myturn){
            game.otherplayers[1].myturn = false;
            game.otherplayers[1].dispplayer.exitplayerturn();
            PassTo2(game);
        }else if(game.otherplayers[2].myturn){
            game.otherplayers[2].myturn = false;
            game.otherplayers[2].dispplayer.exitplayerturn();
            PassToMe(game);
        }else{
            game.myplayer.myturn = false;
            exitMyTurn(game.myplayer);
            PassTo0(game);
        }
    }
    void PassTo0(Game game){
        if(game.otherplayers[0].finish==true){
            PassTo1(game);
        }else{
            game.otherplayers[0].myturn = true;
            game.otherplayers[0].dispplayer.playerturn();
        }
    }
    void PassTo1(Game game){
        if(game.otherplayers[1].finish==true){
            PassTo1(game);
        }else{
            game.otherplayers[1].myturn = true;
            game.otherplayers[1].dispplayer.playerturn();
        }
    }
    void PassTo2(Game game){
        if(game.otherplayers[2].finish==true){
            PassTo1(game);
        }else{
            game.otherplayers[2].myturn = true;
            game.otherplayers[2].dispplayer.playerturn();
        }
    }
    void PassToMe(Game game){
        if(game.myplayer.finish==true){
            PassTo0(game);
        }else{
            game.myplayer.myturn = true;
            myTurn(game);
        }
    }
 
    void myTurn(Game game){
        int num = game.centerstage2.num;
        game.myplayer.num_of_cards = num;
        int j=game.myplayer.inithandcount;
        for(int i=0;i<game.myplayer.inithandcount;i++){
            if(num<game.myplayer.mycards[i].num){
                j=i;
                break;
            }
        }
        for(int i=j;i<game.myplayer.inithandcount;i++){
            game.myplayer.mycards[i].transParentMyCardExit();
        }
        //数字に関する処理をこのへんに
    }    
    void exitMyTurn(MyPlayer myplayer){
        for(int i=0;i<myplayer.inithandcount;i++){
            myplayer.mycards[i].transParentMyCard();;
        }
    }
    //定義のみ記載したメソッド
    void MyPass(Game game){
        Pass(game);
        sendPass();
    }
    void printCenterCards(Game game, int[][] printnums){//中央にカードを配置するように命令
        switch(game.centerstage2.mode){
            //case 0: setCenterCard(printnums);break;//modeを決める
            case 1: printCenterCard1(game, printnums); break;
            case 2: printCenterCard2(game, printnums); break;
            case 3: printCenterCard3(game, printnums); break;
            case 4: printCenterCard4(game, printnums); break;
        }
    }

    void printCenterCard1(Game game, int[][] printnums){//centercardの表示1枚
        CenterCard centercard = new CenterCard(printnums[0][0], printnums[0][1]);
        centercard.rectgp.setVisible(false);
        game.centerstage2.field.getChildren().add(centercard.rectgp);
        centercard.rectgp.setLayoutX(37.5);
        centercard.rectgp.setLayoutY(30);
        centercard.rectgp.setVisible(true);
        game.centerstage2.setCenterStageNum(printnums[0][1]);
    }
    void printCenterCard2(Game game, int[][] printnums){//2枚
        CenterCard[] centercards = new CenterCard[2];
        for(int i=0;i<2;i++){
            centercards[i] = new CenterCard(printnums[i][0], printnums[i][1]);
            centercards[i].rectgp.setVisible(false);
            game.centerstage2.field.getChildren().add(centercards[i].rectgp);
        }
        centercards[0].rectgp.setLayoutX(27.5);
        centercards[1].rectgp.setLayoutX(47.5);
        for(int i=0;i<2;i++){
            centercards[i].rectgp.setLayoutY(30);
            centercards[i].rectgp.setVisible(true);
        }
        game.centerstage2.setCenterStageNum(printnums[0][1]);
    }
    void printCenterCard3(Game game, int[][] printnums){//3枚
        CenterCard[] centercards = new CenterCard[3];
        for(int i=0;i<3;i++){
            centercards[i] = new CenterCard(printnums[i][0], printnums[i][1]);
            centercards[i].rectgp.setVisible(false);
            game.centerstage2.field.getChildren().add(centercards[i].rectgp);
        }
        centercards[0].rectgp.setLayoutX(17.5);
        centercards[1].rectgp.setLayoutX(37.5);
        centercards[2].rectgp.setLayoutX(57.5);
        for(int i=0;i<3;i++){
            centercards[i].rectgp.setLayoutY(30);
            centercards[i].rectgp.setVisible(true);
        }
        game.centerstage2.setCenterStageNum(printnums[0][1]);
    }
    void printCenterCard4(Game game, int[][] printnums){//4枚
        CenterCard[] centercards = new CenterCard[4];
        for(int i=0;i<4;i++){
            centercards[i] = new CenterCard(printnums[i][0], printnums[i][1]);
            centercards[i].rectgp.setVisible(false);
            game.centerstage2.field.getChildren().add(centercards[i].rectgp);
        }
        centercards[0].rectgp.setLayoutX(7.5);
        centercards[1].rectgp.setLayoutX(27.5);
        centercards[2].rectgp.setLayoutX(47.5);
        centercards[3].rectgp.setLayoutX(67.5);
        for(int i=0;i<4;i++){
            centercards[i].rectgp.setLayoutY(30);
            centercards[i].rectgp.setVisible(true);
        }
        game.centerstage2.setCenterStageNum(printnums[0][1]);
    }

    //定義のみ記載したメソッド
    //受信系
    void recieveStartGame(Room room){
        //通信待ち
        room.startGame();
    }
    void receivePlayers(Room room){//プレイヤーを受け取る
        //プレイヤーの名前を格納するメソッド
        String[] names = new String[4];
        names[0] = "P3";
        names[1] = "MyName";
        names[2] = "P2";
        names[3] = "P3";
        room.setplayersnames(names);
    }
    void recieveOrder(Game game){
        int[] order = new int[4];
        //通信待ち
        order[0]=0;
        order[1]=2;
        order[2]=3;
        order[3]=1;
        game.setOrder(order);
    }

    void recievePlayersHandcounts(int[] handcount){//手札の枚数を受け取る
        handcount[0] = 13;
        handcount[1] = 14;
        handcount[2] = 14;
        handcount[3] = 13;
    }

    void recieveMyCards(int[][] distributed){//カードを受け取る
        distributed[0][0] = 2;        distributed[0][1] = 3;
        distributed[1][0] = 3;        distributed[1][1] = 3;
        distributed[2][0] = 0;        distributed[2][1] = 4;
        distributed[3][0] = 2;        distributed[3][1] = 4;
        distributed[4][0] = 3;        distributed[4][1] = 4;
        distributed[5][0] = 0;        distributed[5][1] = 5;
        distributed[6][0] = 3;        distributed[6][1] = 5;
        distributed[7][0] = 3;        distributed[7][1] = 6;
        distributed[8][0] = 3;        distributed[8][1] = 7;
        distributed[9][0] = 3;        distributed[9][1] = 9;
        distributed[10][0] = 1;        distributed[10][1] = 12;
        distributed[11][0] = 2;        distributed[11][1] = 12;
        distributed[12][0] = 3;        distributed[12][1] = 12;
        distributed[13][0] = 1;        distributed[13][1] = 2;
    }

    //送信系
    void sendCards(Game game, int[][] sendnums){//カードを送る
        String s = "P3";
        for(int i=0;i<game.centerstage2.mode;i++){
            String s1 = String.format(",%d,%d",sendnums[i][0],sendnums[i][1]);
            s += s1;
        }
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }
    void sendPass(){
        String s = "P3 Pass";
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }

    void receive(Game game){
        String s = txtf.getText();
        switch (s){
            case "P0 Pass": Pass(game); break;
            case "P1 Pass": Pass(game); break;
            case "P2 Pass": Pass(game); break;
            case "Clear": clear(game);  break;
            default : receivePlayerscards(game, s);
        }
        txtf.setText("");
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }

    void receivePlayerscards(Game game, String s){
        String result = s.substring(3);
        String[] results = result.split(",");
        int halfn = results.length/2;
        if(game.centerstage2.mode==0){
            game.centerstage2.mode = halfn;
        }
        int[][] printnums = new int[halfn][2];
        for(int i=0;i<halfn;i++){
            printnums[i][0] = Integer.parseInt(results[2*i]); 
            printnums[i][1] = Integer.parseInt(results[2*i+1]);
        }
        printCenterCards(game, printnums);
        reduceHandcount(game);
        displayHandcount(game);
        if(game.rank==3) finishGame();
        Pass(game);
    }

    void reduceHandcount(Game game){
        if(game.myplayer.myturn){
            game.myplayer.handcount -= game.centerstage2.mode;
        }
        else if(game.otherplayers[0].myturn){
            game.otherplayers[0].handcount -= game.centerstage2.mode;
            if(game.otherplayers[0].handcount == 0){
                game.otherplayers[0].finish=true;
                game.rank++;
            }
        }
        else if(game.otherplayers[1].myturn){
            game.otherplayers[1].handcount -= game.centerstage2.mode;
            if(game.otherplayers[1].handcount == 0){
                game.otherplayers[1].finish=true;
                game.rank++;
            }
        }
        else if(game.otherplayers[2].myturn){
            game.otherplayers[2].handcount -= game.centerstage2.mode;
            if(game.otherplayers[2].handcount == 0){
                game.otherplayers[2].finish=true;
                game.rank++;
            }
        }
    }

    void displayHandcount(Game game){
        if(game.otherplayers[0].myturn){
            game.otherplayers[0].removePlayersCards();
            game.otherplayers[0].createPlayerCards();
            setPlayer0CardsLocation(game.otherplayers[0]);
        }else if(game.otherplayers[1].myturn){
            game.otherplayers[1].removePlayersCards();
            game.otherplayers[1].createPlayerCards();
            setPlayer1CardsLocation(game.otherplayers[1]);
        }else if(game.otherplayers[2].myturn){
            game.otherplayers[2].removePlayersCards();
            game.otherplayers[2].createPlayerCards();
            setPlayer2CardsLocation(game.otherplayers[2]);
        }
    }

    void finishGame(){
        Rectangle recatangle = new Rectangle(200,200,550,100);
        recatangle.setFill(Color.WHEAT);
        Label exitlbl = new Label("ゲーム終了!");
        exitlbl.setFont(new Font(58));
        root.getChildren().addAll(recatangle,exitlbl);
        exitlbl.setLayoutX(350);
        exitlbl.setLayoutY(210);
    }

    void send(){
        String s = txtf.getText();
        txtf.setText("");
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }
}
