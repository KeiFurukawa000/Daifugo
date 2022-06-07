import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MyApp3_0 extends Application{

    Group root = new Group();
    TextField txtf;
    TextField txts;
    TextArea txta;
    Button receivebtns;


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("MyApp3_0");
        stage.setWidth(960);
        stage.setHeight(540);
        stage.setResizable(false);

        String myname[] = new String[1];
        Game game = new Game();
        myname[0] = "MyName";
        String[] distributedsuit = new String[14];
        int[] distributednum = new int[14];

        Group lobbyg;   //ロビー画面を表示させるためのグループ
        Group renameg;  //ネームを変更する時に表示させるグループ
        Group connectg;//サーバーと接続中に出されるグループ
        Group gameg;    //ゲームを始めるときに表示させるグループ
        Group started;  //ゲームが始まってから表示させるグループ

        //ロビー画面を表示させるためのグループ
        lobbyg = new Group();
        Button rebtn = new Button("rename");
        Button ebtn = new Button("enter");
        String s = "Your name :";
        Label namelbl = new Label();
        rebtn.setPrefWidth(80);
        ebtn.setPrefWidth(80);
        lobbyg.getChildren().addAll(namelbl,rebtn,ebtn);
        namelbl.setText(s + myname[0]);
        rebtn.setLayoutX(440);
        rebtn.setLayoutY(250);
        ebtn.setLayoutX(440);
        ebtn.setLayoutY(280);
        root.getChildren().add(lobbyg);
        //ロビーぐる

        //ゲームの背景となるオブジェクト類
        gameg = new Group();
        Rectangle bg1 = new Rectangle(140,50,680,320);
        Rectangle bg2 = new Rectangle(150,60,660,300);
        bg1.setFill(Color.WHEAT);
        bg2.setFill(Color.GREEN);
        Button passbtn = new Button("MyPass");
        //Button receivepassbtn = new Button("ReceivePass");
        Button clbutton = new Button("Clear");
        Button sendbtn = new Button("send");
        Button receivebtn = new Button("receive");
        Button comeonbaby = new Button("kumon");
        Button exgame = new Button ("exGame");
        //Button sgame = new Button("sGame");
        Button engame = new Button("enGame");
        Button odrbtn = new Button("Order");
        odrbtn.setPrefWidth(80);
        engame.setPrefWidth(80);
        exgame.setPrefWidth(80);
        //sgame.setPrefWidth(80);
        comeonbaby.setPrefWidth(80);
        passbtn.setOnMouseClicked(e->MyPass(game));
        //receivepassbtn.setOnMouseClicked(e->Pass(game));

        passbtn.setMinWidth(80);
        passbtn.setMinHeight(80);
        //receivepassbtn.setMinWidth(80);
        //receivepassbtn.setMinHeight(80);
        clbutton.setMinWidth(80);
        clbutton.setMinHeight(80);
        sendbtn.setMinWidth(80);
        sendbtn.setMinHeight(20);
        receivebtn.setMinWidth(80);
        receivebtn.setMinHeight(20); 
        gameg.getChildren().addAll(bg1,bg2);
        gameg.getChildren().addAll(passbtn,clbutton,sendbtn,receivebtn,comeonbaby,exgame,engame,odrbtn);
        odrbtn.setLayoutX(440);
        odrbtn.setLayoutY(20);
        engame.setLayoutX(860);
        engame.setLayoutY(300);
        exgame.setLayoutX(860);
        exgame.setLayoutY(360);
        //gameg.getChildren().add(receivepassbtn);
        passbtn.setLayoutX(150);
        passbtn.setLayoutY(200);
        //receivepassbtn.setLayoutX(150);
        //receivepassbtn.setLayoutY(100);
        clbutton.setLayoutX(730);
        clbutton.setLayoutY(100);
        sendbtn.setLayoutX(730);
        sendbtn.setLayoutY(220);
        receivebtn.setLayoutX(730);
        receivebtn.setLayoutY(200);
        root.getChildren().add(gameg);
        gameg.setVisible(false);
        //ゲームぐる

        //名前を変更するときのボタンと表示させるオブジェクト類
        renameg = new Group();                      //これらを統合するグループ
        Label renamelbl = new Label("Enter your name.");
        Rectangle renamerect = new Rectangle(200,140);
        TextField textfield = new TextField();      //名前を入力するところ
        Button btncancel = new Button("Cancel");    //キャンセルボタン
        Button btnOk = new Button("OK");            //OKボタン
        renamerect.setArcHeight(20);
        renamerect.setArcWidth(20);
        renamerect.setFill(Color.WHITE);
        renamerect.setStroke(Color.GRAY);
        textfield.setPrefWidth(180);
        btncancel.setPrefWidth(80);
        btnOk.setPrefWidth(80);

        renameg.getChildren().addAll(renamerect,renamelbl,textfield,btnOk,btncancel);
        renamerect.setLayoutX(-100);
        renamelbl.setLayoutX(-50);
        renamelbl.setLayoutY(20);
        textfield.setLayoutX(-90);
        textfield.setLayoutY(60);
        btnOk.setLayoutX(-85);
        btnOk.setLayoutY(100);
        btncancel.setLayoutX(5);
        btncancel.setLayoutY(100);

        root.getChildren().add(renameg);
        renameg.setLayoutX(480);
        renameg.setLayoutY(200);
        renameg.setVisible(false);
        //リネームぐる

        //connectg
        
        connectg = new Group();
        Label connectlabel = new Label("Connecting...");
        Button connectedbtn = new Button("Connected");
        connectlabel.setFont(new Font(50));
        connectedbtn.setPrefWidth(80);
        connectg.getChildren().addAll(connectlabel,connectedbtn);
        connectedbtn.setLayoutX(440);
        connectedbtn.setLayoutY(340);
        connectlabel.setLayoutX(340);
        connectlabel.setLayoutY(200);
        root.getChildren().add(connectg);
        connectg.setVisible(false);

        txtf = new TextField();
        txts = new TextField();
        txta = new TextArea();
        receivebtns = new Button("recieves");
        txtf.setPrefWidth(160);
        txts.setPrefWidth(160);
        receivebtns.setPrefWidth(80);
        receivebtns.setOnMouseClicked(e->recieveStartGame(game, connectg, gameg, myname[0], distributedsuit, distributednum));
        txta.setPrefColumnCount(10);
        txta.setPrefRowCount(3);
        txta.setEditable(false);
        root.getChildren().addAll(receivebtns,txta,txts,txtf);
        txta.setLayoutX(730);
        txta.setLayoutY(0);
        txtf.setLayoutX(730);
        txtf.setLayoutY(80);
        txts.setLayoutX(100);
        txts.setLayoutY(30);
        receivebtns.setLayoutX(100);
        receivebtns.setLayoutY(55);
        

        //ゲームの開始を受け取るめそっど等
        //enterGame(gameg);          //STARTGAME
        //exitGame(gameg);
        String order = "ORDER,P2,13,MyName,14,P0,13,P1,14";        
        recieveOrder(game, myname[0], order);       //ORDER,
        String order2 = "ORDER,P2,13,P0,13,P1,14,MyName,14";  
            //game.setplayernames(players);             プレイヤーの名前を設定
            //game.sethandcounts(handcounts);           プレイヤーの手札を設定
            //game.createplayers();                     プレイヤーを作成
        String hands = "HANDS,S,3,S,4,H,4,D,4,d,5,D,6,D,7,S,9,H,9,D,9,C,10,S,2,H,2,D,2";
        recieveMyCards(distributedsuit,distributednum,hands);     //自分のカードを受け取ってdistributedに代入
        game.centerstage2 = new CenterStage();  //センターステージを作成

        started = new Group();

        root.getChildren().add(started);
        started.setVisible(false);


        //個々にボタン系統のClicked
        btnOk.setOnMouseClicked(e->{
            rename(namelbl, myname, textfield);
            exitrename(renameg);
        });
        btncancel.setOnMouseClicked(e->exitrename(renameg));
        rebtn.setOnMouseClicked(e->showrename(renameg));        
        ebtn.setOnMouseClicked(e->{           
            exitLobby(lobbyg);
            showConnect(connectg);
        });  //enterボタン(ゲーム画面を表示)
        connectedbtn.setOnMouseClicked(e->{
            initGame(game);
            startGame(game, connectg, gameg);
        });
        comeonbaby.setOnMouseClicked(e->finishGame(game,started,gameg,lobbyg));
        clbutton.setOnMouseClicked(e->clear(game,started));
        sendbtn.setOnMouseClicked(e->send());
        receivebtn.setOnMouseClicked(e->receive(game,started));

        exgame.setOnMouseClicked(e->removeStarted(started));
        //sgame.setOnMouseClicked(e->showStarted(started));
        engame.setOnMouseClicked(e->{makeStarted(game,started,distributedsuit,distributednum);showStarted(started);});
        odrbtn.setOnMouseClicked(e->{recieveOrder(game, myname[0], order2);});

        stage.setScene(new Scene(root));
        stage.show();
    }
    void showLobby(Group lobbyg){//ルームに入るためのボタンとrenameするためのボタン
        lobbyg.setVisible(true);
    }
    void exitLobby(Group lobbyg){//ロビーの表示をおわらせる
        lobbyg.setVisible(false);
    }
    void showConnect(Group connectg){
        connectg.setVisible(true);
    }
    void exitConnect(Group connectg){
        connectg.setVisible(false);
    }
    void initGame(Game game){
        game.initGame();
    }
    void startGame(Game game, Group connectg, Group gameg){
        exitConnect(connectg);
        enterGame(gameg);
    }
    void enterGame(Group gameg){
        gameg.setVisible(true);
    }
    void exitGame(Group gameg){
        gameg.setVisible(false);
    }
    void makeStarted(Game game, Group started, String[] distributedsuit, int[] distributednum){
        game.myplayer.createMyCards(distributedsuit, distributednum);    //自分のカードをつくる
        game.setCardSelected();                 //自分のカードのクリック設定
        game.otherplayers[0].createPlayerCards();                 //p0のカードを作成
        game.otherplayers[1].createPlayerCards();
        game.otherplayers[2].createPlayerCards();
        started.getChildren().addAll(game.otherplayers[0].dispplayer,game.otherplayers[1].dispplayer,game.otherplayers[2].dispplayer,game.myplayer.dispplayer.lbl);
        started.getChildren().addAll(game.otherplayers[0].cardsgroup,game.otherplayers[1].cardsgroup,game.otherplayers[2].cardsgroup);
        for(int i=0;i<game.myplayer.inithandcount;i++){
            started.getChildren().add(game.myplayer.mycards[i].rectgp);
        }
        setPlayersNameLocation(game);
        setMyCardsLocation(game.myplayer);
        setPlayer0CardsLocation(game.otherplayers[0]);
        setPlayer1CardsLocation(game.otherplayers[1]);
        setPlayer2CardsLocation(game.otherplayers[2]);
        game.myplayer.dispplayer.lbl.setLayoutX(20);
        game.myplayer.dispplayer.lbl.setLayoutY(400);
        game.myplayer.dispplayer.lbl.setTextFill(Color.BISQUE);

    }

    void recieveStartGame(Game game, Group connectg, Group gameg, String myname, String[] distributedsuit, int[] distributednum){
        String s;
        s = txts.getText();
        String ss[] = s.split(",");
        switch(ss[0]){
            case "STARTGAME": startGame(game, connectg, gameg); break;//ゲームを開始する
            case "ORDER" : recieveOrder(game, myname, s);       break;//順番を決定する
            case "HANDS" : recieveMyCards(distributedsuit,distributednum,s);       break;//カードを受け取る
            case "TURN"  : setTurn(game, ss[1]);                break;//ターンを決める
        }
        txts.setText("");
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }
    void showrename(Group group){
        group.setVisible(true);
    }
    void rename(Label lbl, String[] myname, TextField textField){
        myname[0]=textField.getText();
        lbl.setText("Your name :"+myname[0]);
    }
    void exitrename(Group group){
        group.setVisible(false);
    }
    void showStarted(Group started){
        started.setVisible(true);
    }
    void removeStarted(Group started){
        started.getChildren().clear();
    }

    void setPlayersNameLocation(Game game){
        game.otherplayers[0].dispplayer.setLayoutX(50);
        game.otherplayers[0].dispplayer.setLayoutY(50);
        game.otherplayers[1].dispplayer.setLayoutX(480);
        game.otherplayers[1].dispplayer.setLayoutY(0);
        game.otherplayers[2].dispplayer.setLayoutX(860);
        game.otherplayers[2].dispplayer.setLayoutY(50);
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
    void displayCenterStage(Game game, Group started){
        started.getChildren().add(game.centerstage2.field);
        game.centerstage2.field.setLayoutX(420);
        game.centerstage2.field.setLayoutY(220);
        game.centerstage2.field.setOnMouseClicked(e->transmit(game, started));
    }
    void transmit(Game game,Group started){
        int n = game.myplayer.inithandcount;
        int j = 0;
        String[] sendsuits;
        int[] sendnums;
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
        game.myplayer.myturn = false;
        sendsuits = new String[game.centerstage2.mode];
        sendnums = new int[game.centerstage2.mode];
        int k=0;
        for(int i=0;i<n;i++){
            if(game.myplayer.mycards[i].selected == true){
                started.getChildren().remove(game.myplayer.mycards[i].rectgp);
                sendsuits[k] =  game.myplayer.mycards[i].suit;
                sendnums[k]  =  game.myplayer.mycards[i].num;
                k++;
                 game.myplayer.mycards[i].selected = false;
                 game.myplayer.mycards[i].removed = true;
            }
        }
        //Pass(game);
        game.myplayer.cards_of_num = 0;
        game.myplayer.setMyCardX();
        game.myplayer.handcount -= game.centerstage2.mode;
        printCenterCards(game,sendsuits,sendnums);//カードを表示する
        sendCards(game,sendsuits,sendnums);//カードを送る
        if(game.myplayer.handcount==0){
            game.myplayer.finish = true;
            game.rank++;
            //if(game.rank==3) finishGame(started);
        }
    }
    void setMyCardsLocation(MyPlayer myplayer){
        myplayer.setMyCardY();
        myplayer.setMyCardX();
    }
    void clear(Game game, Group started){
        game.centerstage2.initCenterStage(); 
        game.centerstage2.mode = 0;
        displayCenterStage(game, started);
    }

    void setTurn(Game game, String s){
        game.otherplayers[0].myturn=false;
        game.otherplayers[1].myturn=false;
        game.otherplayers[2].myturn=false;
        game.myplayer.myturn=false;
        game.otherplayers[0].dispplayer.exitplayerturn();
        game.otherplayers[1].dispplayer.exitplayerturn();
        game.otherplayers[2].dispplayer.exitplayerturn();
        exitMyTurn(game.myplayer);
        if(s.equals(game.otherplayers[0].name)){
            game.otherplayers[0].myturn = true;
            game.otherplayers[0].dispplayer.playerturn();
        }else if(s.equals(game.otherplayers[1].name)){
            game.otherplayers[1].myturn = true;
            game.otherplayers[1].dispplayer.playerturn();
        }else if(s.equals(game.otherplayers[2].name)){
            game.otherplayers[2].myturn = true;
            game.otherplayers[2].dispplayer.playerturn();
        }else if(s.equals(game.myplayer.name)){
            game.myplayer.myturn = true;
            myTurn(game);
        }
    }
    void myTurn(Game game){
        int num = game.centerstage2.num;
        int n = game.myplayer.inithandcount;
        int j=game.myplayer.inithandcount;
        
        if(num==1||num==2){
            for(int i=n-1;i>0;i--){
                if(3<=game.myplayer.mycards[i].num){
                    j=i+1;
                    break;
                }
            }
        }
        else{
            for(int i=0;i<game.myplayer.inithandcount;i++){
                if(num<game.myplayer.mycards[i].num){
                    j=i;
                    break;
                }
            }
            if(j==n){
                for(int i=n-1;i>0;i--){
                    if((3 < game.myplayer.mycards[i].num)&&(game.myplayer.mycards[i].num<13)){
                        j=i+1;
                        break;
                    }
                }
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
        sendPass();
    }
    void printCenterCards(Game game,String[]sendnums,int[] printnums){//中央にカードを配置するように命令
        game.centerstage2.setCenterStageNum(printnums[0]);
        switch(game.centerstage2.mode){
            //case 0: setCenterCard(printnums);break;//modeを決める
            case 1: printCenterCard1(game,sendnums,printnums); break;
            case 2: printCenterCard2(game,sendnums,printnums); break;
            case 3: printCenterCard3(game,sendnums,printnums); break;
            case 4: printCenterCard4(game,sendnums,printnums); break;
        }
    }
    void printCenterCard1(Game game,String[] sendnums,int[] printnums){//centercardの表示1枚
        CenterCard centercard = new CenterCard(sendnums[0], printnums[0]);
        centercard.rectgp.setVisible(false);
        game.centerstage2.field.getChildren().add(centercard.rectgp);
        centercard.rectgp.setLayoutX(37.5);
        centercard.rectgp.setLayoutY(30);
        centercard.rectgp.setVisible(true);
    }
    void printCenterCard2(Game game,String[] sendnums,int[] printnums){//2枚
        CenterCard[] centercards = new CenterCard[2];
        for(int i=0;i<2;i++){
            centercards[i] = new CenterCard(sendnums[i], printnums[i]);
            centercards[i].rectgp.setVisible(false);
            game.centerstage2.field.getChildren().add(centercards[i].rectgp);
        }
        centercards[0].rectgp.setLayoutX(27.5);
        centercards[1].rectgp.setLayoutX(47.5);
        for(int i=0;i<2;i++){
            centercards[i].rectgp.setLayoutY(30);
            centercards[i].rectgp.setVisible(true);
        }
    }
    void printCenterCard3(Game game,String[] sendnums,int[] printnums){//3枚
        CenterCard[] centercards = new CenterCard[3];
        for(int i=0;i<3;i++){
            centercards[i] = new CenterCard(sendnums[i], printnums[i]);
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
    }
    void printCenterCard4(Game game,String[] sendnums,int[] printnums){//4枚
        CenterCard[] centercards = new CenterCard[4];
        for(int i=0;i<4;i++){
            centercards[i] = new CenterCard(sendnums[i], printnums[i]);
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
    }
    void recieveOrder(Game game, String myname, String s){
        String names[] = new String[4];
        int handcounts[] = new int[4];
        String[] ss = s.split(",");
        names[0] = ss[1];
        names[1] = ss[3];
        names[2] = ss[5];
        names[3] = ss[7];

        handcounts[0] = Integer.parseInt(ss[2]);
        handcounts[1] = Integer.parseInt(ss[4]);
        handcounts[2] = Integer.parseInt(ss[6]);
        handcounts[3] = Integer.parseInt(ss[8]);

        game.setplayernames(names);
        game.sethandcounts(handcounts);
        game.createPlayers(myname);
    }
    void recievePlayersHandcounts(int[] handcount){//手札の枚数を受け取る
        handcount[0] = 13;
        handcount[1] = 14;
        handcount[2] = 14;
        handcount[3] = 13;
    }
    void recieveMyCards(String[] distributedsuit, int[] distributednum, String s){//カードを受け取る

        String ss[] = s.split(",");
        int n = (ss.length-1)/2;
        
        for(int i=0;i<n;i++){
            distributedsuit[i] = ss[2*i+1];
            distributednum[i] = Integer.parseInt(ss[2*i+2]);
        }

        // distributedsuit[0]  = "H";        distributednum[0] = 3;
        // distributedsuit[1]  = "D";        distributednum[1] = 3;
        // distributedsuit[2]  = "S";        distributednum[2] = 4;
        // distributedsuit[3]  = "C";        distributednum[3] = 4;
        // distributedsuit[4]  = "H";        distributednum[4] = 4;
        // distributedsuit[5]  = "D";        distributednum[5] = 5;
        // distributedsuit[6]  = "S";        distributednum[6] = 5;
        // distributedsuit[7]  = "C";        distributednum[7] = 6;
        // distributedsuit[8]  = "S";        distributednum[8] = 7;
        // distributedsuit[9]  = "C";        distributednum[9] = 9;
        // distributedsuit[10] = "S";        distributednum[10]= 2;
        // distributedsuit[11] = "H";        distributednum[11]= 2;
        // distributedsuit[12] = "D";        distributednum[12]= 2;
        // distributedsuit[13] = "C";        distributednum[13]= 2;
    }
    //送信系
    void sendCards(Game game, String[] sendsuits, int[] sendnums){//カードを送る
        String s = game.myplayer.name;
        for(int i=0;i<game.centerstage2.mode;i++){
            String s1 = String.format(","+sendsuits[i]+",%d",sendnums[i]);
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
    void receive(Game game, Group started){
        String s = txtf.getText();
        String ss[] = s.split(",");
        switch(ss[0]){
            case "TURN": setTurn(game, ss[1]); break;
            case "CLEAR": clear(game,started);  break;
            default : receivePlayerscards(game, s);
        }
        txtf.setText("");
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }
    void receivePlayerscards(Game game, String s){
        String[] results = s.split(",");
        int halfn = (results.length-1)/2;
        if(game.centerstage2.mode==0){
            game.centerstage2.mode = halfn;
        }
        String[] printsuits = new String[halfn];
        int[] printnums = new int[halfn];
        for(int i=0;i<halfn;i++){
            printsuits[i] = results[2*i+1]; 
            printnums[i] = Integer.parseInt(results[2*i+2]);
        }
        printCenterCards(game,printsuits,printnums);
        reduceHandcount(game);
        displayHandcount(game);
        //Pass(game);
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
    void finishGame(Game game, Group started, Group gameg, Group lobbyg){
        Button btnfn = new Button("終了");
        btnfn.setPrefWidth(80);
        btnfn.setOnMouseClicked(e->{removeStarted(started); exitGame(gameg);showLobby(lobbyg);});
        Button btncn = new Button("続ける");
        btncn.setPrefWidth(80);
        btncn.setOnMouseClicked(e->{removeStarted(started); initGame(game);});
        Rectangle recatangle = new Rectangle(200,200,550,160);
        recatangle.setFill(Color.WHEAT);
        Label exitlbl = new Label("ゲーム終了!");
        exitlbl.setFont(new Font(58));
        started.getChildren().addAll(recatangle,exitlbl,btnfn,btncn);
        exitlbl.setLayoutX(350);
        exitlbl.setLayoutY(210);
        btnfn.setLayoutX(360);
        btncn.setLayoutX(500);
        btnfn.setLayoutY(300);
        btncn.setLayoutY(300);
    }
    void send(){
        String s = txtf.getText();
        txtf.setText("");
        String txt = txta.getText();
        txt += s+"\n";
        txta.setText(txt);
    }
}
