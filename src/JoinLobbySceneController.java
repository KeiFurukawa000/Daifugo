import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class JoinLobbySceneController {

    @FXML
    private Button backButton;

    @FXML
    private Button joinButton;

    @FXML
    private TextField lobbyNameTextField;

    @FXML
    private TextField passwordTextFiels;

    private IDaifugoApp app;
    private ILobbyConnectable connection;

    public void Init(IDaifugoApp app, ILobbyConnectable conneciton) {
        this.app = app;
        this.connection = conneciton;
    }

    @FXML
    void onPressedBackButton(ActionEvent event) {
        app.ShowSelectHostOrJoinScene();
    }

    @FXML
    void onPressedJoinButton(ActionEvent event) {
        connection.RequestJoinLobby(lobbyNameTextField.getText(), passwordTextFiels.getText(), app.GetName());
        app.SetLobbyName(lobbyNameTextField.getText());
    }

}

