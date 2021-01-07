package com.example.MonopolyGameV;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;

public class EventMsgManager {

    private final int maxNum = 6;
    private ArrayList<String> msgList = new ArrayList<>();
    private float[] msgX = new float[maxNum];
    private float[] msgY = new float[maxNum];
    private float msgSize;

    public EventMsgManager(float x, float y, float msgSize){
        msgX[0] = x;
        msgY[0] = y;
        for(int i = 1; i < maxNum; ++i){
            msgX[i] = x;
            msgY[i] = msgY[i - 1] + msgSize;
        }
        this.msgSize = msgSize * 0.6f;
    }

    public void addMsg(String msg){
        synchronized (msgList){
            if (msgList.size() >= maxNum) {
                msgList.remove(0);
            }
            msgList.add(msg);
        }
    }

    public void drawMsg(Canvas canvas, Paint paint){
        paint.setTextSize(msgSize);
        for(int i = 0; i < msgList.size(); ++i){
            canvas.drawText("- " + msgList.get(i), msgX[i], msgY[i], paint);
        }
    }

    public ArrayList<String> getMsgList(){
        return (ArrayList<String>)msgList.clone();
    }

    public void setMsgList(ArrayList<String> mList) {
        msgList = mList;
    }
}
