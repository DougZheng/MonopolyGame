package com.example.MonopolyGameMC;

import java.io.Serializable;
import java.util.Random;

/*
    游戏骰子，随机产生点数
 */

public class Dice implements Serializable {

    private final int totValue = 6;
    private int faceValue = 1;
    private Random random;

    public Dice(long seed){
        random = new Random(android.os.SystemClock.uptimeMillis() + seed);
    }

    public void setFaceValue(int faceValue){
        this.faceValue = faceValue;
    }

    public int getFaceValue(){
        return faceValue;
    }

    public void rollDice(){
        setFaceValue(random.nextInt(totValue) + 1);
    }
}
