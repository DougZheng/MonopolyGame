package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    抽象方格类
 */

public abstract class Square implements Serializable {

    transient protected MonopolyGameLogic monopolyGameLogic;

    public void setController(MonopolyGameLogic monopolyGameLogic){
        this.monopolyGameLogic = monopolyGameLogic;
    }

    public abstract void landedOn(Player p);
}
