

import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GuestLobbySceneController {

    @FXML
    private TextField chatInputField;

    @FXML
    private Button chatSendButton;

    @FXML
    private TextArea chatTextField;

    @FXML
    private Button leaveButton;

    @FXML
    private Button optionsButton;

    @FXML
    private ListView<Group> listview;

    @FXML
    private Button readyButton;

    private boolean isPressedReady;

    private IDaifugoApp app;
    private ILobbyConnectable connection;
    private HashMap<String, Group> playerMap;

    public void Init(IDaifugoApp app, ILobbyConnectable connection, String[] members) {
        playerMap = new HashMap<>();

        this.app = app;
        this.connection = connection;

        for (int i = 0; i < members.length; i++) {
            String[] args = members[i].split(",");
            String name = args[0];
            String state = args[1];
            Group group = GetPlayerBox(name, state);
            listview.getItems().add(group);
            playerMap.put(name, group);
        }
    }

    private Group GetPlayerBox(String name, String state) {
        Group group = new Group();
        group.prefWidth(223);
        group.prefHeight(16);
        Text playerName = new Text(name);
        playerName.setFont(new Font("Family", 23));
        playerName.setWrappingWidth(100);
        ImageView readyImage = new ImageView(new Image((state.equals("HOST") ? "/img/host.png" : "/img/check.png")));
        readyImage.setFitWidth(50);
        readyImage.setFitHeight(50);
        readyImage.setPreserveRatio(true);
        readyImage.setSmooth(true);
        readyImage.setVisible((state.equals("HOST") ? true : (state.equals("READY") ? true : false)));
        HBox hbox = new HBox(playerName, readyImage);
        hbox.setAlignment(Pos.CENTER);
        group.getChildren().add(hbox);
        return group;
    }

    public void addTextInChat(String text) {
        chatTextField.appendText(text + System.lineSeparator());
    }

    public void onJoinGuest(String name) {
        Group group = GetPlayerBox(name, "UNREADY");
        listview.getItems().add(group);
        playerMap.put(name, group);
        addTextInChat(name + " さんが入室しました");
    }

    public void onLeaveGuest(String name) {
        Group group = playerMap.get(name);
        listview.getItems().remove(group);
        playerMap.remove(name);
        addTextInChat(name + " さんが退室しました");
    }

    public void onReadyGuest(String name) {
        Group getter = playerMap.get(name);
        Group group = listview.getItems().get(listview.getItems().indexOf(getter));
        HBox hbox = (HBox) group.getChildren().get(0);
        ImageView imageView = (ImageView)hbox.getChildren().get(1);
        imageView.setVisible(true);
    }

    public void onUnreadyGuest(String name) {
        Group getter = playerMap.get(name);
        Group group = listview.getItems().get(listview.getItems().indexOf(getter));
        HBox hbox = (HBox) group.getChildren().get(0);
        ImageView imageView = (ImageView)hbox.getChildren().get(1);
        imageView.setVisible(false);
    }

    public void onChangeHost(String name) {
        Group getter = playerMap.get(name);
        int index = listview.getItems().indexOf(getter);
        Group group = listview.getItems().get(index);
        HBox hbox = (HBox) group.getChildren().get(0);

        Text playerName = (Text)hbox.getChildren().get(0);
        System.out.println(playerName.getText());
        Group preGroup = listview.getItems().get(index+1);
        HBox preBox = (HBox)preGroup.getChildren().get(0);
        Text preName = (Text)preBox.getChildren().get(0);
        System.out.println(preName.getText());
        playerName.setText(preName.getText());
        System.out.println(playerName.getText());

        ImageView imageView = (ImageView)hbox.getChildren().get(1);
        imageView.setImage(new Image("/img/host.png"));
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        listview.getItems().remove(group);
        listview.getItems().set(0, group);

        addTextInChat(preName.getText() + " さんがホストになりました");
    }

    @FXML
    void onPressedChatSendButton(ActionEvent event) {
        connection.SendChat(app.getLobbyName(), app.getName(), chatInputField.getText());
        chatInputField.clear();
    }

    @FXML
    void onPressedLeaveButton(ActionEvent event) {
        connection.RequestLeaveLobby(app.getLobbyName(), app.getName());
        app.showSelectHostOrJoinScene();
    }

    @FXML
    void onPressedOptionsButton(ActionEvent event) {

    }

    @FXML
    void onPressedReadyButton(ActionEvent event) {
        if (!isPressedReady) { 
            connection.RequestReady(app.getLobbyName(), app.getName());
            isPressedReady = true;
        }
        else {
            connection.RequestUnready(app.getLobbyName(), app.getName());
            isPressedReady = false;
        }
    }

}

