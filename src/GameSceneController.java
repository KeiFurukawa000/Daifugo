

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
    private LinkedList<Integer> selectedCardIndexList;
    private LinkedList<Card> selectedCardList;

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
        this.stage = new ArrayList<>();
        selectedCardIndexList = new LinkedList<>();
        selectedCardList = new LinkedList<>();
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
        selectedCardsIndex.clear();
        selectedCardList.clear();
        selectedCardIndexList.clear();
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
        
        //drawMyHand();
        /*
        ArrayList<Integer> selectableIndex = getSelectableCardIndex();
        for (int i = 0; i < selectableIndex.size(); i++) {
            myGroup.getChildren().get(selectableIndex.get(i)).setDisable(false);;
        }
        */
    }

    public void endMyTurn() {
        passButton.setDisable(true);
        putButton.setDisable(true);
        myGroup.setDisable(true);
        myGroup.setOpacity(0.5);
        selectedCardIndexList.clear();
        
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

    private ArrayList<Integer> getSelectableCardIndex() {
        ArrayList<Integer> selectableIndex = new ArrayList<>();
        if (stage.isEmpty()) {
            System.out.println("Empty");
            for (int i = 0; i < myPlayer.hand.getSize(); i++) {
                selectableIndex.add(i);
            }
            return selectableIndex;
        }
        Card lastCard = stage.get(stage.size()-1);
        for (int i = 0; i < myPlayer.hand.getList().size(); i++) {
            int num = myPlayer.hand.getList().get(i).getNumber();
            if (lastCard.getNumber() < num) {
                selectableIndex.add(i);
                System.out.println(i);
            }
        }
        return selectableIndex;
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

                Card enteredCard = myPlayer.hand.getCard(myGroup.getChildren().indexOf(imageView));
                int enteredCardNum = enteredCard.getNumber();
                
                if (!stage.isEmpty() && selectedCards.size() >= stage.size()) return;
                if (selectedCards.isEmpty()) {
                    if (stage.isEmpty()) {
                        imageView.setTranslateY(-10);
                    }
                    else {
                        int stageLastNum = stage.get(stage.size()-1).getNumber();
                        if (enteredCardNum > stageLastNum) imageView.setTranslateY(-10);
                    }
                }
                else {
                    Suit enteredCardSuit = enteredCard.getSuit();
                    if (selectedCards.size() == 1) {
                        Card selectedCard = selectedCardList.peekFirst();
                        int selectedCardNum = selectedCard.getNumber();
                        Suit selectedCardSuit = selectedCard.getSuit();
                        if (stage.isEmpty()) {
                            if (selectedCardNum == enteredCardNum || (selectedCardNum + 1 == enteredCardNum && selectedCardSuit.equals(enteredCardSuit))) imageView.setTranslateY(-10);
                            return;
                        }
                        ArrayList<Integer> stageNumList = new ArrayList<>();
                        for (int j = 0; j < stage.size(); j++) stageNumList.add(stage.get(j).getNumber());
                        if (stageNumList.stream().allMatch(stageNumList.get(0)::equals)) {
                            if (selectedCardNum == enteredCardNum) imageView.setTranslateY(-10);
                        }
                        else {
                            if (selectedCardNum + 1 == enteredCardNum && selectedCardSuit.equals(enteredCardSuit)) imageView.setTranslateY(-10);
                        }
                    }
                    else {
                        Card selectedCard = selectedCardList.peekLast();
                        int selectedCardNum = selectedCard.getNumber();
                        Suit selectedCardSuit = selectedCard.getSuit();
                        ArrayList<Integer> selectedCardNumList = new ArrayList<>();
                        for (int j = 0; j < selectedCardList.size(); j++) selectedCardNumList.add(selectedCardList.get(j).getNumber());
                        if (selectedCardNumList.stream().allMatch(selectedCardNumList.get(0)::equals)) {
                            if (selectedCardNum == enteredCardNum) imageView.setTranslateY(-10);
                        }
                        else {
                            if (selectedCardNum + 1 == enteredCardNum && selectedCardSuit.equals(enteredCardSuit)) imageView.setTranslateY(-10);
                        }
                    }
                }

                if (selectedCards.size() == myGroup.getChildren().size()-1) myGroup.setTranslateY(-10);
            };
            imageView.setOnMouseEntered(mouseEnteredHandler);
            EventHandler<MouseEvent> mouseExitedHandler = (event) -> {
                if (selectedCards.containsKey(imageView)) return;
                imageView.setTranslateY(0);
                myGroup.setTranslateY(0);
            };
            imageView.setOnMouseExited(mouseExitedHandler);
            /*
            EventHandler<MouseEvent> mouseClickedHandler = (event) -> {
                if (selectedCards.containsKey(imageView)) {
                    imageView.setTranslateY(0);
                    selectedCards.remove(imageView);
                    myGroup.setTranslateY(0);
                    selectedCardIndexList.remove(myGroup.getChildren().indexOf(imageView));
                    if (selectedCards.size() == 0) putButton.setDisable(true);
                }
                else {
                    imageView.setTranslateY(-10);
                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                    selectedCardIndexList.add(myGroup.getChildren().indexOf(imageView));
                    putButton.setDisable(false);
                    if (selectedCards.size() == myGroup.getChildren().size()) myGroup.setTranslateY(-10);
                }
            };
            */
            EventHandler<MouseEvent> mouseClickedHandler = (event) -> {
                Card clickedCard = myPlayer.hand.getCard(myGroup.getChildren().indexOf(imageView));
                int clickedCardNum = clickedCard.getNumber();
                if (!selectedCards.containsKey(imageView)) {             
                    if (!stage.isEmpty() && selectedCards.size() >= stage.size()) return;
                    if (selectedCards.isEmpty()) {
                        if (stage.isEmpty()) {
                            imageView.setTranslateY(-10);
                            selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                            selectedCardList.add(clickedCard);
                            putButton.setDisable(false);
                        }
                        else {
                            int stageLastNum = stage.get(stage.size()-1).getNumber();
                            if (clickedCardNum > stageLastNum) {
                                imageView.setTranslateY(-10);
                                selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                selectedCardList.add(clickedCard);
                                if (stage.size() == 1) putButton.setDisable(false);
                            }
                        }
                    }
                    else {
                        Suit enteredCardSuit = clickedCard.getSuit();
                        if (selectedCards.size() == 1) {
                            Card selectedCard = selectedCardList.peekFirst();
                            int selectedCardNum = selectedCard.getNumber();
                            Suit selectedCardSuit = selectedCard.getSuit();

                            if (stage.isEmpty()) {
                                if (selectedCardNum == clickedCardNum || (selectedCardNum + 1 == clickedCardNum && selectedCardSuit.equals(enteredCardSuit))) {
                                    imageView.setTranslateY(-10);
                                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                    selectedCardList.add(clickedCard);
                                }

                                ArrayList<Integer> selectedCardNumList = new ArrayList<>();
                                for (int j = 0; j < selectedCardList.size(); j++) selectedCardNumList.add(selectedCardList.get(j).getNumber());
                                if (selectedCardNumList.stream().allMatch(selectedCardNumList.get(0)::equals)) {
                                    putButton.setDisable(false);
                                }
                                else {
                                    if (selectedCards.size() >= 3) putButton.setDisable(false);
                                    else putButton.setDisable(true);
                                }
                                return;
                            }
                            ArrayList<Integer> stageNumList = new ArrayList<>();
                            for (int j = 0; j < stage.size(); j++) stageNumList.add(stage.get(j).getNumber());
                            if (stageNumList.stream().allMatch(stageNumList.get(0)::equals)) {
                                if (selectedCardNum == clickedCardNum) {
                                    imageView.setTranslateY(-10);
                                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                    selectedCardList.add(clickedCard);
                                }
                            }
                            else {
                                if (selectedCardNum + 1 == clickedCardNum && selectedCardSuit.equals(enteredCardSuit)) {
                                    imageView.setTranslateY(-10);
                                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                    selectedCardList.add(clickedCard);
                                }
                            }
                        }
                        else {
                            Card selectedCard = selectedCardList.peekLast();
                            int selectedCardNum = selectedCard.getNumber();
                            Suit selectedCardSuit = selectedCard.getSuit();
                            ArrayList<Integer> selectedCardNumList = new ArrayList<>();
                            for (int j = 0; j < selectedCardList.size(); j++) selectedCardNumList.add(selectedCardList.get(j).getNumber());
                            if (selectedCardNumList.stream().allMatch(selectedCardNumList.get(0)::equals)) {
                                if (selectedCardNum == clickedCardNum) {
                                    imageView.setTranslateY(-10);
                                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                    selectedCardList.add(clickedCard);
                                }
                            }
                            else {
                                if (selectedCardNum + 1 == clickedCardNum && selectedCardSuit.equals(enteredCardSuit)) {
                                    imageView.setTranslateY(-10);
                                    selectedCards.put(imageView, myGroup.getChildren().indexOf(imageView));
                                    selectedCardList.add(clickedCard);
                                }
                            }
                        }

                        ArrayList<Integer> selectedCardNumList = new ArrayList<>();
                        for (int j = 0; j < selectedCardList.size(); j++) selectedCardNumList.add(selectedCardList.get(j).getNumber());
                        if (stage.isEmpty()) {
                            if (selectedCardNumList.stream().allMatch(selectedCardNumList.get(0)::equals)) {
                                putButton.setDisable(false);
                            }
                            else {
                                if (selectedCards.size() >= 3) putButton.setDisable(false);
                                else putButton.setDisable(true);
                            }
                        }
                        else {
                            if (selectedCards.size() >= stage.size()) putButton.setDisable(false);
                        }
                        //if (stage.isEmpty() || selectedCards.size() == stage.size()) putButton.setDisable(false);
                    }
                    if (selectedCards.size() == myGroup.getChildren().size()) myGroup.setTranslateY(-10);
                    
                }
                else {
                    imageView.setTranslateY(0);
                    myGroup.setTranslateY(0);

                    selectedCards.remove(imageView);
                    selectedCardList.remove(clickedCard);
                
                    if (stage.isEmpty()) {
                        if (selectedCards.isEmpty()) {
                            putButton.setDisable(true);
                        }
                        else {
                            if (selectedCards.size() == 1) {
                                putButton.setDisable(false);
                                return;
                            }
                            ArrayList<Integer> selectedCardNumList = new ArrayList<>();
                            for (int j = 0; j < selectedCardList.size(); j++) selectedCardNumList.add(selectedCardList.get(j).getNumber());
                            if (selectedCardNumList.stream().allMatch(selectedCardNumList.get(0)::equals)) {
                                return;
                            }
                            else {
                                if (selectedCards.size() < 3) putButton.setDisable(true);
                            }
                        }
                    }
                    else {
                        if (selectedCards.size() < stage.size()) putButton.setDisable(true);
                    }
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
            queue.peek().handCount -= Integer.parseInt(stageArray[1]);
            drawHand(queue.peek());
            if (queue.peek().handCount == 0) {
                win(queue.peek());
                return;
            }
            else if (Integer.parseInt(stageArray[1]) == 0) {
                rotate();
            }
        }
        else {
            Card[] cards = new Card[stageArray.length-1];
            System.out.println(cards.length);
            for (int i = 0; i < cards.length; i++) {
                String[] meta = stageArray[i].split(",");
                String suit = meta[0];
                String num = meta[1];
                System.out.println(suit + " " + num);
                cards[i] = Card.strToCard(suit, num);
            }
            
            drawStage(cards);
            
            queue.peek().handCount -= Integer.parseInt(stageArray[stageArray.length-1]);
            drawHand(queue.peek());
            if (queue.peek().handCount == 0) win(queue.peek());
            else rotate();
        }
        drawMyHand();
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