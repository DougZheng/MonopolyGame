package com.example.MonopolyGameV;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.MonopolyGameMC.*;

import java.util.ArrayList;

/*
    负责游戏界面的绘制
 */

public class MonopolyGameView extends View {

    private static final String TAG = "MonopolyGameView"; // for Log.d

    public final int squareLNum = 5; // 左右放置的方格数
    public final int squareUNum = 7; // 上下放置的方格数
    public final int squareNum = squareLNum * 2 + squareUNum * 2 + 4;
    public final int playerNum = 4;
    public final int playerCardNum = 3; // 玩家持有道具卡最大数量
    public final int totCardNum = 5; // 道具卡种数

    // 图形类型：其他，方格，骰子按钮
    public enum ViewType{
        DEFAULT, SQUARE, BUTTON_DICE, BUTTON_Y, BUTTON_N, BUTTON_BACK,
        PLAYER, PROP_CARD
    }

    private int viewWidth;
    private int viewHeight;
    private float baseGapWidth;
    private float baseGapHeight;

    // 图像、动画
    private GameRect header;
    private GameRect board;
    private GameRect body;
    private GameRect eventWnd;
    private GameRect[] datas;
    private GameRect[] squares;

    private GameButton[] buttonDiceA;
    private GameButton[] buttonDiceS;
    private GameButton[] buttonY;
    private GameButton[] buttonN;
    private GameButton buttonBack;

    private GameImage[] players;
    private GameImage[] playersBig;
    private GameImage[] playersSmall;
    private GameImage[] dicePip;
    private GameImage[] diceRoll;
    private GameImage[] dicePipSmall;
    private GameImage[] diceRollSmall;
    private GameImage[] squareImages;
    private GameImage[] housesL;
    private GameImage[] housesU;
    private GameImage[] money;
    private GameImage[] propCards;

    private GameImage startPointDesc;
    private GameImage airfieldDesc;
    private GameImage prisonDesc;
    private GameImage cappuccinoDesc;
    private GameImage randomEventDesc;
    private GameImage[] houseDesc;
    private GameImage[] cardDesc;

    public GameAnimation diceAnimation;
    public GameAnimation diceSmallAnimation;
    public GameAnimation buttonDiceAnimation;
    public PlayerAnimation[] playerAnimations;

    private EventMsgManager eventMsgManager;

    private int[] colorOfPlayers; // TODO: 可以自定义颜色
    private int[] resIdOfPlayers;
    private int[] resIdOfDicePip;
    private int[] resIdOfDiceRoll;
    private int[] playerNumOfSquare;

    private Bitmap backgroundBitmap; // 静态背景图
    private Paint paintFill = new Paint();
    private Paint paintStroke = new Paint();
    private Paint paintAlpha = new Paint();
    private Paint paintText = new Paint();

    private int squareSelect = -1;
    private int playerSelect = -1;
    private int cardSelect = -1;

    private MonopolyGameLogic monopolyGameLogic;

    public MonopolyGameView(Context context){
        super(context);
        monopolyGameLogic = new MonopolyGameLogic(this, initHandler());
        setOnTouchListener(new MonopolyGameListener(this, monopolyGameLogic));
    }

    public MonopolyGameView(Context context, AttributeSet attrs){
        super(context, attrs);
        monopolyGameLogic = new MonopolyGameLogic(this, initHandler());
        setOnTouchListener(new MonopolyGameListener(this, monopolyGameLogic));
    }

    // 与游戏线程通信
    public Handler initHandler(){
        return new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case 1001:{
                        int num = (Integer)msg.obj;
                        playerAnimations[msg.arg1].start(msg.arg2, num);
                        break;
                    }
                    case 1002:{
                        String event = (String)msg.obj;
                        eventMsgManager.addMsg(event);
                        break;
                    }
                    case 1003:{
                        playerAnimations[msg.arg1].setPos(msg.arg2);
                        break;
                    }
                    case 1004:{
                        ArrayList<String> list = (ArrayList<String>)msg.obj;
                        eventMsgManager.setMsgList(list);
                        break;
                    }
                    case 1005:{
                        squareSelect = -1;
                        playerSelect = -1;
                        cardSelect = -1;
                        String tip = (String)msg.obj;
                        eventMsgManager.addMsg(tip);
                        break;
                    }
                    default: break;
                }
                invalidate();
            }
        };
    }

    public MonopolyGameLogic getThread(){
        return monopolyGameLogic;
    }

    @Override
    protected void onDraw(Canvas canvas){
        Log.d(TAG, "onDraw");
        long t1 = android.os.SystemClock.uptimeMillis();
        int layerId = canvas.saveLayer(0, 0, viewWidth, viewHeight, null, Canvas.ALL_SAVE_FLAG);
        drawView(canvas);
        canvas.restoreToCount(layerId);
        long t2 = android.os.SystemClock.uptimeMillis();
        Log.d(TAG, "duration: " + (t2 - t1) + "ms");
    }

    private void drawView(Canvas canvas){
        Log.d(TAG, "drawView");
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        drawHouse(canvas);
        drawCurPlayer(canvas);
        boolean reDraw = false; // 是否有动画未完成，需要重绘
        reDraw |= drawDice(canvas);
        reDraw |= drawButtonDice(canvas);
        for(int i = 0; i < squareNum; ++i){
            playerNumOfSquare[i] = 0;
        }
        int curPlayer = monopolyGameLogic.getCurPlayer();
        reDraw |= drawPlayer(canvas, curPlayer);
        for(int i = (curPlayer + 1) % playerNum; i != curPlayer; i = (i + 1) % playerNum) {
            reDraw |= drawPlayer(canvas, i); // 越先行动显示优先级越高
        }
        drawMoney(canvas);
        drawEvent(canvas);
        drawDesc(canvas);
        drawCard(canvas);
        if(reDraw) invalidate();
    }

    // 事件绘制
    private void drawEvent(Canvas canvas){
        eventMsgManager.drawMsg(canvas, paintText);
        if(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YON){
            buttonY[0].draw(canvas, paintFill);
            buttonN[0].draw(canvas, paintFill);
        }
        else{
            buttonY[1].draw(canvas, paintFill);
            buttonN[1].draw(canvas, paintFill);
        }
    }

    // 玩家金钱绘制
    private void drawMoney(Canvas canvas){
        for(int i = 0; i < playerNum; ++i){
            float x = money[i].getPosX() + money[i].getWidth();
            float y = money[i].getPosY() + money[i].getHeight() * 0.8f;
            paintText.setTextSize(money[i].getWidth() * 0.6f);
            String moneyText = String.valueOf(monopolyGameLogic.getPlayer(i).getMoney());
            canvas.drawText(moneyText, x, y, paintText);
        }
    }

    // 选框描述
    private void drawDesc(Canvas canvas){
        if(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YON){
            squareSelect = -1;
            cardSelect = -1;
            playerSelect = -1;
        }
        if(playerSelect != -1){
            drawBound(canvas, playersBig[playerSelect].getRectF(), 8, colorOfPlayers[playerSelect]);
        }
        if(squareSelect != -1){
            eventWnd.draw(canvas, paintFill);
            boolean flyFlag = false;
            if(monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.CHOOSE_YOY){
                buttonY[0].draw(canvas, paintFill);
                buttonN[1].draw(canvas, paintFill);
                flyFlag = true;
            }
            else{
                buttonY[1].draw(canvas, paintFill);
                buttonN[1].draw(canvas, paintFill);
            }
            drawBound(canvas, squares[squareSelect], 8);
            Square square = monopolyGameLogic.getBoard().getSquare(squareSelect);
            if(square instanceof StartPoint){
                startPointDesc.draw(canvas, null);
            }
            else if(square instanceof Airfield){
                airfieldDesc.draw(canvas, null);
            }
            else if(square instanceof Prison){
                prisonDesc.draw(canvas, null);
            }
            else if(square instanceof Cappuccino){
                cappuccinoDesc.draw(canvas, null);
            }
            else if(square instanceof RandomEvent){
                randomEventDesc.draw(canvas, null);
            }
            else{
                houseDesc[((House)square).getGrade()].draw(canvas, null);
            }
            if(flyFlag) return;
        }
        if(cardSelect != -1){
            PropCard card = monopolyGameLogic.getCard(cardSelect);
            if(card != null){
                eventWnd.draw(canvas, paintFill);
                int playerID = cardSelect / playerCardNum;
                int cardID = cardSelect % playerCardNum;
                int cardType = getCardID(card);
                float x = datas[playerID].getPosX() + datas[playerID].getWidth() * (0.4f + cardID * 0.2f);
                float y = datas[playerID].getPosY() + datas[playerID].getHeight() * 0.66f;
                propCards[cardType].setPosX(x);
                propCards[cardType].setPosY(y);
                drawBound(canvas, propCards[cardType].getRectF(), 8, colorOfPlayers[playerID]);
                cardDesc[cardType].draw(canvas, null);
                if(cardSelect < 3 && monopolyGameLogic.getGameState() == MonopolyGameLogic.GameState.ENROLL){
                    buttonY[0].draw(canvas, paintFill);
                    buttonN[1].draw(canvas, paintFill);
                }
                else{
                    buttonY[1].draw(canvas, paintFill);
                    buttonN[1].draw(canvas, paintFill);
                }
            }
            else{
                cardSelect = -1;
            }
        }
    }

    // 房屋绘制
    private void drawHouse(Canvas canvas){
        for(int i = 0; i < squareNum; ++i){
            Square square = monopolyGameLogic.getBoard().getSquare(i);
            if(!(square instanceof House)) continue;
            House house = (House)square;
            squareImages[i].setBitmap(getHouseBitmap(i, house.getGrade()));
            squareImages[i].draw(canvas, paintAlpha);
            if(house.getBelongTo() != null){
                squares[i].setColor(colorOfPlayers[house.getBelongTo().getId()]);
                drawOccupy(canvas, squares[i]);
            }
        }
    }

    private int getCardID(PropCard card){
        int cardID;
        if(card instanceof TranspositionCard) cardID = 0;
        else if(card instanceof TurtleCard) cardID = 1;
        else if(card instanceof StayCard) cardID = 2;
        else if(card instanceof BuildingCard) cardID = 3;
        else cardID = 4;
        return cardID;
    }

    // 卡牌绘制
    private void drawCard(Canvas canvas){
        for(int i = 0; i < playerNum; ++i){
            float x = datas[i].getPosX() + datas[i].getWidth() * 0.4f;
            float y = datas[i].getPosY() + datas[i].getHeight() * 0.66f;
            ArrayList<PropCard> cardList = monopolyGameLogic.getCardList(i);
            for(int j = 0; j < cardList.size(); ++j){
                PropCard card = cardList.get(j);
                int cardID = getCardID(card);
                propCards[cardID].setPosX(x);
                propCards[cardID].setPosY(y);
                propCards[cardID].draw(canvas, null);
                x += datas[i].getWidth() * 0.2f;
            }
        }
    }

    // 万能绘制边框大法
    private void drawBound(Canvas canvas, GameRect rect, float width){
        paintStroke.setColor(rect.getColor() & 0X00FFFFFF | 0X7F000000);
        paintStroke.setStrokeWidth(width);
        canvas.drawRoundRect(rect.getRectF(), rect.getRoundX(), rect.getRoundY(), paintStroke);
    }

    private void drawBound(Canvas canvas, RectF rect, float width, int color){
        paintStroke.setColor(color & 0X00FFFFFF | 0X7F000000);
        paintStroke.setStrokeWidth(width);
        Log.d("zbs", rect.left + " " + rect.top + " " + rect.width() + " " + rect.height());
        canvas.drawRoundRect(rect, width, width, paintStroke);
    }

    // 绘制玩家占领标志
    private void drawOccupy(Canvas canvas, GameRect rect){
        Path path = new Path();
        RectF r = new RectF(rect.getRectF());
        r.left += 2.5f; r.right -= 2.5f;
        r.top += 2.5f; r.bottom -= 2.5f;
        path.moveTo(r.right - 2.5f, r.bottom - 2.5f);
        path.arcTo(r, 0, 90, false);
        path.close();
        paintFill.setColor(rect.getColor());
        canvas.drawPath(path, paintFill);
    }

    // 绘制当前玩家边框
    private void drawCurPlayer(Canvas canvas){
        Log.d(TAG, "drawCurPlayer");
        int curPlayer = monopolyGameLogic.getCurPlayer();
        drawBound(canvas, datas[curPlayer], baseGapHeight);
    }

    // 绘制骰子动画
    private boolean drawDice(Canvas canvas){
        Log.d(TAG, "drawDice");
        diceAnimation.draw(canvas, null, monopolyGameLogic.getDice(0).getFaceValue() - 1);
        diceSmallAnimation.draw(canvas, null, monopolyGameLogic.getDice(1).getFaceValue() - 1);
        return diceAnimation.isAlive() | diceSmallAnimation.isAlive();
    }

    private boolean drawButtonDice(Canvas canvas){
        Log.d(TAG, "drawButtonDice");
        buttonDiceAnimation.draw(canvas, paintFill, 0);
        return buttonDiceAnimation.isAlive();
    }

    // 绘制玩家移动动画
    private boolean drawPlayer(Canvas canvas, int id){
        Log.d(TAG, "drawPlayer");
        int nxtPos = playerAnimations[id].getNextPos();
        boolean isAlive = playerAnimations[id].draw(canvas, squares[nxtPos].getRectF(), playerNumOfSquare[nxtPos]++);
        return isAlive;
    }

    // view初始化工作，适配屏幕
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("TAG", "onSizeChanged");
        viewWidth = w;
        viewHeight = h;
        Log.d(TAG, "viewSize: " + w + " x " + h);
        initParams();
        createBackground();
        monopolyGameLogic.start();
    }

    // 游戏图形、动画参数初始化
    private void initParams(){
        Log.d(TAG, "initParams");

        float squareLWidth, squareLHeight, squareUWidth, squareUHeight;
        float curX, curY, squareWidth, squareHeight;
        float playerWidth, playerHeight;
        float diceX, diceY, diceWidth, diceHeight;
        float diceSmallX, diceSmallY, diceSmallWidth, diceSmallHeight;
        float buttonDiceX, buttonDiceY, buttonDiceWidth, buttonDiceHeight;
        float buttonYX, buttonYY, buttonNX, buttonNY, buttonYNWidth, buttonYNHeight;

        baseGapWidth = viewWidth * 0.01f;
        baseGapHeight = viewHeight * 0.01f;

        colorOfPlayers = new int[playerNum];
        colorOfPlayers[0] = ContextCompat.getColor(getContext(), R.color.colorDog);
        colorOfPlayers[1] = ContextCompat.getColor(getContext(), R.color.colorHorse);
        colorOfPlayers[2] = ContextCompat.getColor(getContext(), R.color.colorFrog);
        colorOfPlayers[3] = ContextCompat.getColor(getContext(), R.color.colorCock);

        resIdOfPlayers = new int[playerNum];
        resIdOfPlayers[0] = R.drawable.player_dog;
        resIdOfPlayers[1] = R.drawable.player_horse;
        resIdOfPlayers[2] = R.drawable.player_frog;
        resIdOfPlayers[3] = R.drawable.player_cock;

        resIdOfDicePip = new int[6];
        resIdOfDicePip[0] = R.drawable.dice_pip1;
        resIdOfDicePip[1] = R.drawable.dice_pip2;
        resIdOfDicePip[2] = R.drawable.dice_pip3;
        resIdOfDicePip[3] = R.drawable.dice_pip4;
        resIdOfDicePip[4] = R.drawable.dice_pip5;
        resIdOfDicePip[5] = R.drawable.dice_pip6;

        resIdOfDiceRoll = new int[4];
        resIdOfDiceRoll[0] = R.drawable.dice_roll0;
        resIdOfDiceRoll[1] = R.drawable.dice_roll1;
        resIdOfDiceRoll[2] = R.drawable.dice_roll2;
        resIdOfDiceRoll[3] = R.drawable.dice_roll3;

        header = new GameRect(baseGapWidth, baseGapHeight, (viewWidth - baseGapWidth * 3) * 0.2f,
                viewHeight - baseGapHeight * 2, baseGapWidth, baseGapWidth,
                ContextCompat.getColor(getContext(), R.color.colorHeader));

        datas = new GameRect[playerNum];
        datas[0] = new GameRect(baseGapWidth * 2, baseGapHeight * 3,
                header.getWidth() - baseGapWidth * 2, (header.getHeight() - baseGapHeight * 10) * 0.25f,
                baseGapWidth, baseGapWidth, colorOfPlayers[0]);
        for(int i = 1; i < playerNum; ++i){
            datas[i] = new GameRect(datas[i - 1].getPosX(),
                    datas[i - 1].getPosY() + datas[i - 1].getHeight() + baseGapHeight * 2,
                    datas[i - 1].getWidth(), datas[i - 1].getHeight(),
                    baseGapWidth, baseGapWidth, colorOfPlayers[i]);
        }

        board = new GameRect(header.getWidth() + baseGapWidth * 2, baseGapHeight,
                viewWidth - baseGapWidth * 3 - header.getWidth(), header.getHeight(), baseGapHeight, baseGapHeight,
                ContextCompat.getColor(getContext(), R.color.colorBoard));

        squareLWidth = squareUHeight = Math.min(board.getWidth() * 0.2f, board.getHeight() * 0.2f);

        body = new GameRect(header.getWidth() + baseGapWidth * 2 + squareLWidth, baseGapHeight + squareUHeight,
                board.getWidth() - squareLWidth * 2, board.getHeight() - squareUHeight * 2, 0, 0,
                ContextCompat.getColor(getContext(), R.color.colorBody));

        eventWnd = new GameRect(viewWidth - baseGapWidth - squareLWidth - body.getWidth() * 0.66f * 0.94f,
                body.getHeight() * 0.12f * 0.5f + baseGapHeight + squareUHeight,
                body.getWidth() * 0.66f * 0.88f, body.getHeight() * 0.88f, baseGapWidth, baseGapWidth,
                ContextCompat.getColor(getContext(), R.color.colorEventWnd));

        squareUWidth = body.getWidth() / squareUNum;
        squareLHeight = body.getHeight() / squareLNum;

        squares = new GameRect[squareNum];
        squares[0] = new GameRect(header.getWidth() + baseGapWidth * 2 + board.getWidth() - squareLWidth,
                baseGapHeight + board.getHeight() - squareUHeight, squareLWidth, squareUHeight,
                baseGapHeight, baseGapHeight,
                ContextCompat.getColor(getContext(), R.color.colorBoard));
        curX = squares[0].getPosX(); curY = squares[0].getPosY();
        for(int i = 1; i < squareNum; ++i){
            if(i == squareLNum + squareUNum + 2){
                curX -= squareLWidth;
            }
            else if(i == squareLNum * 2 + squareUNum + 4){
                curX += squareLWidth;
            }
            else if(i > squareLNum + 1 && i < squareLNum + squareUNum + 2){
                curX -= squareUWidth;
            }
            else if(i > squareLNum * 2 + squareUNum + 4){
                curX += squareUWidth;
            }
            if(i == squareLNum + 1){
                curY -= squareUHeight;
            }
            else if(i == squareLNum + squareUNum + 3){
                curY += squareUHeight;
            }
            else if(i < squareLNum + 1){
                curY -= squareLHeight;
            }
            else if(i > squareLNum + squareUNum + 3 && i < squareLNum * 2 + squareUNum + 4){
                curY += squareLHeight;
            }
            if(i == squareLNum + 1 || i == squareLNum + squareUNum + 2 || i == squareLNum * 2 + squareUNum + 3){
                squareWidth = squareLWidth;
                squareHeight = squareUHeight;
            }
            else if(i < squareLNum + 1 || i > squareLNum + squareUNum + 2 && i < squareLNum * 2 + squareUNum + 3){
                squareWidth = squareLWidth;
                squareHeight = squareLHeight;
            }
            else{
                squareWidth = squareUWidth;
                squareHeight = squareUHeight;
            }
            squares[i] = new GameRect(curX, curY, squareWidth, squareHeight, baseGapHeight, baseGapHeight,
                    ContextCompat.getColor(getContext(), R.color.colorBoard));
        }

        playerWidth = (int)(Math.min(Math.min(squareLWidth, squareLHeight), Math.min(squareUWidth, squareUHeight)) * 0.66f);
        playerHeight = playerWidth;
        players = new GameImage[playerNum];
        for(int i = 0; i < playerNum; ++i){
            players[i] = new GameImage(0, 0, playerWidth, playerHeight, resIdOfPlayers[i]);
        }
        playersBig = new GameImage[playerNum];
        for(int i = 0; i < playerNum; ++i){
            playersBig[i] = new GameImage(getInnerRectF(datas[i].getPosX(), datas[i].getPosY(),
                    datas[i].getWidth() * 0.4f, datas[i].getHeight() * 0.66f, 0.8f), resIdOfPlayers[i]);
        }
        playersSmall = new GameImage[playerNum];
        for(int i = 0; i < playerNum; ++i){
            playersSmall[i] = new GameImage(0, 0, playerWidth / 2, playerHeight / 2, resIdOfPlayers[i]);
        }

        diceWidth = (int)Math.min((body.getWidth() - eventWnd.getWidth() - baseGapWidth * 4) * 0.66f, body.getHeight() * 0.33f);
        diceHeight = diceWidth;
        diceX = baseGapWidth * 2 + header.getWidth() + squareLWidth
                + ((body.getWidth() - eventWnd.getWidth()) * 0.66f - baseGapWidth * 2 - diceWidth) * 0.5f;
        diceY = baseGapHeight * 2 + squareUHeight + body.getHeight() * 0.1f;
        diceSmallWidth = diceWidth * 0.8f;
        diceSmallHeight = diceHeight * 0.8f;
        diceSmallX = (diceX + diceWidth + eventWnd.getPosX()) * 0.5f - diceSmallWidth * 0.5f;
        diceSmallY = diceY + diceHeight - diceSmallHeight;
        dicePip = new GameImage[6];
        dicePipSmall = new GameImage[6];
        for(int i = 0; i < 6; ++i){
            dicePip[i] = new GameImage(diceX, diceY, diceWidth, diceHeight, resIdOfDicePip[i]);
            dicePipSmall[i] = new GameImage(diceSmallX, diceSmallY, diceSmallWidth, diceSmallHeight, resIdOfDicePip[i]);
        }
        diceRoll = new GameImage[4];
        diceRollSmall = new GameImage[4];
        for(int i = 0; i < 4; ++i){
            diceRoll[i] = new GameImage(diceX, diceY, diceWidth, diceHeight, resIdOfDiceRoll[i]);
            diceRollSmall[i] = new GameImage(diceSmallX, diceSmallY, diceSmallWidth, diceSmallHeight, resIdOfDiceRoll[i]);
        }
        diceAnimation = new GameAnimation(4, 140, 1680, diceRoll, dicePip);
        diceSmallAnimation = new GameAnimation(4, 100, 1680, diceRollSmall, dicePipSmall);

        playerAnimations = new PlayerAnimation[playerNum];
        for(int i = 0; i < playerNum; ++i){
            playerAnimations[i] = new PlayerAnimation(players[i], playersSmall[i]);
        }
        playerNumOfSquare = new int[squareNum];

        buttonDiceWidth = (body.getWidth() - eventWnd.getWidth()) * 0.55f;
        buttonDiceHeight = body.getHeight() * 0.2f;
        buttonDiceX = ((body.getPosX() + eventWnd.getPosX()) * 0.5f - buttonDiceWidth * 0.5f);
        buttonDiceY = baseGapHeight + squareUHeight + body.getHeight() * 0.5f;
        buttonDiceS = new GameButton[2];
        buttonDiceS[0] = new GameButton(buttonDiceX, buttonDiceY, buttonDiceWidth, buttonDiceHeight,
                buttonDiceHeight * 0.5f, buttonDiceHeight * 0.5f,
                ContextCompat.getColor(getContext(), R.color.colorButtonB),
                "大写的按钮", ContextCompat.getColor(getContext(), R.color.colorText));
        buttonDiceS[1] = new GameButton(buttonDiceS[0].getRectF(),
                buttonDiceS[0].getRoundX(), buttonDiceS[0].getRoundY(),
                ContextCompat.getColor(getContext(), R.color.colorButtonA),
                buttonDiceS[0].getButtonText(), ContextCompat.getColor(getContext(), R.color.colorText));
        buttonDiceA = new GameButton[15];
        for(int i = 0; i < 15; ++i){
            buttonDiceA[i] = new GameButton(buttonDiceS[0].getRectF(),
                    buttonDiceS[0].getRoundX(), buttonDiceS[0].getRoundY(),
                    getGraColor(buttonDiceS[1].getColor(), buttonDiceS[0].getColor(), i, 15),
                    buttonDiceS[0].getButtonText(), ContextCompat.getColor(getContext(), R.color.colorText));
        }
        buttonDiceAnimation = new GameAnimation(15, 1000, 15000,
                buttonDiceA, buttonDiceS);

        buttonBack = new GameButton(buttonDiceS[0].getRectF(),
                buttonDiceS[0].getRoundX(), buttonDiceS[0].getRoundY(),
                ContextCompat.getColor(getContext(), R.color.colorButtonA),
                "返回主菜单", ContextCompat.getColor(getContext(), R.color.colorText));
        buttonBack.setPosY(buttonDiceY + buttonDiceHeight * 1.2f);

        buttonYNWidth = eventWnd.getWidth() * 0.32f;
        buttonYNHeight = eventWnd.getHeight() * 0.18f;
        buttonYX = eventWnd.getPosX() + eventWnd.getWidth() * 0.12f;
        buttonYY = eventWnd.getPosY() + eventWnd.getHeight() * 0.96f - buttonYNHeight;
        buttonNX = buttonYX + buttonYNWidth + eventWnd.getWidth() * 0.12f;
        buttonNY = buttonYY;
        buttonY = new GameButton[2];
        buttonN = new GameButton[2];
        buttonY[0] = new GameButton(buttonYX, buttonYY, buttonYNWidth, buttonYNHeight,
                buttonYNHeight * 0.5f, buttonYNHeight * 0.5f,
                ContextCompat.getColor(getContext(), R.color.colorButtonA),
                "确定", ContextCompat.getColor(getContext(), R.color.colorText));
        buttonY[1] = new GameButton(buttonYX, buttonYY, buttonYNWidth, buttonYNHeight,
                buttonYNHeight * 0.5f, buttonYNHeight * 0.5f,
                ContextCompat.getColor(getContext(), R.color.colorButtonB),
                "确定", ContextCompat.getColor(getContext(), R.color.colorText));
        buttonN[0] = new GameButton(buttonNX, buttonNY, buttonYNWidth, buttonYNHeight,
                buttonYNHeight * 0.5f, buttonYNHeight * 0.5f,
                ContextCompat.getColor(getContext(), R.color.colorButtonA),
                "取消", ContextCompat.getColor(getContext(), R.color.colorText));
        buttonN[1] = new GameButton(buttonNX, buttonNY, buttonYNWidth, buttonYNHeight,
                buttonYNHeight * 0.5f, buttonYNHeight * 0.5f,
                ContextCompat.getColor(getContext(), R.color.colorButtonB),
                "取消", ContextCompat.getColor(getContext(), R.color.colorText));

        housesL = new GameImage[4];
        housesL[0] = new GameImage(getInnerRectF(squares[1].getRectF(), 0.88f), R.drawable.house_level0);
        housesL[1] = new GameImage(getInnerRectF(squares[1].getRectF(), 0.88f), R.drawable.house_level1);
        housesL[2] = new GameImage(getInnerRectF(squares[1].getRectF(), 0.88f), R.drawable.house_level2);
        housesL[3] = new GameImage(getInnerRectF(squares[1].getRectF(), 0.88f), R.drawable.house_level3);
        housesU = new GameImage[4];
        housesU[0] = new GameImage(getInnerRectF(squares[squareNum - 1].getRectF(), 0.66f), R.drawable.house_level0);
        housesU[1] = new GameImage(getInnerRectF(squares[squareNum - 1].getRectF(), 0.66f), R.drawable.house_level1);
        housesU[2] = new GameImage(getInnerRectF(squares[squareNum - 1].getRectF(), 0.66f), R.drawable.house_level2);
        housesU[3] = new GameImage(getInnerRectF(squares[squareNum - 1].getRectF(), 0.66f), R.drawable.house_level3);

        squareImages = new GameImage[squareNum];
        for(int i = 0; i < squareNum; ++i){
            if(i == 0){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.66f),
                        R.drawable.start_point);
            }
            else if(i == squareLNum + 1){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.66f),
                        R.drawable.airfield);
            }
            else if(i == squareNum / 2){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.66f),
                        R.drawable.prison);
            }
            else if(i == squareNum - squareUNum - 1){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.66f),
                        R.drawable.cappuccino);
            }
            else if(i == (squareLNum + 1) / 2
                    || i == squareNum / 2 + (squareLNum + 1) / 2){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.88f),
                        R.drawable.random_event);
            }
            else if(i == squareLNum + 1 + (squareUNum + 1) / 2
                    || i == squareNum - (squareUNum + 1) / 2){
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), 0.66f),
                        R.drawable.random_event);
            }
            else{
                squareImages[i] = new GameImage(getInnerRectF(squares[i].getRectF(), isSquareL(i) ? 0.88f : 0.66f),
                        R.drawable.house_level0);
            }
        }

        money = new GameImage[playerNum];
        for(int i = 0; i < playerNum; ++i){
            money[i] = new GameImage(getInnerRectF(datas[i].getPosX() + datas[i].getWidth() * 0.4f,
                    datas[i].getPosY(), datas[i].getWidth() * 0.25f, datas[i].getHeight() * 0.66f, 0.8f),
                    R.drawable.money);
        }

        startPointDesc = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.start_point_desc);
        airfieldDesc = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.airfield_desc);
        prisonDesc = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.prison_desc);
        cappuccinoDesc = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.cappuccino_desc);
        randomEventDesc = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.random_event_desc);
        houseDesc = new GameImage[4];
        houseDesc[0] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.house_level0_desc);
        houseDesc[1] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.house_level1_desc);
        houseDesc[2] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.house_level2_desc);
        houseDesc[3] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.house_level3_desc);

        eventMsgManager = new EventMsgManager(eventWnd.getPosX() + eventWnd.getWidth() * 0.1f,
                eventWnd.getPosY() + eventWnd.getHeight() * 0.7f / 7.0f * 2.0f,
                eventWnd.getHeight() * 0.7f / 7.0f);

        propCards = new GameImage[totCardNum];
        RectF rect = getInnerRectF(datas[0].getPosX() + datas[0].getWidth() * 0.4f,
                datas[0].getPosY() + datas[0].getHeight() * 0.66f,
                datas[0].getWidth() * 0.2f, datas[0].getHeight() * 0.34f, 0.8f);
        propCards[0] = new GameImage(rect, R.drawable.transposition_card);
        propCards[1] = new GameImage(rect, R.drawable.turtle_card);
        propCards[2] = new GameImage(rect, R.drawable.stay_card);
        propCards[3] = new GameImage(rect, R.drawable.building_card);
        propCards[4] = new GameImage(rect, R.drawable.free_card);

        cardDesc = new GameImage[totCardNum];
        cardDesc[0] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.transposition_card_desc);
        cardDesc[1] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.turtle_card_desc);
        cardDesc[2] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.stay_card_desc);
        cardDesc[3] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.building_card_desc);
        cardDesc[4] = new GameImage(getInnerRectF(eventWnd.getRectF(), 1.0f), R.drawable.free_card_desc);

        // 画笔参数设置
        paintFill.setAntiAlias(true);
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paintFill.setTextAlign(Paint.Align.CENTER);
        paintFill.setFakeBoldText(true);

        paintStroke.setAntiAlias(true);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        paintAlpha.setAlpha(188);

        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.colorText));
        paintText.setTextAlign(Paint.Align.LEFT);
        paintText.setFakeBoldText(true);
    }

    // 绘制静态背景
    private void createBackground(){
        Log.d(TAG, "createBackground");
        backgroundBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(backgroundBitmap);
        header.draw(backgroundCanvas, paintFill);
        board.draw(backgroundCanvas, paintFill);
        body.draw(backgroundCanvas, paintFill);
        eventWnd.draw(backgroundCanvas, paintFill);
        buttonBack.draw(backgroundCanvas, paintFill);
        for(int i = 0; i < playerNum; ++i){
            datas[i].draw(backgroundCanvas, paintFill);
            playersBig[i].draw(backgroundCanvas, null);
            money[i].draw(backgroundCanvas, paintAlpha);
        }
        for(int i = 0; i < squareNum; ++i){
            drawBound(backgroundCanvas, squares[i], 5);
            if(!(monopolyGameLogic.getBoard().getSquare(i) instanceof House)){
                squareImages[i].draw(backgroundCanvas, paintAlpha);
            }
        }
        drawName(backgroundCanvas);
    }

    // 玩家名称绘制
    private void drawName(Canvas canvas){
        for(int i = 0; i < playerNum; ++i){
            String name = monopolyGameLogic.getPlayer(i).getName();
            float size = Math.min(datas[i].getHeight() * 0.33f, datas[i].getWidth() * 0.33f / name.length()) * 0.8f;
            float x = playersBig[i].getPosX() + datas[i].getWidth() * 0.06f;
            float y = datas[i].getPosY() + datas[i].getHeight() - datas[i].getHeight() * 0.1f;
            paintText.setTextSize(size);
            canvas.drawText(name, x, y, paintText);
        }
    }

    // 获取<图形类型, 附加编号>
    public Pair<ViewType, Integer> getViewType(float x, float y){
        if(buttonDiceS[0].isLocated(x, y)){
            return Pair.create(ViewType.BUTTON_DICE, -1);
        }
        else if(buttonY[0].isLocated(x, y)){
            return Pair.create(ViewType.BUTTON_Y, -1);
        }
        else if(buttonN[0].isLocated(x, y)){
            return Pair.create(ViewType.BUTTON_N, -1);
        }
        else if(buttonBack.isLocated(x, y)){
            return Pair.create(ViewType.BUTTON_BACK, -1);
        }
        for(int i = 0; i < squareNum; ++i){
            if(squares[i].isLocated(x, y)){
                return Pair.create(ViewType.SQUARE, i);
            }
        }
        for(int i = 0; i < playerNum; ++i){
            if(playersBig[i].isLocated(x, y)){
                return Pair.create(ViewType.PLAYER, i);
            }
            float posX = datas[i].getPosX() + datas[i].getWidth() * 0.4f;
            float posY = datas[i].getPosY() + datas[i].getHeight() * 0.66f;
            for(int j = 0; j < playerCardNum; ++j){
                propCards[0].setPosX(posX);
                propCards[0].setPosY(posY);
                if(propCards[0].isLocated(x, y)){
                    return Pair.create(ViewType.PROP_CARD, i * playerCardNum + j);
                }
                posX += datas[i].getWidth() * 0.2f;
            }
        }
        return Pair.create(ViewType.DEFAULT, -1);
    }

    private boolean isSquareL(int id){
        return id <= squareLNum || id > squareLNum + squareUNum + 2 && id < squareNum - squareUNum - 1;
    }

    private Bitmap getHouseBitmap(int id, int level){
        if(isSquareL(id)){
            return housesL[level].getBitmap();
        }
        else{
            return housesU[level].getBitmap();
        }
    }

    // 图像居中适应，占据比例为k
    public RectF getInnerRectF(RectF rectF, float k){
        float s = Math.min(rectF.width(), rectF.height()) * k;
        float x = rectF.centerX() - s * 0.5f, y = rectF.centerY() - s * 0.5f;
        return new RectF(x, y, x + s, y + s);
    }

    public RectF getInnerRectF(float x, float y, float w, float h, float k){
        return getInnerRectF(new RectF(x, y, x + w, y + h), k);
    }

    // colorS -> colorE 渐变中间色计算
    public int getGraColor(int colorS, int colorE, int step, int tot){
        int colorRet = colorS & 0XFF000000; // 透明度不作计算
        int dtR = (colorS >> 16 & 0XFF) + ((colorE >> 16 & 0XFF) - (colorS >> 16 & 0XFF)) * step / (tot - 1);
        int dtG = (colorS >> 8 & 0XFF) + ((colorE >> 8 & 0XFF) - (colorS >> 8 & 0XFF)) * step / (tot - 1);
        int dtB = (colorS & 0XFF) + ((colorE & 0XFF) - (colorS & 0XFF)) * step / (tot - 1);
        return colorRet | (dtR << 16) | (dtG << 8) | dtB;
    }

    public void setSquareSelect(int id){
        squareSelect = id;
    }

    public int getSquareSelect(){
        return squareSelect;
    }

    public void setPlayerSelect(int id){
        playerSelect = id;
    }

    public int getPlayerSelect(){
        return playerSelect;
    }

    public void setCardSelect(int id){
        cardSelect = id;
    }

    public int getCardSelect(){
        return cardSelect;
    }

    public ArrayList<String> getMsgList(){
        return eventMsgManager.getMsgList();
    }
}
