import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class GameOptionsPopupController {
    @FXML
    private Button acceptButton;
    @FXML
    private Button gameCountButton_1;
    @FXML
    private Button gameCountButton_3;
    @FXML
    private Button gameCountButton_5;
    @FXML
    private Button gameCountButton_10;
    @FXML
    private Text gameCountText;

    @FXML
    void onPressedAcceptButton(ActionEvent event) {

    }

    private void setButtonEnable(boolean b1, boolean b2, boolean b3, boolean b4) {
        gameCountButton_1.setDisable(b1);
        gameCountButton_3.setDisable(b2);
        gameCountButton_5.setDisable(b3);
        gameCountButton_10.setDisable(b4);
    }

    @FXML
    void onPressedGameCountButton_1(ActionEvent event) {
        gameCountText.setText("1");
        setButtonEnable(true, false, false, false);
    }

    @FXML
    void onPressedGameCountButton_3(ActionEvent event) {
        gameCountText.setText("3");
        setButtonEnable(false, true, false, false);
    }

    @FXML
    void onPressedGameCountButton_5(ActionEvent event) {
        gameCountText.setText("5");
        setButtonEnable(false, false, true, false);
    }

    @FXML
    void onPressedGameCountButton_10(ActionEvent event) {
        gameCountText.setText("10");
        setButtonEnable(false, false, false, true);
    }
}

