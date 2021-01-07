package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    特殊方格 - 监狱
 */

public class Prison extends Square implements Serializable {

    @Override
    public void landedOn(Player p){
        p.imprison();
        monopolyGameLogic.addEventMsg(p.getName() + "进入监狱，2回合内禁止行动");
    }
}
