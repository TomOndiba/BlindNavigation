package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Triste on 2016/11/2.
 */
public class SetNaviInfoActivity extends Activity{

    SpeechTip speechTip=null;
    protected static final int RESULT_SPEECH = 1;
    TextView txtText=null;
    TextView txtText1 = null;
    protected static final int INPUT_MODE = 0;
    protected static final int INPUT_START = 1;
    protected static final int INPUT_END = 2;
    protected static final int START_NAVI = 3;
    protected static final int NAVI = 4;
    private int sign = -1;
    private String start =  "";
    private String end = "";
    int naviMode = -1;
    public static boolean nowIsRed = false;

    public static MySocket mySocket = null;


    private TextView textView_info=null;
    HTTP http=null;
    static String points = "";
    static String crossings =  "";
    static String lights =  "";
    static String startName =  "";
    static String endName = "";
    static String startPos =  "";
    static String endPos =  "";
    static String middlePos =  "";
    static int radius =  -1;
    private static PointsList route=null;
    private int currentDirect = -1;

    public  static boolean isStartNavi=false;//是否点击开始导航按钮
    private boolean isInNavi = true;
    //private int step = 0;
    private int tipIndex=1;
    private int stepLength = 42;
    private WalkstepUtil Walkstep;
    SensorManager mSensorManager = null;

    private int zouPianCnt = 0;

    public static int plusStepofObstacle = 0;

    Handler handler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {//计时
                if(isStartNavi)
                {
                    Log.d("wjctag","curr:"+currentDirect+"  point.curr:"+PointsList.currentDirect);
                    int  x = (currentDirect-PointsList.currentDirect+360)%360;
                    int xx = 0;
                    if(x>180)
                        xx =360-xx;
                    else
                    xx=x;
                    if(x>30){
                        ++zouPianCnt;
                        if(zouPianCnt>=5&&speechTip!=null){
                            Log.d("wjctag",PointsList.currentDirect+""+currentDirect);
                            speechTip.speakQueue(PointsList.getTipByAngles1(PointsList.currentDirect, currentDirect));
                            zouPianCnt = 0;
                        }

                    }

                }

            }

            super.handleMessage(msg);
        };
    };
    Timer timer2 = new Timer();
    TimerTask timeTask2 = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息`
            Message message = new Message();
            message.what = 1;
            handler2.sendMessage(message);
            Log.d("wjctag", "in time2222222");

        }
    };

    Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {//计时
                timer1.cancel();
                speechTip.speakSlow("请选择导航模式，一精准模式，二模糊模式");
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
            Log.d("wjctag", "in time");

        }
    };

    int sayLight = 0;

    Handler handler4 = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {//计时
                /*++sayLight;
                if(sayLight<=4)
                    speechTip.speak("现在是红灯");
                else{
                    timer4.cancel();
                    speechTip.speak("可以通过红绿灯");
                }*/
                timer4.cancel();
                speechTip.speak("可以通过红绿灯");
                Log.d("wjctag", "in handle");
            }

            super.handleMessage(msg);
        };
    };
    Timer timer4 = new Timer();
    TimerTask timeTask4 = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息`
            Message message = new Message();
            message.what = 1;
            handler4.sendMessage(message);
            Log.d("wjctag","in time");

        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {//计时
                MySocket.sendString("1");
                //Log.d("wjctag", "send 1");
                /*if(nowIsRed){
                    Log.d("wjctag","send 0");
                    MySocket.sendString("0");
                }
                else{
                    MySocket.sendString("1");
                    Log.d("wjctag", "send 1");
                }*/
                txtText.setText("当前走了:"+Walkstep.getStep()+"步，下次指令在:"+(route.list.get(tipIndex).getLength()/stepLength-1)+"步");//plusStepofObstacle
                if(tipIndex!=0&&tipIndex<=route.list.size()-1&&
                        Walkstep.getStep()>=(((route.list.get(tipIndex).getLength()/stepLength))-1)//+plusStepofObstacle
                        &&speechTip!=null ){

                    Log.d("wjctag", "injjjjjjj");
                    String tip =route.getNextTip(currentDirect);
                    //txtText1.setText(tip);
                    if(tip.equals("前方有红绿灯")){
                        timer4.schedule(timeTask4, 9000, 2000000);

                        MySocket.sendString("0");
                        nowIsRed = true;
                    }
                    if(tip.equals("红绿灯结束")){

                        MySocket.sendString("1");
                        nowIsRed = false;
                    }



                    speechTip.speak(tip);
                    ++tipIndex;
                    if(tipIndex>=route.list.size()) {
                        timer.cancel();
                        timer2.cancel();
                        isStartNavi  =false;
                    }
                }
            }
            if(msg.what==2){

            }
            super.handleMessage(msg);
        }
    };
    Timer timer = new Timer();
    TimerTask timeTask = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            if(isInNavi){
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("wjctag", "setnavi create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setnaviinfo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        speechTip = SpeechTip.getInstance(this.getApplicationContext());

        txtText = (TextView)findViewById(R.id.textView_speak);
        txtText1 = (TextView)findViewById(R.id.textView_txt1);

        sign = 0;
        naviMode = -1;
/*        sign = 3;
        naviMode = 1;*/

        ImageView v = (ImageView)findViewById(R.id.imageView_speak);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*                Log.d("wjctag","click");
                MySocket.sendString("0");*/



                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    txtText.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

        timer1.schedule(timeTask1, 500, 2000);


        http = new HTTP();

        mSensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int a = (int)event.values[0];
                if(currentDirect-a>15||currentDirect-a<-15){
                    currentDirect=a;

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerListener(listener,
                mOrientation, SensorManager.SENSOR_DELAY_GAME);

        Walkstep = WalkstepUtil.getInstance(mSensorManager);

        textView_info = (TextView)findViewById(R.id.textView_info);




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String s = text.get(0).split("。")[0];
                    txtText.setText(s);
                    switch (sign){
                        case INPUT_MODE:

                            if(s.equals("一")){
                                naviMode = 1;
                                ++sign;
                                speechTip.speakSlow("请输入起点");
                            }
                            else if(s.equals("二")){
                                naviMode = 2;
                                ++sign;
                                Intent intent=new Intent();
                                intent.setClass(SetNaviInfoActivity.this, SwitchRoute.class);
                                startActivity(intent);
                            }
                            else{
                                speechTip.speakSlow("我没有听清，请再说一遍");
                            }
                            break;
                        case INPUT_START:
                            start = s;
                            speechTip.speakSlow("请输入终点");
                            ++sign;
                            break;
                        case INPUT_END:
                            end = s;
                            speechTip.speakSlow("是否从" + start + "到" + end);
                            ++sign;
                            break;
                        case START_NAVI:
                            //s="是";
                            if(s.equals("是")){
                                if(naviMode==1){


                                    http.getRoute(start, end);

                                    while(true){
                                        if(route!=null&&!isStartNavi){
                                            if(route.list.size()== 0){
                                                Log.d("wjctag","loop:"+route.list.size()+"");
                                                speechTip.speak("查询路线失败");
                                                break;
                                            }
                                            else{
                                                speechTip.speakSlow("准备开始导航");
                                                Log.d("wjctag","uhsuiha8i");
                                                isStartNavi=!isStartNavi;
                                                textView_info.setText(route.print());
                                                String ss = PointsList.getTipByAngles(route.list.get(0).getDirect(),currentDirect);
                                                //txtText.setText(ss);
                                                Walkstep.setIsStart(isStartNavi);

                                                timer.schedule(timeTask, 200, 200);
                                                timer2.schedule(timeTask2, 500, 1000);
                                                break;
                                            }
                                        }
                                    }


                                    ++sign;
                                }
                                else if(naviMode==2){
                                    Intent intent=new Intent();
                                    intent.setClass(SetNaviInfoActivity.this, SwitchRoute.class);
                                    startActivity(intent);
                                    ++sign;
                                }
                                else{
                                    speechTip.speakSlow("未知错误，请选择导航模式，一精准模式，二模糊模式");
                                    sign = INPUT_MODE;
                                }
                            }
                            else{
                                speechTip.speakSlow("请输入起点");
                                sign = INPUT_START;
                            }
                    }
                }
                break;
            }

        }
    }


    public static void handleRoute(String response){
        try {
           //
            Log.d("wjctag","oooooooooo");
            if(!response.equals("")){
                Log.d("wjctag","sssssss");
                JSONObject json = new JSONObject(response);
                if(json.getString("status").equals("success")){
                    points = json.getString("points");
                    crossings = json.getString("crossings");
                    lights = json.getString("lights");
                    startName = json.getString("startName");
                    endName = json.getString("endName");
                    startPos = json.getString("startPos");
                    endPos = json.getString("endPos");
                    middlePos = json.getString("middlePos");
                    radius = json.getInt("radius");
                    route = new PointsList(points);
                    route.initDirect();


                    Log.d("wjctag", points);
                    Log.d("wjctag", crossings);
                    Log.d("wjctag", lights);
                    Log.d("wjctag",startName);
                    Log.d("wjctag",endName);
                    Log.d("wjctag",startPos);
                    Log.d("wjctag",endPos);
                    Log.d("wjctag",middlePos);
                    Log.d("wjctag",radius+"");

                    Log.d("wjctag","success size:"+route.list.size()+"");
                    Log.d("wjctag","");
                    Log.d("wjctag","");
                }

                else{
                    route = new PointsList();
                    Log.d("wjctag","failed size:"+route.list.size()+"");
                }



            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {

        super.onResume();
        //speechTip.speak("请输入起点");
        Log.d("wjctag", "uploadmode resume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("wjctag", "uploadmode restart");
        if(speechTip==null){
            speechTip=SpeechTip.getInstance(getApplicationContext());
        }
        speechTip.speakSlow("返回选择导航模式界面，请选择导航模式，一精准模式，二模糊模式");
        sign = 0;
        naviMode = -1;
    }

    @Override
    protected void onStop() {
        super.onStop();
        speechTip.release();
        speechTip = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Walkstep.Destroy();
        timer.cancel();
        timer2.cancel();
    }
}
