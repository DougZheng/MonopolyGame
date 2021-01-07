package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class GameView {

    private float posX, posY;
    private float width, height;

    public GameView(float posX, float posY, float width, float height){
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public GameView(RectF rectF){
        this.posX = rectF.left;
        this.posY = rectF.top;
        this.width = rectF.width();
        this.height = rectF.height();
    }

    public boolean isLocated(float x, float y){
        return x >= posX && x <= posX + width && y >= posY && y <= posY + height;
    }

    public void draw(Canvas canvas, Paint paint){

    }

    public float getPosX(){
        return posX;
    }

    public float getPosY(){
        return posY;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public void setPosX(float x){
        posX = x;
    }

    public void setPosY(float y){
        posY = y;
    }

    public RectF getRectF(){
        return new RectF(posX, posY, posX + width, posY + height);
    }
}
