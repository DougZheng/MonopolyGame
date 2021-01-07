package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class GameRect extends GameView {

    private RectF rectF;
    private float roundX, roundY;
    private int color;

    public GameRect(float x, float y, float w, float h,
                    float roundX, float roundY, int color){
        super(x, y, w, h);
        this.roundX = roundX;
        this.roundY = roundY;
        this.color = color;
        rectF = new RectF(x, y, x + w, y + h);
    }

    public GameRect(RectF rectF, float roundX, float roundY, int color){
        super(rectF);
        this.roundX = roundX;
        this.roundY = roundY;
        this.color = color;
        this.rectF = new RectF(rectF);
    }

    @Override
    public void draw(Canvas canvas, Paint paint){
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundX, roundY, paint);
    }

    public int getColor(){
        return color;
    }

    public float getRoundX(){
        return roundX;
    }

    public float getRoundY(){
        return roundY;
    }

    @Override
    public RectF getRectF(){
        return rectF;
    }

    public void setColor(int c){
        color = c;
    }

    @Override
    public void setPosX(float x){
        super.setPosX(x);
        rectF.offset(x - rectF.left, 0);
    }

    @Override
    public void setPosY(float y){
        super.setPosY(y);
        rectF.offset(0, y - rectF.top);
    }
}
