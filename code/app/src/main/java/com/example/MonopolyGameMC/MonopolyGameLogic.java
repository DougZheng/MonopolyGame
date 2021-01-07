package com.example.MonopolyGameMC;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.example.MonopolyGameV.MonopolyGameActivity;
import com.example.MonopolyGameV.MonopolyGameView;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/*
    负责游戏逻辑的管理
 */

public class MonopolyGameLogic extends Thread {

    private final String TAG = "MonopolyGameLogic"; // for Log.d

    // 游戏状态：其他，掷骰子阶段，移动阶段
    public enum GameState{
        DEFAULT, ENROLL, ROLL, MOVE, CHOOSE_YON, CHOOSE_YOY, CHOOSE_Y, CHOOSE_N, USE_PROP_CARD
    }

    private MonopolyGameView monopolyGameView;
    private Handler handler;
    private boolean isRunning = true; // TODO: 游戏暂停
    private boolean isPaused = false;
    private GameState gameState = GameState.DEFAULT;

    private final int playerNum = 4;
    private final int totCardNum = 5;
    private Dice[] dice;
    private Player[] players;
    private Board board;
    private PropCard[] cards;

    private File file = MonopolyGameActivity.getFile();

    private final int maxRoundCnt = 40;
    private int roundCnt = 0; // TODO: 回合数
    private int curPlayer = 0; // 当前行动玩家

    public MonopolyGameLogic(MonopolyGameView monopolyGameView, Handler handler){
        this.monopolyGameView = monopolyGameView;
        this.handler = handler;
        board = new Board(monopolyGameView.squareUNum + 2, monopolyGameView.squareLNum + 2);
        board.setController(this);
        dice = new Dice[2];
        dice[0] = new Dice(20200601);
        dice[1] = new Dice(20200607);
        players = new Player[playerNum];
        for(int i = 0; i < playerNum; ++i){
            players[i] = new Player(i, board, dice);
            players[i].setController(this);
        }
        players[0].setAI(false);
        cards = new PropCard[totCardNum];
        cards[0] = new TranspositionCard();
        cards[1] = new TurtleCard();
        cards[2] = new StayCard();
        cards[3] = new BuildingCard();
        cards[4] = new FreeCard();
        for(int i = 0; i < totCardNum; ++i){
            cards[i].setController(this);
        }
    }

    // 游戏保存，对象序列化
    private void save() throws IOException {
        Log.d("zbs", "save");
        Log.d("zbs", file.getAbsolutePath());
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        // gameLogic
        out.writeObject(gameState);
        out.writeInt(roundCnt);
        out.writeInt(curPlayer);
        // board
        out.writeObject(board);
        // dice
        out.writeObject(dice[0]);
        out.writeObject(dice[1]);
        // player
        for(int i = 0; i < playerNum; ++i){
            out.writeObject(players[i]);
        }
        // cards
        for(int i = 0; i < totCardNum; ++i){
            out.writeObject(cards[i]);
        }
        // eventMsg
        out.writeObject(monopolyGameView.getMsgList());
        out.close();
    }

    // 游戏存档加载
    private void load() throws IOException, ClassNotFoundException {
        Log.d("zbs", "load");
        Log.d("zbs", file.getAbsolutePath());
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        // gameLogic
        gameState = (GameState)in.readObject();
        roundCnt = in.readInt();
        curPlayer = in.readInt();
        // board
        board = (Board)in.readObject();
        board.setController(this);
        // dice
        dice[0] = (Dice)in.readObject();
        dice[1] = (Dice)in.readObject();
        // player
        for(int i = 0; i < playerNum; ++i){
            players[i] = (Player)in.readObject();
            players[i].setController(this);
            flyToNewPos(i, players[i].getPos());
        }
        // cards
        for(int i = 0; i < totCardNum; ++i){
            cards[i] = (PropCard)in.readObject();
            cards[i].setController(this);
        }
        Object msgList = in.readObject();
        sendMsgList(msgList);
        in.close();
        requestDraw();
    }

    public void pause(){
        isPaused = true;
    }

    public void unPause(){
        isPaused = false;
    }

    public void setRunning(boolean b){
        isRunning = b;
    }

    // 请求重绘
    private void requestDraw(){
        monopolyGameView.postInvalidate();
    }

    public void enRoll(){
        setGameState(GameState.ENROLL);
        monopolyGameView.buttonDiceAnimation.start();
        requestDraw();
    }

    public void startRoll(){
        setGameState(GameState.ROLL);
        monopolyGameView.diceAnimation.start();
        monopolyGameView.diceSmallAnimation.start();
        monopolyGameView.buttonDiceAnimation.end();
        requestDraw();
    }

    public void startMove(){
        setGameState(GameState.MOVE);
        Message msg = handler.obtainMessage(1001, curPlayer, players[curPlayer].getPos(), board.getSquareNum());
        handler.sendMessage(msg);
    }

    public void addEventMsg(String event){
        Message msg = handler.obtainMessage(1002, event);
        handler.sendMessage(msg);
    }

    public void flyToNewPos(int id, int pos){
        Message msg = handler.obtainMessage(1003, id, pos);
        handler.sendMessage(msg);
    }

    public void sendMsgList(Object msgList){
        Message msg = handler.obtainMessage(1004, msgList);
        handler.sendMessage(msg);
    }

    public void sendTip(String tip){
        Message msg = handler.obtainMessage(1005, tip);
        handler.sendMessage(msg);
    }

    public void acquiredCard(int playerID){
        Random r = new Random();
        int id = r.nextInt(totCardNum);
        players[playerID].addCard(cards[id]);
    }

    private void distributeCard(int num){
        for(int i = 0; i < playerNum; ++i){
            Random r = new Random();
            for(int j = 0; j < num; ++j){
                int id = r.nextInt(totCardNum);
                players[i].addCard(cards[id]);
            }
        }
    }

    private boolean isGameOver(){
        int resPlayer = 0;
        for(int i = 0; i < playerNum; ++i){
            if(!players[i].isOut()) ++resPlayer;
        }
        if(resPlayer == 1) return true;
        return false;
    }

    private void displayResult(){
        addEventMsg("游戏结束");
        int winner = 0;
        for(int i = 1; i < playerNum; ++i){
            if(players[i].getMoney() > players[winner].getMoney()){
                winner = i;
            }
        }
        addEventMsg(players[winner].getName() + "获得本局胜利");
        addEventMsg("返回主菜单开始新一轮游戏");
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        if(file.isFile()){ // 有存档，加载存档
            Log.d("zbs", "file length: " + file.length() + " bytes");
            try{
                load();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        else{
            distributeCard(3); // 开局每人三张道具卡
        }
        ALL:
        do{
            for(int i = curPlayer; i < playerNum; ++i){
                if(!isRunning) break;
                while(isPaused);
                try{
                    save();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                if(!players[curPlayer].isOut()){
                    players[curPlayer].takeTurn();
                }
                setGameState(GameState.DEFAULT);
                curPlayer = getNxtPlayer(); // 下一玩家行动
                if(curPlayer == 0){
                    if(roundCnt + 1 <= maxRoundCnt && (roundCnt + 1) % 5 == 0){
                        addEventMsg("下一回合初每人将得到一张道具卡");
                    }
                    if(roundCnt > 0 && roundCnt % 5 == 0){
                        distributeCard(1);
                    }
                    ++roundCnt;
                    addEventMsg("第" + roundCnt + "回合结束");
                }
                requestDraw();
                if(isGameOver()){
                    break ALL; // 游戏结束
                }
            }
        }
        while(roundCnt < maxRoundCnt);
        displayResult();
    }

    public synchronized void setGameState(GameState gameState){
        this.gameState = gameState;
    }

    public synchronized GameState getGameState(){
        return gameState;
    }

    public int getCurPlayer(){
        return curPlayer;
    }

    private int getNxtPlayer(){
        return (curPlayer + 1) % playerNum;
    }

    public Dice getDice(int id){
        return dice[id];
    }

    public Player getPlayer(int id){
        return players[id];
    }

    public ArrayList<PropCard> getCardList(int id){
        return players[id].getCardList();
    }

    public PropCard getCard(int id){
        ArrayList<PropCard> cardList = getCardList(id / 3);
        id %= 3;
        return id < cardList.size() ? cardList.get(id) : null;
    }

    public Board getBoard(){
        return board;
    }

    public int getSquareSelect(){
        return monopolyGameView.getSquareSelect();
    }

    public int getCardSelect(){
        return monopolyGameView.getCardSelect();
    }

    public Player getPlayerSelect(){
        int id = monopolyGameView.getPlayerSelect();
        if(id == -1) return null;
        else return players[id];
    }
}
