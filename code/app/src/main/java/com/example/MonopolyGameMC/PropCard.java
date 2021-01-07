package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    抽象道具卡类
 */

public abstract class PropCard implements Serializable {

    transient protected MonopolyGameLogic monopolyGameLogic;

    public void setController(MonopolyGameLogic monopolyGameLogic){
        this.monopolyGameLogic = monopolyGameLogic;
    }

    public abstract boolean usePropCard(Player p);
}
