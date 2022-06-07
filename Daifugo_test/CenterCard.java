

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

class CenterCard extends DisplayCard{
    CenterCard(String suit,int num){
        this.suit = suit;
        this.num = num;
        Rectangle rect = new Rectangle(45,70);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setArcHeight(10);
        rect.setArcWidth(10);

        String s0 = String.format("%d", this.num);
        String s1 = this.suit;
        Label lbl = new Label();
        Label lbl2 = new Label();
        Label lbl3 = new Label();
        lbl.setFont(new Font(6));
        lbl2.setFont(new Font(6));
        lbl3.setFont(new Font(6));
        lbl.setText(s0);
        lbl2.setText(s0);
        lbl3.setText(s1);

        this.rectgp.getChildren().addAll(rect,lbl,lbl2,lbl3);
        lbl.setLayoutX(5);
        lbl.setLayoutX(10);
        lbl2.setLayoutX(35);
        lbl2.setLayoutY(60);
        lbl2.setRotate(180);
        lbl3.setLayoutX(5);
        lbl3.setLayoutY(20);
    }
    
}