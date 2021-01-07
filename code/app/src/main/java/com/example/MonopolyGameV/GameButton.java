package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class GameButton extends GameRect {

    private String buttonText;
    private int color;

    public GameButton(float x, float y, float w, float h, float rx, float ry,
                      int rc, String buttonText, int color){
        super(x, y, w, h, rx, ry, rc);
        this.buttonText = buttonText;
        this.color = color;
    }

    public GameButton(RectF rectF, float rx, float ry,
                      int rc, String buttonText, int color){
        super(rectF, rx, ry, rc);
        this.buttonText = buttonText;
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas, Paint paint){
        super.draw(canvas, paint);
        paint.setColor(color);
        paint.setTextSize(getWidth());
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.min(getWidth() * getWidth() * 0.66f / paint.measureText(buttonText),
                getHeight() * 0.5f));
        Rect r = new Rect();
        paint.getTextBounds(buttonText, 0, 1, r);
        canvas.drawText(buttonText, getRectF().centerX(),
                getPosY() + getHeight() * 0.5f + r.height() * 0.5f, paint);
    }

    public String getButtonText(){
        return buttonText;
    }
}