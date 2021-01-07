package com.example.MonopolyGameMC;

import java.io.Serializable;

/*
    游戏板，包含若干方格
 */

public class Board implements Serializable {

    private final int squareXNum;
    private final int squareYNum;
    private final int squareNum;
    private Square[] squares;

    public Board(int squareXNum, int squareYNum){
        this.squareXNum = squareXNum;
        this.squareYNum = squareYNum;
        this.squareNum = (squareXNum + squareYNum) * 2 - 4;
        squares = new Square[squareNum];
        squares[0] = new StartPoint();
        squares[squareYNum - 1] = new Airfield();
        squares[squareNum / 2] = new Prison();
        squares[squareNum - squareXNum + 1] = new Cappuccino();
        squares[squareYNum / 2] = new RandomEvent();
        squares[squareYNum - 1 + squareXNum / 2] = new RandomEvent();
        squares[squareNum / 2 + squareYNum / 2] = new RandomEvent();
        squares[squareNum - squareXNum / 2] = new RandomEvent();
        for(int i = 0; i < squareNum; ++i){
            if(squares[i] == null) squares[i] = new House();
        }
    }

    public void setController(MonopolyGameLogic monopolyGameLogic){
        for(int i = 0; i < squareNum; ++i){
            squares[i].setController(monopolyGameLogic);
        }
    }

    public Square getSquare(int id){
        return squares[id];
    }

    public int getSquareNum(){
        return squareNum;
    }

    // 通过板大小转化具体位置
    public int getPos(int pos){
        return pos % squareNum;
    }
}
