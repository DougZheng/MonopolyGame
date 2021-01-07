package com.example.MonopolyGameMC;

import java.io.Serializable;
import java.util.Random;

/*
    特殊方格 - 随机事件方格
 */

public class RandomEvent extends Square implements Serializable {

    private Random r = new Random();
    private final int eventNum = 11;

    @Override
    public void landedOn(Player p){
        int id = r.nextInt(eventNum);
        switch(id){
            case 0:{
                monopolyGameLogic.addEventMsg(p.getName() + "给阿姨倒了杯卡布奇诺，" + p.getName() + "花费$250");
                p.reduceMoney(250);
                break;
            }
            case 1:{
                monopolyGameLogic.addEventMsg("阿姨给" + p.getName() + "倒了杯卡布奇诺，" + p.getName() + "获得$250");
                p.addMoney(250);
                break;
            }
            case 2:{
                monopolyGameLogic.addEventMsg(p.getName() + "请专业团队跳舞，" + p.getName() + "花费$250");
                p.reduceMoney(250);
                break;
            }
            case 3:{
                monopolyGameLogic.addEventMsg("专业团队请" + p.getName() + "跳舞，" + p.getName() + "失去$500");
                p.reduceMoney(500);
                break;
            }
            case 4:{
                monopolyGameLogic.addEventMsg("阿伟突然发红包了，" + p.getName() + "抢到$1");
                p.addMoney(1);
                break;
            }
            case 5:{
                monopolyGameLogic.addEventMsg("阿伟突然要你发红包了，" + p.getName() + "花费$100");
                p.reduceMoney(100);
                break;
            }
            case 6:{
                monopolyGameLogic.addEventMsg("阿伟突然又发红包了，" + p.getName() + "抢到$2");
                p.addMoney(2);
                break;
            }
            case 7:{
                monopolyGameLogic.addEventMsg("不知道怎么描述了，反正" + p.getName() + "得到$500");
                p.addMoney(500);
                break;
            }
            case 8:{
                monopolyGameLogic.addEventMsg("得得得得得，" + p.getName() + "某处房屋升级了");
                int n = p.getHouseNum();
                if(n > 0){
                    House h = p.getHouse(r.nextInt(n));
                    h.upgrade();
                }
                break;
            }
            case 9:{
                monopolyGameLogic.addEventMsg("天降鸿运，" + p.getName() + "获得一张道具卡");
                monopolyGameLogic.acquiredCard(p.getId());
                break;
            }
            default:{
                monopolyGameLogic.addEventMsg("什么事都没有发生");
                break;
            }
        }
    }
}
