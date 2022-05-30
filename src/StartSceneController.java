import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class StartSceneController {

    @FXML
    private TextField playerNameTextField;

    @FXML
    private Button startButton;

    private IServerConnectable connection;
    private IDaifugoApp app;

    public void SetCallback(IServerConnectable connection, IDaifugoApp app) {
        this.connection = connection;
        this.app = app;
    }

    @FXML
    void onPressedStartButton(ActionEvent event) {
        connection.RequestCreateAccount(playerNameTextField.getText());
        app.setName(playerNameTextField.getText());
    }
}

