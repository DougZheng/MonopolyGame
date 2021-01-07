package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.RectF;
import java.util.Stack;

/*
    特别负责玩家静止画面以及移动动画
 */

public class PlayerAnimation {

    private GameImage playerImage; // 居中正常显示
    private GameImage playerSmallImage; // 作为角标显示
    private int frameTime = 220;
    private long lastTime = 0;
    private int endPos = 0; // 移动目的地
    private Stack<Integer> posStack; // 移动路线，栈顶为当前移动位置

    public PlayerAnimation(GameImage playerImage, GameImage playerSmallImage){
        this.playerImage = playerImage;
        this.playerSmallImage = playerSmallImage;
        posStack = new Stack<>();
        posStack.push(0); // 初始位置为起点处
    }

    public int getNextPos(){
        return posStack.peek();
    }

    public int getEndPos(){
        return endPos;
    }

    public void setPos(int pos){
        posStack.clear();
        posStack.add(pos);
    }

    public void start(int newPos, int squareNum){
        endPos = newPos;
        int oldPos = posStack.pop();
        while(newPos != oldPos){ // 逆序压入移动路线
            posStack.push(newPos);
            newPos = (newPos - 1 + squareNum) % squareNum;
        }
        posStack.push(oldPos);
    }

    // rect: 所在格子的参数
    // pri: 优先级，0: 大图，1~3: 角标
    public boolean draw(Canvas canvas, RectF rect, int pri){
        long curTime = android.os.SystemClock.uptimeMillis();
        float x = rect.centerX() - playerImage.getWidth() * 0.5f;
        float y = rect.centerY() - playerImage.getHeight() * 0.5f;
        if(pri == 0){
            playerImage.setPosX(x);
            playerImage.setPosY(y);
            playerImage.draw(canvas, null);
        }
        else{
            x = rect.left + (x - rect.left - playerSmallImage.getWidth()) * 0.5f;
            y = rect.top + rect.height() * 0.25f * pri - playerSmallImage.getHeight() * 0.5f;
            playerSmallImage.setPosX(x);
            playerSmallImage.setPosY(y);
            playerSmallImage.draw(canvas, null);
        }
        if(posStack.size() == 1){
            lastTime = 0;
            return false; // 移动结束，无需重绘
        }
        if(lastTime == 0 || curTime - lastTime >= frameTime){
            posStack.pop();
            lastTime = curTime;
        }
        return true;
    }
}
