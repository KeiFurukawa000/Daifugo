

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import javafx.scene.input.RotateEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameSceneController {
    private IDaifugoApp app;

    private LinkedList<GamePlayer> queue;
    private GamePlayer myPlayer;

    private HashMap<Pos, Group> handGroupMap;
    private HashMap<Pos, Text>  agariTextMap;
    private HashMap<ImageView, Integer> selectedCards;
    private ClientConnection connection;

    private ArrayList<Card> stage;

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
    @FXML
    private Text gameCountText;
    @FXML
    private Text leftAgariText;
    @FXML
    private Text myAgariText;
    @FXML
    private Text rightArariText;
    @FXML
    private Text topAgariText;

    public void init(Stage stage, IDaifugoApp app, ClientConnection connection, int currentGameCount, int maxGameCount) {
        this.app = app;
        this.connection = connection;
        queue = new LinkedList<>();
        handGroupMap = new HashMap<>(){{
            put(Pos.MY, myGroup);
            put(Pos.LEFT, leftGroup);
            put(Pos.TOP, topGroup);
            put(Pos.RIGHT, rightGroup);
        }};
        agariTextMap = new HashMap<>(){{
            put(Pos.MY, myAgariText);
            put(Pos.LEFT, leftAgariText);
            put(Pos.TOP, topAgariText);
            put(Pos.RIGHT, rightArariText);
        }};
        selectedCards = new HashMap<>();
        gameCountText.setText("GAME " + currentGameCount + " / " + maxGameCount);
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
        System.out.println("index: " + myQueueIndex);
        System.out.println(myQueueIndex);
        HashMap<String, Pos> posMap = new HashMap<>();
        switch (myQueueIndex) {
            case 0:
                posMap.put(players[0], Pos.MY);
                posMap.put(players[1], Pos.LEFT);
                posMap.put(players[2], Pos.TOP);
                posMap.put(players[3], Pos.RIGHT);
                break;
            case 1:
                posMap.put(players[0], Pos.RIGHT);
                posMap.put(players[1], Pos.MY);
                posMap.put(players[2], Pos.LEFT);
                posMap.put(players[3], Pos.TOP);
                break;
            case 2:
                posMap.put(players[0], Pos.TOP);
                posMap.put(players[1], Pos.RIGHT);
                posMap.put(players[2], Pos.MY);
                posMap.put(players[3], Pos.LEFT);
                break;
            case 3:
                posMap.put(players[0], Pos.LEFT);
                posMap.put(players[1], Pos.TOP);
                posMap.put(players[2], Pos.RIGHT);
                posMap.put(players[3], Pos.MY);
                break;
        }

        for (int i = 0; i < players.length; i++) {
            String[] meta = players[i].split(",");
            String name = meta[0];
            int handCount = Integer.parseInt(meta[1]);
            GamePlayer newPlayer = new GamePlayer(name, handCount, posMap.get(players[i]));
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

    private GamePlayer rotate() {
        GamePlayer head = queue.poll();
        queue.add(head);
        return head;
    }

    public void show() {
        
    }

    /********************************************************/
    @FXML
    void onPressedPassButton(ActionEvent event) {
        connection.RequestPut(app.getLobbyName(), app.getName(), null);
        endMyTurn();
    }

    @FXML
    void onPressedPutButton(ActionEvent event) {
        ArrayList<Integer> selectedCardsIndex = new ArrayList<>(selectedCards.values());
        Card[] removeCards = new Card[selectedCardsIndex.size()];
        for (int i = 0; i < removeCards.length; i++) { removeCards[i] = myPlayer.hand.getCard(selectedCardsIndex.get(i)); }

        for (int i = 0; i < selectedCardsIndex.size(); i++) {
            myPlayer.hand.remove(removeCards[i]);
        }

        selectedCards.clear();
        putButton.setDisable(true);
        drawMyHand();
        ArrayList<Card> sortedCards = new ArrayList<>(Arrays.asList(removeCards));
        sortedCards.sort(Comparator.comparing(Card::getNumber));
        drawStage(sortedCards.toArray(new Card[sortedCards.size()]));
        connection.RequestPut(app.getLobbyName(), app.getName(), sortedCards.toArray(new Card[sortedCards.size()]));
        endMyTurn();
    }

    public void startMyTurn() {
        passButton.setDisable(false);
        myGroup.setDisable(false);
        myGroup.setOpacity(1);
    }

    public void endMyTurn() {
        passButton.setDisable(true);
        putButton.setDisable(true);
        myGroup.setDisable(true);
        myGroup.setOpacity(0.5);
        
        if (myPlayer.hand.getSize() == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connection.RequestWin(app.getLobbyName(), app.getName());
        }
    }

    public void start() {
        for (int i = 0; i < queue.size(); i++) {
            GamePlayer player = queue.get(i);
            drawHand(player);
        }
    }

    private boolean canPutCard() {
        if (selectedCards.size() <= 0) return false;

        ArrayList<Integer> selectedIndex = new ArrayList<>(selectedCards.values());
        Collections.sort(selectedIndex);
        ArrayList<Card> selected = new ArrayList<>();
        for (int i = 0; i < selectedIndex.size(); i++) {
            selected.add(myPlayer.hand.getCard(selectedIndex.get(i)));
        }

        if (selected.size() == stage.size()) return false;
        else if (selected.get(0).getNumber() <= stage.get(0).getNumber()) return false;
        
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            numbers.add(selected.get(i).getNumber());
        }
        // 数字がすべて等しい場合
        if (numbers.stream().allMatch(numbers.get(0)::equals)) {
            return true;
        }
        // 階段の場合
        else {
            ArrayList<Suit> suits = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++) {
                suits.add(selected.get(i).getSuit());
            }
            if (!suits.stream().allMatch(suits.get(0)::equals)) return false;
            for (int i = 1; i < numbers.size(); i++) {
                if (numbers.get(i-1) == numbers.get(i)-1) continue;
                else return false;
            }
            return true;
        }
        
    }

    private void drawMyHand() {
        myGroup.getChildren().clear();

        myPlayer.hand.sort();
        ImageView[] images = new ImageView[myPlayer.hand.getSize()];
        for (int i = 0; i < myPlayer.hand.getSize(); i++) {
            ImageView imageView = drawCard(myPlayer.hand.getCard(i));
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
                if (selectedCards.size() == myGroup.getChildren().size()-1) myGroup.setTranslateY(-10);
            };
            imageView.setOnMouseEntered(mouseEnteredHandler);
            EventHandler<MouseEvent> mouseExitedHandler = (event) -> {
                if (selectedCards.containsKey(imageView)) return;
                imageView.setTranslateY(0);
                myGroup.setTranslateY(0);
            };
            imageView.setOnMouseExited(mouseExitedHandler);
            EventHandler<MouseEvent> mouseClickedHandler = (event) -> {
                if (selectedCards.containsKey(imageView)) { 
                    imageView.setTranslateY(0);
                    selectedCards.remove(imageView);
                    myGroup.setTranslateY(0);
                    if (selectedCards.size() == 0) putButton.setDisable(true);
                }
                else {
                    imageView.setTranslateY(-10);
                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                    putButton.setDisable(false);
                    if (selectedCards.size() == myGroup.getChildren().size()) myGroup.setTranslateY(-10);
                }
            };
            imageView.setOnMouseClicked(mouseClickedHandler);
            images[i] = imageView;
        }
        myGroup.getChildren().addAll(images);
    }

    public void drawHand(GamePlayer player) {
        Pos pos = player.getPos();
        if (pos.equals(Pos.MY)) {
            drawMyHand();
        }
        else {
            Group group = handGroupMap.get(pos);
            group.getChildren().clear();
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
        if (stageArray[0].equals("NONE")) {
            rotate();
        }
        else if (stageArray[0].equals("CLEAR")) {
            drawStage(new Card[0]);
            rotate();
        }
        else {
            Card[] cards = new Card[stageArray.length];
            for (int i = 0; i < cards.length; i++) {
                String[] meta = stageArray[i].split(",");
                String suit = meta[0];
                String num = meta[1];
                cards[i] = Card.strToCard(suit, num);
            }
            
            drawStage(cards);
            
            queue.peek().handCount -= stageArray.length;
            drawHand(queue.peek());
            if (queue.peek().handCount == 0) win(queue.peek());
            else rotate();
        }
    }

    public void drawStage(Card[] cards) {
        stageGroup.getChildren().clear();
        stage = new ArrayList<>(Arrays.asList(cards));
        Comparator<Card> cardComparator =
            Comparator.comparing(Card::getNumber);
        stage.sort(cardComparator);

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

    private ImageView drawCard(Card card) {
        return new ImageView(new Image(card.getImageURL()));
    }

    private void win(GamePlayer player) {
        queue.remove(player);
        agariTextMap.get(player.getPos()).setVisible(true);
    }
}

class GamePlayer {
    private String name;
    public int handCount;

    public Hand hand;
    private Pos pos;
    
    GamePlayer(String name, int handCount, Pos pos) {
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

enum Pos {
    TOP, RIGHT, LEFT, MY
}