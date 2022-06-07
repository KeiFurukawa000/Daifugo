

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class CenterStage{
    int num;//場に出ているカードの数字
    int mode = 0;
    Group field;
    CenterStage(){
        setup();
    }
    void setup(){
        num = 0;
        field = new Group();
        Rectangle rect = new Rectangle(120,120);
        rect.setFill(Color.LIGHTGREEN);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(1);
        field.getChildren().add(rect);
    }
    void initCenterStage(){
        this.field.getChildren().clear();
        setup();
    }
    void setCenterStageNum(int num){
        this.num = num;
    }
}
