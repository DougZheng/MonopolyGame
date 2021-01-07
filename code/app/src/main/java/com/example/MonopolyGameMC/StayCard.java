package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    道具卡 - 停留卡
 */

public class StayCard extends PropCard implements Serializable {

    @Override
    public boolean usePropCard(Player p) {
        Player p2 = p.choosePlayerToUse();
        if(p2 != null){
            monopolyGameLogic.sendTip(p.getName() + "对" + p2.getName() + "使用停留卡");
            p2.setStayBuff(true);
            return true;
        }
        if(!p.getIsAI()) monopolyGameLogic.sendTip("使用失败，请指定玩家（点击头像）");
        return false;
    }
}
