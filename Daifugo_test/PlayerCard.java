
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class PlayerCard extends Card{
    PlayerCard(){
        Rectangle rect = new Rectangle(30,42);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setArcHeight(7);
        rect.setArcWidth(7);
        this.rectgp.getChildren().add(rect);
    }
}