package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    特殊方格 - 起点
 */

public class StartPoint extends Square implements Serializable {

    @Override
    public void landedOn(Player p){
        p.addMoney(1000);
        monopolyGameLogic.addEventMsg(p.getName() + "到达起点，奖励$1000");
    }
}
