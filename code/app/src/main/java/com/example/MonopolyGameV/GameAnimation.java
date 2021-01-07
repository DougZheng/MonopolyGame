package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.Paint;

/*
    负责基本动画，包括固定坐标循环动画以及静态画面
 */

public class GameAnimation {

    private int totFrame;
    private int frameTime;
    private int elapsedTime;
    private int durationTime;
    private long lastTime = -1;
    private GameView[] activeFrame;
    private GameView[] staticFrame;

    public GameAnimation(int totFrame, int frameTime, int durationTime,
                         GameView[] activeFrame, GameView[] staticFrame){
        this.totFrame = totFrame;
        this.frameTime = frameTime;
        this.durationTime = durationTime;
        this.activeFrame = activeFrame;
        this.staticFrame = staticFrame;
    }

    // 启用动画
    public void start(){
        lastTime = 0;
        elapsedTime = 0;
    }

    // 停止动画
    public void end(){
        elapsedTime = durationTime;
    }

    // frame: 动画静止时的静态帧
    public void draw(Canvas canvas, Paint paint, int frame){
        long curTime = android.os.SystemClock.uptimeMillis();
        if(lastTime == 0) lastTime = curTime;
        elapsedTime += curTime - lastTime;
        if(!isAlive()){ // 绘制静态画面
            staticFrame[frame].draw(canvas, paint);
            lastTime = -1;
        }
        else{ // 绘制动画
            frame = getCurFrame();
            activeFrame[frame].draw(canvas, paint);
            lastTime = curTime;
        }
    }

    public int getCurFrame(){
        return elapsedTime / frameTime % totFrame;
    }

    public boolean isAlive(){
        return elapsedTime >= 0 && elapsedTime < durationTime;
    }
}