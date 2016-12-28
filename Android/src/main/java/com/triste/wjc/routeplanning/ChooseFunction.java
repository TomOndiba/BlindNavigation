package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Triste on 2016/9/9.
 */
public class ChooseFunction extends Activity {

    private RelativeLayout relativeLayout1=null;
    private RelativeLayout relativeLayout2=null;
    SpeechTip speechTip=null;
    String TAG="wjctag";

    MySocket mySocket = null;

    Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {//计时
                timer1.cancel();
                speechTip.speakSlow("进入选择功能界面");
                Log.d("wjctag", "in handle");
            }

            super.handleMessage(msg);
        };
    };
    Timer timer1 = new Timer();
    TimerTask timeTask1 = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息`
            Message message = new Message();
            message.what = 1;
            handler1.sendMessage(message);
            Log.d("wjctag","in time");

        }
    };

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();

                    if (x > 0) {//RIGHT
                        Log.d("wjctag", "RIGHT");
                    }
                    else if (x < 0) {
                        Log.d("wjctag","LEFT");
                        Intent intent=new Intent();
                        intent.setClass(ChooseFunction.this, BaseActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
            };
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_choosefunction);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String sss="-1";
        int ttt = Integer.parseInt(sss);
        Log.d("wjctag","+:"+ttt);

        speechTip=SpeechTip.getInstance(this.getApplicationContext());

        mySocket=MySocket.getInstance(this.getApplication());

        relativeLayout1=(RelativeLayout)findViewById(R.id.relative1);
        relativeLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ChooseFunction.this, ChooseUploadMode.class);
                startActivity(intent);
            }
        });

        relativeLayout2=(RelativeLayout)findViewById(R.id.relative2);
        relativeLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ChooseFunction.this, SetNaviInfoActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btn_route=(ImageButton)findViewById(R.id.btn_route);
        btn_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNaviInfoActivity.mySocket = mySocket;
                Intent intent=new Intent();
                intent.setClass(ChooseFunction.this, ChooseUploadMode.class);
                startActivity(intent);
            }
        });

        ImageButton btn_navi=(ImageButton)findViewById(R.id.btn_navi);
        btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ChooseFunction.this, SetNaviInfoActivity.class);
                startActivity(intent);
            }
        });

        timer1.schedule(timeTask1, 500, 2000);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("wjctag", "restart");
        if(speechTip==null)
            speechTip = SpeechTip.getInstance(this.getApplicationContext());
        speechTip.speakSlow("返回选择功能界面");

    }

    @Override
    protected void onStop() {
        super.onStop();
        speechTip.release();
        speechTip = null;

        Log.d("wjctag","choose func stop");
    }
}
