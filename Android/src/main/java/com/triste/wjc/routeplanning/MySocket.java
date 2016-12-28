package com.triste.wjc.routeplanning;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Triste on 2016/9/11.
 */
public class MySocket {

    Socket receiveSocket=null;
    Socket sendSocket = null;
    private boolean startFlag=false;
    String line="";
    String ip="115.28.147.142";//"192.168.3.25";//"172.16.0.207";//"117.136.75.80";//"172.20.10.4";//"223.3.101.28";//"172.20.10.4";//223.3.93.59
    int receivePort = 9888;
    int sendPort = 9777;
    private static MySocket mySocket=null;
    private static String sendString=null;
    private PrintWriter pw;
    Context mContext = null;

    public final static int NOTHING = -1;

    public final static int FRONT_OBSTACLE = 1;
    public final static int LEFT_OBSTACLE = 2;
    public final static int RIGHT_OBSTACLE = 3;

    public final static int UP_STAIR = 5;
    public final static int DOWN_STAIR = 6;
    public final static int CHEST_OBSTACLE = 7;
    public final static int WAIST_OBSTACLE = 8;
    public final static int KNEE_OBSTACLE = 9;
    public final static int END_STAIR = 0;

    public final static int RED_LIGHT = 100;
    public final static int PASS_RED = 101;
    public final static int KEEP_RIGHT = 102;

    String TAG="wjctag";
    int obstacleCount = 0;

    int nowTime = 0;
    int lastTime = 0;

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            ++nowTime;

        }
    };

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){

            String s="";
            switch(msg.what){
                case NOTHING:
                    break;
                case FRONT_OBSTACLE:
                    s = "前方障碍物";
                    break;
                case LEFT_OBSTACLE:
                    s = "左侧障碍物";
                    break;
                case RIGHT_OBSTACLE:
                    s = "右侧障碍物";
                    break;
                case UP_STAIR:
                    s = "向上台阶";
                    break;
                case DOWN_STAIR:
                    s = "向下台阶";
                    break;
                case CHEST_OBSTACLE:
                    s = "胸前有障碍物";
                    break;
                case WAIST_OBSTACLE:
                    s = "腰前障碍物";
                    break;
                case KNEE_OBSTACLE:
                    s = "膝盖前有障碍物";
                    break;
                case END_STAIR:
                    s = "台阶结束";
                    break;
                case RED_LIGHT:
                    s = "现在是红灯";
                    SetNaviInfoActivity.nowIsRed=true;
                    break;
                case PASS_RED:
                    s = "现在可以通过红绿灯";
                    SetNaviInfoActivity.nowIsRed=false;
                    break;
                case KEEP_RIGHT:
                    s = "请靠右行";
                    break;
                default:
                    s = "未知指令";
                    break;
            }

            int i = msg.what;
            if(i==7||i==8||i==9){
                ++obstacleCount;
                if(obstacleCount ==1){
                    lastTime = nowTime;
                    SpeechTip.speechTip.speak(s);
                    Log.d("wjctag","111111111");
                }
                else{
                   /* if(nowTime-lastTime>=1){
                        lastTime = nowTime;
                        SpeechTip.speechTip.speak(s);
                        Log.d("wjctag","444444444");
                    }*/
                    if(obstacleCount>=6){
                        obstacleCount = 0;
                        Log.d("wjctag","2222222222");
                        //SpeechTip.speechTip.speak(s);
                    }
                }
            }
            else{
                //obstacleCount = 0;
                Log.d("wjctag","33333333333");
                SpeechTip.speechTip.speak(s);
            }


            super.handleMessage(msg);
        }

    };


    private MySocket(){
        initSocket();
    }

    public static MySocket getInstance(Context c){
        if(mySocket==null){
            mySocket=new MySocket();
            mySocket.mContext =c;
            //mySocket.initSocket();
        }

        return mySocket;
    }

    public void setHandler(Handler h){
        mySocket.handler=h;
    }

    public boolean initSocket(){

        Thread tt = new Thread(new Thread() {
            @Override
            public void run() {
                try {
                    receiveSocket = new Socket(ip, receivePort);//     223.3.6.153  10.0.2.2 223.3.174.72
                    sendSocket = new Socket(ip,sendPort);
                    System.out.println("connect socket 127.0.0.1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startFlag = true;

            }
        });
        tt.start();

        Thread t = new Thread(
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            if (startFlag)
                                break;
                        }

                        BufferedReader reader = null;
                        try {
                            //把网络访问的代码放在这
                            reader = new BufferedReader(new InputStreamReader(receiveSocket
                                    .getInputStream(), "utf-8"));

                            OutputStream os = sendSocket.getOutputStream();
                            pw=new PrintWriter(os);

                            Log.d("wjctag","initSokcet success");
                        } catch (UnknownHostException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println("problem of connect socket:");
                            e.printStackTrace();
                            receiveSocket=null;
                            sendSocket=null;
                        }
                        
                        while (true) {
                            try {
                                if (reader != null) {
                                    if ((line = reader.readLine()) != null) {

                                        //Log.d(TAG, "from server msg:" + line);
                                        String []info = line.split(",");
                                        int info1 = Integer.parseInt(info[0]);
                                        int info2 = Integer.parseInt(info[1]);
                                        if(info1!=-1){
                                            if(info1==1||info1==7||info1==8||info1==9)
                                                if(SetNaviInfoActivity.isStartNavi)
                                                    ++SetNaviInfoActivity.plusStepofObstacle;

                                        }
                                        Message message1=new Message();
                                        message1.what=info1;
                                        if(handler!=null)
                                            handler.sendMessage(message1);
                                        else
                                            ToastUtil.show(mContext,"handler is empty");



                                        if(info2!=-1){
                                            info2+=100;
                                            Message message=new Message();
                                            message.what=info2;
                                            if(handler!=null)
                                                handler.sendMessage(message);
                                            else
                                                ToastUtil.show(mContext,"handler is empty");
                                        }

                                        if(info1!=-1)
                                            Log.d("wjctag","from server"+line);




                                        


                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                receiveSocket=null;
                            }
                        }

                    }
                }
        );
        t.start();

        return true;
    }

    public static void sendString(String s){
        if(mySocket.pw!=null){
            Log.d("wjctag","send:"+s);
            mySocket.pw.write(s);
            mySocket.pw.flush();
        }
    }



}
