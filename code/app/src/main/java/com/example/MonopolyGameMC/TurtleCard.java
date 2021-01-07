package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    道具卡 - 乌龟卡
 */

public class TurtleCard extends PropCard implements Serializable {

    @Override
    public boolean usePropCard(Player p) {
        Player p2 = p.choosePlayerToUse();
        if(p2 != null){
            monopolyGameLogic.sendTip(p.getName() + "对" + p2.getName() + "使用乌龟卡");
            p2.turtle();
            return true;
        }
        if(!p.getIsAI()) monopolyGameLogic.sendTip("使用失败，请指定玩家（点击头像）");
        return false;
    }
}
