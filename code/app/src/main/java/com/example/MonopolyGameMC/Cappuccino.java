package com.example.MonopolyGameMC;

import java.io.Serializable;
import java.util.Random;

/*
    特殊方格 - 卡布奇诺
 */

public class Cappuccino extends Square implements Serializable {

    @Override
    public void landedOn(Player p){
        int n = p.getHouseNum();
        if(n > 0){
            Random r = new Random();
            int id = r.nextInt(n);
            House h = p.getHouse(id);
            h.upgrade();
            monopolyGameLogic.addEventMsg(p.getName() + "获得lbw的力量，某处房屋升级了");
        }
    }
}
