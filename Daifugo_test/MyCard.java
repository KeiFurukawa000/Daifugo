

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class MyCard extends DisplayCard{
    boolean selected = false;
    boolean removed = false;
    boolean possible = false;
    MyCard(String suit, int num){
        this.suit = suit;
        this.num = num;
        Rectangle rect = new Rectangle(90,140);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setArcHeight(20);
        rect.setArcWidth(20);

        String s0 = String.format("%d", num);
        String s1 = this.suit;
        Label lbl = new Label();
        Label lbl2 = new Label();
        Label lbl3 = new Label();
        lbl.setText(s0);
        lbl2.setText(s0);
        lbl3.setText(s1);

        this.rectgp.getChildren().addAll(rect,lbl,lbl2,lbl3);
        lbl.setLayoutX(5);
        lbl.setLayoutX(10);
        lbl2.setLayoutX(70);
        lbl2.setLayoutY(125);
        lbl2.setRotate(180);
        lbl3.setLayoutX(5);
        lbl3.setLayoutY(20);
        this.rectgp.setOnMouseEntered(e -> mouseOnCard());
        this.rectgp.setOnMouseExited(e -> mouseOffCard());
    }
    void mouseOnCard(){
        if(this.selected == false)
            this.rectgp.setLayoutY(this.rectgp.getLayoutY()-10);
    }
    void mouseOffCard(){
        if(this.selected == false)
            this.rectgp.setLayoutY(this.rectgp.getLayoutY()+10);
    }
    void transParentMyCard(){
        this.rectgp.setMouseTransparent(true);
    }
    void transParentMyCardExit(){
        this.rectgp.setMouseTransparent(false);
    }
}
