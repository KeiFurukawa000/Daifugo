

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

class DispPlayer extends Group{
    Circle circ = new Circle(30);//icon
    Label lbl = new Label();

    DispPlayer(String name){
        circ.setFill(Color.WHEAT);
        lbl.setText(name);
        lbl.setTextFill(Color.FLORALWHITE);
        lbl.setFont(new Font(40));
        this.getChildren().addAll(circ,lbl);
        this.lbl.setLayoutX(this.lbl.getWidth()/2);
        this.lbl.setLayoutY(0);
        this.circ.setCenterX(0);
        this.circ.setCenterY(60);
    }

    void playerturn(){
        circ.setFill(Color.RED);
    }
    void exitplayerturn(){
        circ.setFill(Color.WHEAT);
    }
}