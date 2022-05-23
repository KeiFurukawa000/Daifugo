import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SelectHostOrJoinSceneController {

    @FXML
    private Button createLobbyButton;

    @FXML
    private Button joinLobbyButton;

    @FXML
    private Button backButton;

    private IDaifugoApp callback;

    public void SetCallback(IDaifugoApp callback) {
        this.callback = callback;
    }

    @FXML
    void onPressedBackButton(ActionEvent event) {
        callback.ShowStartScene();
    }

    @FXML
    void onPressedCreateLobbyButton(ActionEvent event) {
        callback.ShowCreateLobbyScene();
    }

    @FXML
    void onPressedJoinLobbyButton(ActionEvent event) {
        callback.ShowJoinLobbyScene();
    }

}
