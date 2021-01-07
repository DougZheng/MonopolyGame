package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    游戏计时器
 */

public class GameTimer implements Serializable {

    private long startTime = 0;
    private long resTime = 0;

    public boolean isTimeOut(){
        long curTime = android.os.SystemClock.uptimeMillis();
        return curTime - startTime > resTime;
    }

    public void setResTime(long time){
        startTime = android.os.SystemClock.uptimeMillis();
        resTime = time;
    }

    public void addResTime(long time){
        resTime += time;
    }
}
