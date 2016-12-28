package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Triste on 2016/9/11.
 */
public class SwitchRoute extends Activity implements View.OnTouchListener,
        GestureDetector.OnGestureListener {
    SpeechTip speechTip=null;
    TextView textView_showroute=null;
    ReadWriteIndex rw=null;
    RelativeLayout enter_route=null;
    String startName="";
    String endName="";
    GestureDetector mGestureDetector;

    private static final int FLING_MIN_DISTANCE = 250;
    private static final int FLING_MIN_VELOCITY = 0;

    public static int index=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGestureDetector = new GestureDetector(this);
        speechTip=SpeechTip.getInstance(getApplication());
        speechTip.speakSlow("进入选择路线界面");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_switchroute);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textView_showroute=(TextView)findViewById(R.id.textView_showroute);

        enter_route=(RelativeLayout)findViewById(R.id.enter_route);
        enter_route.setOnTouchListener(this);
        enter_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        rw=new ReadWriteIndex();
        rw.readIndex();
        rw.Print();
        if(rw.list.size()==0){
            textView_showroute.setText("当前未设定路线");
            speechTip.speakSlow("当前未设定路线");
        }
        else{
            textView_showroute.setText("路线"+index+"\n"+"起点："
                    +rw.list.get(index-1).startName+"\n"+"终点："+rw.list.get(index-1).endName);
            //speechTip.speak("路线" + index);
            startName=rw.list.get(index-1).startName;
            endName=rw.list.get(index-1).endName;
            speechTip.speakSlow("路线" + index);
            speechTip.speakSlow("从" + startName + "到" + endName);
        }



    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            Log.d("wjctag","left");
            if(index<=rw.list.size()&&index>0){
                String fileName=rw.list.get(index-1).fileName;
                Intent intent = new Intent();
                //intent.putExtra("latitude", currentMarker.getPosition().latitude);
                intent.putExtra("file",fileName);
                intent.setClass(SwitchRoute.this, BaseActivity.class);
                startActivity(intent);
            }
        } else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            Log.d("wjctag","right");
            // Fling right
            //Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
        }
        else if(e2.getY()-e1.getY()>FLING_MIN_DISTANCE){
            Log.d("wjctag","down");
            --index;
            if(index>0){
                textView_showroute.setText("路线"+index+"\n"+"起点："
                        +rw.list.get(index-1).startName+"\n"+"终点："+rw.list.get(index-1).endName);
                startName=rw.list.get(index-1).startName;
                endName=rw.list.get(index-1).endName;
                if(index==2){
                    speechTip.speakSlow("路线二");
                }
                else
                    speechTip.speakSlow("路线" + index);
                speechTip.speakSlow("从" + startName + "到" + endName);
            }
            else{
                ++index;
                speechTip.speakSlow("已经是第一条路线");
            }
        }
        else if(e1.getY()-e2.getY()>FLING_MIN_DISTANCE){

            Log.d("wjctag","up");
            ++index;
            if(index<=rw.list.size()){
                textView_showroute.setText("路线"+index+"\n"+"起点："
                        +rw.list.get(index-1).startName+"\n"+"终点："+rw.list.get(index-1).endName);
                startName=rw.list.get(index-1).startName;
                endName=rw.list.get(index-1).endName;
                if(index==2){
                    speechTip.speakSlow("路线二");
                }
                else
                    speechTip.speakSlow("路线" + index);
                speechTip.speakSlow("从" + startName + "到" + endName);
            }
            else{
                --index;
                speechTip.speakSlow("已经是最后一条路线");
            }
            //Toast.makeText(this, "向上手势", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("wjctag", "uploadmode restart");
        if(speechTip==null){
            speechTip=SpeechTip.getInstance(getApplicationContext());
        }
        speechTip.speak("返回选择路线界面");
        if(rw.list.size()==0){
            textView_showroute.setText("当前未设定路线");
            speechTip.speakSlow("当前未设定路线");
        }
        else{
            textView_showroute.setText("路线"+index+"\n"+"起点："
                    +rw.list.get(index-1).startName+"\n"+"终点："+rw.list.get(index-1).endName);
            //speechTip.speak("路线" + index);
            startName=rw.list.get(index-1).startName;
            endName=rw.list.get(index-1).endName;
            speechTip.speakSlow("路线" + index);
            speechTip.speakSlow("从" + startName + "到" + endName);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        speechTip.release();
        speechTip = null;
    }



}
