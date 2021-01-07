package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    方格 - 房屋
 */

public class House extends Square implements Serializable {

    private int grade = 0;
    private int houseToll = 250;
    private int buyAmount = 500;
    private Player belongTo = null;

    public int getGrade(){
        return grade;
    }

    public int getBuyAmount(){
        return buyAmount;
    }

    public void upgrade(){
        switch(grade){
            case 0: ++grade; houseToll = 500; break;
            case 1: ++grade; houseToll = 1000; break;
            case 2: ++grade; houseToll = 2500; break;
            default: break;
        }
    }

    public Player getBelongTo(){
        return belongTo;
    }

    public void setBelongTo(Player p){
        belongTo = p;
    }

    @Override
    public void landedOn(Player p){
        if(belongTo == null){ // 无人占领
            p.buyHouseOrNot(this);
        }
        else{
            if(belongTo == p){ // 自己的房屋
                upgrade();
            }
            else if(!belongTo.isImprisoned()){
                if(p.getFreeBuff()){
                    monopolyGameLogic.addEventMsg(p.getName() + "免费卡生效，免付通行费");
                    p.setFreeBuff(false);
                }
                else{
                    p.reduceMoney(houseToll);
                    belongTo.addMoney(houseToll);
                    monopolyGameLogic.addEventMsg(p.getName() + "经过" + belongTo.getName() + "的房屋，付通行费$" + houseToll);
                }
            }
            else{
                monopolyGameLogic.addEventMsg(belongTo.getName() + "在狱中，免收" + p.getName() + "通行费");
            }
        }
    }

    public void buyHouse(Player p){
        p.reduceMoney(buyAmount);
        p.addHouse(this);
        setBelongTo(p);
        monopolyGameLogic.addEventMsg(p.getName() + "购买房屋，花费$" + buyAmount);
    }
}
