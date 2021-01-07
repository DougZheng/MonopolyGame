package com.example.MonopolyGameV;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.os.Bundle;
import java.io.*;

public class MonopolyGameActivity extends Activity {

    private static MonopolyGameActivity monopolyGameActivity = null;
    private MonopolyGameView monopolyGameView = null;
    private int curLayout = R.layout.menu_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 全屏显示
        monopolyGameActivity = this;
        setMenu();
        Log.d("zbs", "new");
    }

    private void setMenu(){
        monopolyGameActivity.setContentView(R.layout.menu_layout);
        findViewById(R.id.start_game).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        getFile().delete();
                        setContentView(R.layout.game_layout);
                        monopolyGameView = findViewById(R.id.game_view);
                    default: break;
                }
                return true;
            }
        });
        findViewById(R.id.continue_game).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        setContentView(R.layout.game_layout);
                        monopolyGameView = findViewById(R.id.game_view);
                    default: break;
                }
                return true;
            }
        });
        findViewById(R.id.exit_game).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        System.exit(0);
                    default: break;
                }
                return true;
            }
        });
    }

    static public File getFile(){
        return new File(monopolyGameActivity.getFilesDir().getAbsolutePath() + "/gameData.bat");
    }

    static public void backToMenu(){
        Log.d("zbs", "menu_layout");
        getMonopolyGameView().getThread().setRunning(false);
        monopolyGameActivity.setMenu();
    }

    static public MonopolyGameView getMonopolyGameView(){
        return monopolyGameActivity.monopolyGameView;
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Log.d("zbs", "onSave");
//    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("zbs", "pause");
        if(curLayout == R.layout.game_layout){
            monopolyGameView.getThread().pause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("zbs", "resume");
        if(curLayout == R.layout.game_layout){
            monopolyGameView.getThread().unPause();
        }
    }

//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//        Log.d("zbs", "destroy");
//    }

    public static MonopolyGameActivity getMonopolyGameActivity(){
        return monopolyGameActivity;
    }
}