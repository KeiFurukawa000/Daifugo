
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 大富豪アプリケーションクラス
 * @version 0.0.1
 * @author Kei Furukawa
 * <p>
 * このクラスはメイン関数を含むクラスです
 * このクラスはJavaFXによるGUIの変更を行います
 */
public class DaifugoApp extends Application implements IDaifugoApp {
    private String name;
    private String lobbyName;
    private Stage stage;
    private ClientConnection connection;
    private HostLobbySceneController hostLobbyScenecontroller;
    private GuestLobbySceneController guestLobbyScenecontroller;
    private GameSceneController gameSceneController;
    private Scene gameScene;
    private ArrayList<String> currentLobbyMembers;
    private boolean isHost;
    private String password;
    private Thread thread;

    /**
     * メイン関数
     * @param args コマンドライン
     * <p>
     * JavaFX Applicationクラスのstart関数の呼び出しを行います
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * start関数
     * @param stage
     * @throws Exception
     * <p>
     * JavaFX Applicationの起動を行う
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("大富豪");
        stage.setResizable(false);

        String addr = getParameters().getRaw().get(0);
        int port = Integer.parseInt(getParameters().getRaw().get(1));
        connection = new ClientConnection(addr, port, this);
        thread = new Thread(new ClientListen(connection.getSocket(), this));
        thread.start();
        this.stage = stage;
        showStartScene();
    }

    /** Implements of IDaifugoApp */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void showStartScene() throws MalformedURLException {
        if (name != null && (!name.isEmpty() || !name.isBlank())) connection.RequestDeleteAccount(name);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StartScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            StartSceneController controller = (StartSceneController)fxmlLoader.getController();
            controller.SetCallback(connection, this);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            this.stage.show();
        } catch (IOException e) { }
    }

    @Override
    public void showSelectHostOrJoinScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SelectHostOrJoinScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            SelectHostOrJoinSceneController controller = (SelectHostOrJoinSceneController)fxmlLoader.getController();
            controller.SetCallback(this);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void showCreateLobbyScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateLobbyScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            CreateLobbySceneController controller = fxmlLoader.getController();
            controller.SetCallback(this, connection);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void showJoinLobbyScene() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("JoinLobbyScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            JoinLobbySceneController controller = fxmlLoader.getController();
            controller.Init(this, connection);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void showHostLobbyScene(String password, ArrayList<String> members) {
        guestLobbyScenecontroller = null;
        this.password = password;
        this.currentLobbyMembers = members;
        this.isHost = true;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HostLobbyScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            hostLobbyScenecontroller = (HostLobbySceneController)fxmlLoader.getController();
            hostLobbyScenecontroller.Init(this, connection, password, members);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void showGuestLobbyScene(ArrayList<String> members) {
        hostLobbyScenecontroller = null;
        this.currentLobbyMembers = members;
        this.isHost = false;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GuestLobbyScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            guestLobbyScenecontroller = (GuestLobbySceneController)fxmlLoader.getController();
            guestLobbyScenecontroller.Init(this, connection, members);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void setLobbyName(String name) {
        lobbyName = name;
    }

    @Override
    public String getLobbyName() {
        return lobbyName;
    }

    @Override
    public void addLobbyMember(String name) {
        currentLobbyMembers.add(name);
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onJoinGuest(name);
        else guestLobbyScenecontroller.onJoinGuest(name);
    }

    @Override
    public void removeLobbyMember(String name) {
        currentLobbyMembers.remove(name);
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onLeaveGuest(name);
        else guestLobbyScenecontroller.onLeaveGuest(name);
    }

    @Override
    public void readyLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onReadyGuest(name);
        else guestLobbyScenecontroller.onReadyGuest(name);
    }

    @Override
    public void unreadyLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onUnreadyGuest(name);
        else guestLobbyScenecontroller.onUnreadyGuest(name);
    }

    @Override
    public void changeHost(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onChangeHost(name); 
        else guestLobbyScenecontroller.onChangeHost(name);
    }

    @Override
    public void addChatText(String sender, String content) {
        String text = sender + ": " + content;
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.addTextInChat(text);
        else guestLobbyScenecontroller.addTextInChat(text);
    }

    @Override
    public void loadGameScene(int currentGameCount, int maxGameCount) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GameScene.fxml"));
        Parent root = (Parent)fxmlLoader.load();
        gameScene = new Scene(root);
        gameSceneController = fxmlLoader.getController();
        gameSceneController.init(stage, this, connection, currentGameCount, maxGameCount);
        connection.RequestPlayerTurn(lobbyName, name);
    }

    @Override
    public void showGameScene() throws IOException {
        stage.setScene(gameScene);
        gameSceneController.start();
        stage.show();

    }
    
    @Override
    public void setPlayerTurn(String content) throws InterruptedException {
        String[] players = content.split(" ");
        gameSceneController.setQueue(players);
        Thread.sleep(100);
        connection.RequestHand(lobbyName, name);
    }

    @Override
    public void setHand(String content) throws IOException {
        String[] handArray = content.split(" ");
        gameSceneController.setMyPlayerHand(handArray);
        showGameScene();
    }

    @Override
    public void setStage(String content) {
        String[] stageArray = content.split(" ");
        gameSceneController.setStage(stageArray);
    }

    @Override
    public void setMyTurn() {
        gameSceneController.startMyTurn();
    }

    @Override
    public boolean isHost() {
        return isHost;
    }

    @Override
    public ArrayList<String> getCurrentLobbyMembers() {
        return currentLobbyMembers;
    }

    @Override
    public String getCurrentLobbyPassword() {
        return password;
    }
}

interface IDaifugoApp {
    /**
     * アカウントの名前をセットする
     * @param name アカウントの名前
     */
    void setName(String name);

    /**
     * アカウントの名前を取得する
     * @return アカウントの名前
     */
    String getName();

    /**
     * 現在のロビーの名前をセットする
     * @param name 現在のロビーの名前
     */
    void setLobbyName(String name);

    /**
     * 現在のロビーの名前を取得する
     * @return 現在のロビーの名前
     */
    String getLobbyName();

    /** スタート画面の表示 
     * @throws MalformedURLException*/
    void showStartScene() throws MalformedURLException;

    /** ロビー作成/入室 選択画面の表示 */
    void showSelectHostOrJoinScene();

    /** ロビー作成画面の表示 */
    void showCreateLobbyScene();

    /** ロビー入室画面の表示 */
    void showJoinLobbyScene();

    /** ホストとしてのロビー画面の表示 */
    void showHostLobbyScene(String password, ArrayList<String> members);

    /** ゲストとしてのロビー画面の表示 */
    void showGuestLobbyScene(ArrayList<String> members);

    /** ロビー画面にメンバーを追加する */
    void addLobbyMember(String name);

    /** ロビー画面からメンバーを削除する */
    void removeLobbyMember(String name);

    /** ロビー画面のメンバーを準備状態にする */
    void readyLobbyMember(String name);

    /** ロビー画面のメンバーを非準備状態にする */
    void unreadyLobbyMember(String name);

    /** ホストを変更する */
    void changeHost(String name);

    void addChatText(String sender, String content);

    void loadGameScene(int currentGameCount, int maxGameCount) throws IOException, InterruptedException;

    void setPlayerTurn(String content) throws InterruptedException;

    void setHand(String content) throws IOException;

    void setStage(String content);

    void setMyTurn();

    void showGameScene() throws Exception;

    boolean isHost();

    ArrayList<String> getCurrentLobbyMembers();

    String getCurrentLobbyPassword();


}
