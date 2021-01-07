package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    特殊方格 - 飞机场
 */

public class Airfield extends Square implements Serializable {

    @Override
    public void landedOn(Player p){
        p.flyToNewPos();
    }
}
