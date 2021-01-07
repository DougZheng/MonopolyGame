package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    道具卡 - 互换卡
 */

public class TranspositionCard extends PropCard implements Serializable {

    @Override
    public boolean usePropCard(Player p) {
        Player p2 = p.choosePlayerToUse();
        if(p2 != null){
            monopolyGameLogic.sendTip(p.getName() + "对" + p2.getName() + "使用互换卡");
            p.transposition(p2);
            return true;
        }
        if(!p.getIsAI()) monopolyGameLogic.sendTip("使用失败，请指定玩家（点击头像）");
        return false;
    }
}
