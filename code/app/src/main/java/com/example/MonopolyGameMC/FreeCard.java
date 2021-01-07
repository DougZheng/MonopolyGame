package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    道具卡 - 免费卡
 */

public class FreeCard extends PropCard implements Serializable {

    @Override
    public boolean usePropCard(Player p) {
        monopolyGameLogic.sendTip(p.getName() + "使用免费卡");
        p.setFreeBuff(true);
        return true;
    }
}
