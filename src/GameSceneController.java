

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GameSceneController {
    private Stage stage;
    private IDaifugoApp app;

    private LinkedList<Player> queue;
    private Player myPlayer;

    private HashMap<Pos, Group> handGroupMap;
    private HashMap<ImageView, Integer> selectedCards;
    private ClientConnection connection;

    @FXML
    private Group myGroup;
    @FXML
    private Group rightGroup;
    @FXML
    private Group leftGroup;
    @FXML
    private Group topGroup;
    @FXML
    private Button passButton;
    @FXML
    private Button putButton;
    @FXML
    private Group stageGroup;

    public void init(Stage stage, IDaifugoApp app, ClientConnection connection) {
        this.stage = stage;
        this.app = app;
        this.connection = connection;
        queue = new LinkedList<>();
        handGroupMap = new HashMap<>(){{
            put(Pos.MY, myGroup);
            put(Pos.LEFT, leftGroup);
            put(Pos.TOP, topGroup);
            put(Pos.RIGHT, rightGroup);
        }};
        selectedCards = new HashMap<>();
    }

    /**
     * プレイヤーのターン順を設定する
     * @param players プレイヤーの名前配列(ターン順にソート済み)
     */
    public void setQueue(String[] players) {
        int myQueueIndex = 0;
        for (int i = 0; i < players.length; i++) {
            String[] meta = players[i].split(",");
            String name = meta[0];
            if (name.equals(app.getName())) myQueueIndex = i;
        }
        System.out.println(myQueueIndex);
        HashMap<String, Pos> posMap = new HashMap<>();
        switch (myQueueIndex) {
            case 0:
                posMap.put(players[0], Pos.MY);
                posMap.put(players[1], Pos.LEFT);
                posMap.put(players[2], Pos.TOP);
                posMap.put(players[3], Pos.RIGHT);
            case 1:
                posMap.put(players[0], Pos.RIGHT);
                posMap.put(players[1], Pos.MY);
                posMap.put(players[2], Pos.LEFT);
                posMap.put(players[3], Pos.TOP);
            case 2:
                posMap.put(players[0], Pos.TOP);
                posMap.put(players[1], Pos.RIGHT);
                posMap.put(players[2], Pos.MY);
                posMap.put(players[3], Pos.LEFT);
            case 3:
                posMap.put(players[0], Pos.LEFT);
                posMap.put(players[1], Pos.TOP);
                posMap.put(players[2], Pos.RIGHT);
                posMap.put(players[3], Pos.MY);
        }

        for (int i = 0; i < players.length; i++) {
            String[] meta = players[i].split(",");
            String name = meta[0];
            int handCount = Integer.parseInt(meta[1]);
            Player newPlayer = new Player(name, handCount, posMap.get(players[i]));
            queue.add(newPlayer);
            if (name.equals(app.getName())) {
                myPlayer = newPlayer;
            }
        }
    }

    /**
     * 自分のプレイヤーの手札を設定する
     * @param handArray 手札情報が格納された文字列配列
     */
    public void setMyPlayerHand(String[] handArray) {
        Card[] cards = new Card[handArray.length];
        for (int i = 0; i < handArray.length; i++) {
            String[] meta = handArray[i].split(",");
            String suit = meta[0];
            String num = meta[1];
            cards[i] = Card.strToCard(suit, num);
        }
        myPlayer.setHand(new Hand(cards));
    }

    private Player rotate() {
        Player head = queue.poll();
        queue.add(head);
        return head;
    }

    public void show() {
        
    }

    /********************************************************/
    @FXML
    void onPressedPassButton(ActionEvent event) {

    }

    @FXML
    void onPressedPutButton(ActionEvent event) {
        Integer[] selectedCardsIndex = selectedCards.values().toArray(new Integer[selectedCards.size()]);
        Card[] removedCards = new Card[selectedCardsIndex.length];
        for (int i = 0; i < selectedCardsIndex.length; i++) {
            myGroup.getChildren().remove(selectedCardsIndex[i].intValue());
            removedCards[i] = myPlayer.hand.remove(selectedCardsIndex[i]);
        }
        drawHand(myPlayer);
        connection.RequestPut(app.getLobbyName(), app.getName(), removedCards);
    }

    public void start() {
        for (int i = 0; i < queue.size(); i++) {
            Player player = queue.get(i);
            drawHand(player);
        }
    }

    public void drawHand(Player player) {
        Pos pos = player.getPos();
        if (pos.equals(Pos.MY)) {
            Hand myHand = myPlayer.getHand();
            myHand.sort();
            ImageView[] images = new ImageView[myHand.getSize()];
            for (int i = 0; i < myHand.getSize(); i++) {
                ImageView imageView = drawCard(myHand.getCard(i));
                imageView.setScaleX(0.7);
                imageView.setScaleY(0.7);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setFitHeight(150);
                imageView.setFitWidth(100);
                imageView.setLayoutX(20*i);
                EventHandler<MouseEvent> mouseEnteredHandler = (event) -> {
                    if (selectedCards.containsKey(imageView)) return;
                    imageView.setTranslateY(-10);
                };
                imageView.setOnMouseEntered(mouseEnteredHandler);
                EventHandler<MouseEvent> mouseExitedHandler = (event) -> {
                    if (selectedCards.containsKey(imageView)) return;
                    imageView.setTranslateY(0);
                };
                imageView.setOnMouseExited(mouseExitedHandler);
                EventHandler<MouseEvent> mouseClickedHandler = (event) -> {
                    if (selectedCards.containsKey(imageView)) { 
                        imageView.setTranslateY(0);
                        selectedCards.remove(imageView);
                    }
                    else {
                        imageView.setTranslateY(-10);
                        selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                    }
                };
                imageView.setOnMouseClicked(mouseClickedHandler);
                images[i] = imageView;
            }
            myGroup.getChildren().addAll(images);
        }
        else {
            Group group = handGroupMap.get(pos);
            ImageView[] images = new ImageView[player.getHandCount()];
            for (int i = 0; i < player.getHandCount(); i++) {
                ImageView imageView = new ImageView(new Image("img/card_back.png"));
                imageView.setScaleX(0.5);
                imageView.setScaleY(0.5);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setFitHeight(150);
                imageView.setFitWidth(100);
                if (pos.equals(Pos.LEFT)) {
                    imageView.setTranslateY(10*i);
                    imageView.setRotate(90);
                }
                else if (pos.equals(Pos.TOP)) {
                    imageView.setTranslateX(-10*i);
                    imageView.setRotate(180);
                }
                else {
                    imageView.setTranslateY(-10*i);
                    imageView.setRotate(-90);
                }
                images[i] = imageView;
            }
            group.getChildren().addAll(images);
        }
    }

    public void setStage(String[] stageArray) {
        Card[] cards = new Card[stageArray.length];
        for (int i = 0; i < stageArray.length; i++) {
            String[] meta = stageArray[i].split(",");
            String suit = meta[0];
            String num = meta[1];
            cards[i] = Card.strToCard(suit, num);
        }
        ImageView[] images = new ImageView[cards.length];
        for (int i = 0; i < images.length; i++) {
            ImageView imageView = new ImageView(new Image(cards[i].getImageURL()));
            imageView.setScaleX(0.5);
            imageView.setScaleY(0.5);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setFitHeight(150);
            imageView.setFitWidth(100);
            imageView.setLayoutX(20*i);
            images[i] = imageView;
        }
        stageGroup.getChildren().addAll(images);
    }

    public void drawStage(Card[] cards) {
        for (int i = 0; i < cards.length; i++) {
            ImageView imageView = drawCard(cards[i]);
            imageView.setLayoutX(20*i);
            imageView.setScaleX(0.7);
            imageView.setScaleY(0.7);
            stageGroup.getChildren().add(imageView);
        }
    }

    private ImageView drawCard(Card card) {
        return new ImageView(new Image(card.getImageURL()));
    }

    class Player {
        private String name;
        private int handCount;
    
        private Hand hand;
        private Pos pos;
        
        Player(String name, int handCount, Pos pos) {
            this.name = name;
            this.handCount = handCount;
            this.pos = pos;
        }
    
        public void setHand(Hand hand) {
            this.hand = hand;
        }
    
        public Hand getHand() {
            return hand;
        }
    
        public String getName() {
            return name;
        }
    
        public int getHandCount() {
            return handCount;
        }
    
        public Pos getPos() {
            return pos;
        }
    }
}

enum Pos {
    TOP, RIGHT, LEFT, MY
}