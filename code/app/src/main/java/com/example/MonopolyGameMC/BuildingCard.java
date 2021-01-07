package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    道具卡 - 建造卡
 */

public class BuildingCard extends PropCard implements Serializable {

    @Override
    public boolean usePropCard(Player p) {
        if(p.isAbleBuildCard()){
            monopolyGameLogic.sendTip(p.getName() + "使用建造卡");
            p.chooseHouseToUp();
            return true;
        }
        else{
            if(!p.getIsAI()) monopolyGameLogic.sendTip("使用失败，没有可升级的房屋");
            return false;
        }
    }
}