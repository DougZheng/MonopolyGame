package com.example.MonopolyGameV;

import android.graphics.*;

public class GameImage extends GameView {

    Bitmap bitmap;
    int resId;

    public GameImage(float x, float y, float w, float h, int resId){
        super(x, y, w, h);
        this.resId = resId;
        loadBitmap();
        bitmap = adjustBitmap(bitmap, (int)w, (int)h);
    }

    public GameImage(RectF rectF, int resId){
        super(rectF);
        this.resId = resId;
        loadBitmap();
        bitmap = adjustBitmap(bitmap, (int)rectF.width(), (int)rectF.height());
    }

    public void loadBitmap(){
        bitmap = BitmapFactory.decodeResource(MonopolyGameActivity.getMonopolyGameActivity().getResources(), resId);
    }

    public static Bitmap adjustBitmap(Bitmap bitmap, int w, int h){
        Matrix matrix = new Matrix();
        matrix.postScale((float)w / bitmap.getWidth(), (float)h / bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, getPosX(), getPosY(), paint);
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }
}
