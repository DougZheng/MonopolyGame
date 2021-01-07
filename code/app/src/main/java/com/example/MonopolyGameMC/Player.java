package com.example.MonopolyGameMC;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

/*
    游戏玩家
 */

public class Player implements Serializable {

    private int id;
    private String name;
    private int pos = 0;
    private int money = 10000;
    private boolean isAI = true;
    private int imprisonedRound = 0;
    private boolean freeBuff = false;
    private boolean stayBuff = false;
    private int turtleRound = 0;
    transient private MonopolyGameLogic monopolyGameLogic;
    private final Board board;
    private final Dice[] dice;
    private ArrayList<House> houseList = new ArrayList<>();
    private ArrayList<PropCard> cardList = new ArrayList<>();
    private GameTimer gameTimer = new GameTimer();

    public Player(int id, Board board, Dice[] dice){
        this.id = id;
        name = "P" + (id + 1);
        this.monopolyGameLogic = monopolyGameLogic;
        this.board = board;
        this.dice = dice;
    }

    public void setController(MonopolyGameLogic monopolyGameLogic){
        this.monopolyGameLogic = monopolyGameLogic;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void setPos(int p){
        pos = p;
        Log.d("QAQ", "new Pos: " + p);
    }

    public int getPos(){
        return pos;
    }

    public void addMoney(int add){
        money += add;
    }

    public void reduceMoney(int reduce){
        money -= reduce;
        if(money < 0){
            bankrupt();
        }
    }

    public int getMoney(){
        return money;
    }

    public void imprison(){
        imprisonedRound = 2;
    }

    public boolean isImprisoned(){
        return imprisonedRound > 0;
    }

    public void addHouse(House h){
        houseList.add(h);
    }

    public House getHouse(int id){
        return houseList.get(id);
    }

    public int getHouseNum(){
        return houseList.size();
    }

    public void setAI(boolean b){
        isAI = b;
    }

    public boolean getIsAI(){
        return isAI;
    }

    public void setFreeBuff(boolean b){
        freeBuff = b;
    }

    public boolean getFreeBuff(){
        return freeBuff;
    }

    public void setStayBuff(boolean b){
        stayBuff = b;
    }

    public void turtle(){
        turtleRound = 2;
    }

    public boolean isOut(){
        return money < 0;
    }

    public ArrayList<PropCard> getCardList(){
        return cardList;
    }

    public void addCard(PropCard card){
        if(cardList.size() < 3) cardList.add(card);
    }

    // 选择使用的道具卡
    private int chooseCardToUse(){
        if(isAI){
            Random r = new Random();
            int cardID = r.nextInt(cardList.size() + 2);
            if(cardID < cardList.size()){
                return cardID;
            }
            else return -1;
        }
        else{
            if(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_Y){
                int cardID = monopolyGameLogic.getCardSelect();
                if(cardID < cardList.size()){
                    return cardID;
                }
                else{
                    return -1;
                }
            }
        }
        return -1;
    }

    // 选择道具卡作用的玩家
    public Player choosePlayerToUse(){
        Player player;
        if(isAI){
            Random r = new Random();
            int playerID;
            do{
                playerID = r.nextInt(4);
                player = monopolyGameLogic.getPlayer(playerID);
            } while(playerID == this.id || player.isOut());
            return player;
        }
        else{
            player = monopolyGameLogic.getPlayerSelect();
            if(player == null || player.isOut()) return null;
            else return player;
        }
    }

    private void block(long t){
        try {
            sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 轮到该玩家行动
    public void takeTurn(){
        if(imprisonedRound > 0){
            stayBuff = false;
            --turtleRound;
            --imprisonedRound;
            monopolyGameLogic.addEventMsg(getName() + "距出狱还有" + imprisonedRound + "回合");
            block(1000);
            return;
        }
        if(isAI){ // 人机逻辑
            block(1000); // 迟钝亿点点
            // usePropCard
            int cardID = chooseCardToUse();
            if(cardID != -1 && cardList.get(cardID).usePropCard(this)){
                cardList.remove(cardID);
                block(1000);
            }
        }
        else{
            monopolyGameLogic.enRoll();
            gameTimer.setResTime(15000 + 1000);
            while(monopolyGameLogic.getGameState() != MonopolyGameLogic.GameState.ROLL){
                // 计时15s、使用道具卡
                int cardID = chooseCardToUse();
                if(cardID != -1 && cardList.get(cardID).usePropCard(this)){
                    cardList.remove(cardID);
                }
                monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.ENROLL);
                if (gameTimer.isTimeOut()) break;
            }
        }
        dice[0].rollDice();
        dice[1].rollDice();
        monopolyGameLogic.startRoll();
        block(2000);
        int faceValue = dice[0].getFaceValue() + dice[1].getFaceValue();
        if(stayBuff){
            stayBuff = false;
            --turtleRound;
            monopolyGameLogic.addEventMsg(getName() + "该回合停留在原地");
            block(1000);
            board.getSquare(getPos()).landedOn(this);
            return;
        }
        if(turtleRound > 0){
            --turtleRound;
            faceValue = 1;
            monopolyGameLogic.addEventMsg(getName() + "移动一格，乌龟状态剩余" + turtleRound + "回合");
        }
        int newPos = board.getPos(getPos() + faceValue);
        setPos(newPos);
        monopolyGameLogic.startMove();
        block(240 * faceValue + 300);
        Square square = board.getSquare(newPos);
        square.landedOn(this);
    }

    // 是否购买房屋
    public void buyHouseOrNot(House h){
        if(isAI){
            if(money >= h.getBuyAmount()) h.buyHouse(this);
        }
        else{
            monopolyGameLogic.addEventMsg("确认花费$" + h.getBuyAmount() + "购买此房屋？");
            monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_YON);
            while(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YON);
            if(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_Y){
                if(money >= h.getBuyAmount()) h.buyHouse(this);
                else monopolyGameLogic.sendTip("购买房屋失败，金钱不足");
            }
        }
    }

    // 选择飞到的方格
    public void flyToNewPos(){
        int newPos;
        Square square;
        if(isAI){
            Random r = new Random();
            do{
                newPos = r.nextInt(monopolyGameLogic.getBoard().getSquareNum());
                square = board.getSquare(newPos);
                if(square instanceof House){ // 不飞到其他玩家的房屋
                    Player p = ((House)square).getBelongTo();
                    if(p != null && p != this) continue;
                }
                else if(!(square instanceof Prison)){ // 不飞到监狱
                    break;
                }
            }
            while(true);
        }
        else{
            monopolyGameLogic.sendTip("到达飞机场，选择飞行目的地");
            monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_YOY);
            while(monopolyGameLogic.getGameState() != MonopolyGameLogic.GameState.CHOOSE_Y);
            newPos = monopolyGameLogic.getSquareSelect();
        }
        setPos(newPos);
        monopolyGameLogic.flyToNewPos(id, newPos);
        // 再次触发
        square = board.getSquare(newPos);
        if(!(square instanceof Airfield)){
            square.landedOn(this);
        }
    }

    // 判断有无条件使用建造卡
    public boolean isAbleBuildCard(){
        for(int i = 0; i < houseList.size(); ++i){
            House h = houseList.get(i);
            if(h.getGrade() < 3) return true;
        }
        return false;
    }

    // 升级条件：未满级的自己的房屋
    private boolean isAbleToUp(Square square){
        if(square instanceof House){
           House house = (House)square;
           if(house.getBelongTo() == this && house.getGrade() < 3) return true;
           else return false;
        }
        else return false;
    }

    // 使用建造卡，前置条件：持有房屋，且有未满级的房屋
    public void chooseHouseToUp(){
        Square square;
        if(isAI){
            Random r = new Random();
            int hID = r.nextInt(houseList.size());
            square = houseList.get(hID);
        }
        else{
            monopolyGameLogic.sendTip("使用成功，请选择要升级的房屋");
            do{
                monopolyGameLogic.setGameState(MonopolyGameLogic.GameState.CHOOSE_YOY);
                while(monopolyGameLogic.getGameState() != MonopolyGameLogic.GameState.CHOOSE_Y);
                int hID = monopolyGameLogic.getSquareSelect();
                square = board.getSquare(hID);
                if(isAbleToUp(square)){
                    break;
                }
                else{
                    monopolyGameLogic.sendTip("只能选择未满级的自己的房屋");
                }
            } while(true);

        }
        House house = (House)square;
        house.upgrade();
    }

    // 与另一玩家互换位置
    public void transposition(Player p){
        int tmpPos = pos;
        pos = p.getPos();
        p.setPos(tmpPos);
        monopolyGameLogic.flyToNewPos(id, pos);
        monopolyGameLogic.flyToNewPos(p.getId(), p.getPos());
    }

    // 破产
    public void bankrupt(){
        monopolyGameLogic.addEventMsg(name + "破产了，所有房屋被回收");
        for(int i = 0; i < houseList.size(); ++i){
            House house = houseList.get(i);
            house.setBelongTo(null);
        }
    }
}
