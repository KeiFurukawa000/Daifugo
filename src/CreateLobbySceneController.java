import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateLobbySceneController {

    @FXML
    private Button backButton;

    @FXML
    private Button createButton;

    @FXML
    private TextField lobbyNameTextField;

    private IDaifugoApp app;
    private ILobbyConnectable connection;

    public void SetCallback(IDaifugoApp app, ILobbyConnectable connection) {
        this.app = app;
        this.connection = connection;
    }

    @FXML
    void onPressedBackButton(ActionEvent event) {
        app.showSelectHostOrJoinScene();
    }

    @FXML
    void onPressedCreateButton(ActionEvent event) {
        connection.RequestCreateLobby(lobbyNameTextField.getText(), app.getName());
        app.setLobbyName(lobbyNameTextField.getText());
    }

}

