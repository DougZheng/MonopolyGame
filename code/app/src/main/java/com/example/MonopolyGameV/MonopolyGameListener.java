package com.example.MonopolyGameV;

import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import com.example.MonopolyGameMC.MonopolyGameLogic;

/*
    负责监听屏幕动作
 */

public class MonopolyGameListener implements View.OnTouchListener {

    private static final String TAG = "MonopolyGameListener"; // for Log.d

    private final MonopolyGameView monopolyGameView;
    private final MonopolyGameLogic monopolyGameLogic;
    private float touchX, touchY;
    Pair<MonopolyGameView.ViewType, Integer> viewType; // <图形类型，附加编号>

    public MonopolyGameListener(MonopolyGameView monopolyGameView, MonopolyGameLogic monopolyGameLogic){
        this.monopolyGameView = monopolyGameView;
        this.monopolyGameLogic = monopolyGameLogic;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "action down");
                touchX = event.getX();
                touchY = event.getY();
                viewType = monopolyGameView.getViewType(touchX, touchY);
                // 点击骰子按钮 && 当前轮到玩家移动 && 处于准备掷骰子阶段
                if(viewType.first == MonopolyGameView.ViewType.BUTTON_DICE
                        && monopolyGameLogic.getCurPlayer() == 0
                        && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.ENROLL){
                    monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.ROLL); // 进入掷骰子阶段
                }
                // 点击确认按钮
                else if(viewType.first == MonopolyGameView.ViewType.BUTTON_Y
                        && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YON){
                    monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_Y);
                }
                else if(viewType.first == MonopolyGameView.ViewType.BUTTON_Y
                        && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YOY
                        && monopolyGameView.getSquareSelect() != -1){
                    monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_Y);
                }
                // 点击使用道具卡按钮
                else if(viewType.first == MonopolyGameView.ViewType.BUTTON_Y
                        && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.ENROLL
                        && monopolyGameView.getCardSelect() < 3){
                    monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_Y);
                }
                // 点击取消按钮
                else if(viewType.first == MonopolyGameView.ViewType.BUTTON_N
                        && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YON){
                    monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_N);
                }
                // 点击返回菜单按钮
                else if(viewType.first == MonopolyGameView.ViewType.BUTTON_BACK){
                    MonopolyGameActivity.backToMenu();
                }
                // 点击方格，显示方格信息
                else if(viewType.first == MonopolyGameView.ViewType.SQUARE){
                    monopolyGameView.setCardSelect(-1);
                    if(monopolyGameView.getSquareSelect() == viewType.second){
                        monopolyGameView.setSquareSelect(-1);
                    }
                    else{
                        monopolyGameView.setSquareSelect(viewType.second);
                    }
                    monopolyGameView.invalidate();
                }
                // 点击玩家头像
                else if(viewType.first == MonopolyGameView.ViewType.PLAYER){
                    if(monopolyGameView.getPlayerSelect() == viewType.second){
                        monopolyGameView.setPlayerSelect(-1);
                    }
                    else{
                        monopolyGameView.setPlayerSelect(viewType.second);
                    }
                    monopolyGameView.invalidate();
                }
                // 点击道具卡
                else if(viewType.first == MonopolyGameView.ViewType.PROP_CARD){
                    monopolyGameView.setSquareSelect(-1);
                    if(monopolyGameView.getCardSelect() == viewType.second){
                        monopolyGameView.setCardSelect(-1);
                    }
                    else{
                        monopolyGameView.setCardSelect(viewType.second);
                    }
                    monopolyGameView.invalidate();
                }
                // TODO: 更多点击事件
                break;
            default: break;
        }
        return true;
    }
}
