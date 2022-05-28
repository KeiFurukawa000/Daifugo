import java.io.IOException;

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
     * JavaFX Applicationの起動を行います
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("大富豪");

        String addr = getParameters().getRaw().get(0);
        int port = Integer.parseInt(getParameters().getRaw().get(1));
        connection = new ClientConnection(addr, port, this);
        thread = new Thread(new ClientListen(connection.getSocket(), this));
        thread.start();
        this.stage = stage;
        ShowStartScene();
    }

    @Override
    public void SetName(String name) {
        this.name = name;
    }

    @Override
    public String GetName() {
        return name;
    }

    @Override
    public void ShowStartScene() {
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
    public void ShowSelectHostOrJoinScene() {
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
    public void ShowCreateLobbyScene() {
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
    public void ShowJoinLobbyScene() {
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
    public void ShowHostLobbyScene(String password, String[] members) {
        guestLobbyScenecontroller = null;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HostLobbyScene.fxml"));
        Parent root;
        try {
            root = (Parent)fxmlLoader.load();
            hostLobbyScenecontroller = (HostLobbySceneController)fxmlLoader.getController();
            hostLobbyScenecontroller.SetCallback(this, connection, password, members);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {}
    }

    @Override
    public void ShowGuestLobbyScene(String[] members) {
        hostLobbyScenecontroller = null;
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
    public void SetLobbyName(String name) {
        lobbyName = name;
    }

    @Override
    public String GetLobbyName() {
        return lobbyName;
    }

    @Override
    public void AddLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onJoinGuest(name);
        else guestLobbyScenecontroller.onJoinGuest(name);
    }

    @Override
    public void RemoveLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onLeaveGuest(name);
        else guestLobbyScenecontroller.onLeaveGuest(name);
    }

    @Override
    public void ReadyLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onReadyGuest(name);
        else guestLobbyScenecontroller.onReadyGuest(name);
    }

    @Override
    public void UnreadyLobbyMember(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onUnreadyGuest(name);
        else guestLobbyScenecontroller.onUnreadyGuest(name);
    }

    @Override
    public void ChangeHost(String name) {
        if (hostLobbyScenecontroller != null) hostLobbyScenecontroller.onChangeHost(name); 
        else guestLobbyScenecontroller.onChangeHost(name);
    }
}

interface IDaifugoApp {
    void SetName(String name);
    String GetName();
    void SetLobbyName(String name);
    String GetLobbyName();
    void ShowStartScene();
    void ShowSelectHostOrJoinScene();
    void ShowCreateLobbyScene();
    void ShowJoinLobbyScene();
    void ShowHostLobbyScene(String password, String[] members);
    void ShowGuestLobbyScene(String[] members);
    void AddLobbyMember(String name);
    void RemoveLobbyMember(String name);
    void ReadyLobbyMember(String name);
    void UnreadyLobbyMember(String name);
    void ChangeHost(String name);
}
