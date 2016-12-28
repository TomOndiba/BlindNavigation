package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/*import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;*/


/**
 * Created by Triste on 2016/10/13.
 */


public class UploadRouteActivity extends Activity implements LocationSource,
        AMapLocationListener {

    private LatLng myPosition=null;
    public Button btn_addloc=null;
    public Button btn_light=null;
    public Button btn_crossing=null;
    public Button btn_upload = null;
    public static TextView text_info=null;

    private AMap aMap=null;
    private MapView mapView=null;
    private OnLocationChangedListener mListener;
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;

    final HTTP http = new HTTP();
    private PointsList list = new PointsList();
    private PointsList crossingList = new PointsList();
    private PointsList lightList = new PointsList();
    private Context mContext=null;
    public static boolean isLocChanged=true;
    private LinearLayout layout = null;
    private boolean []btn_index={true,true,true};
    private boolean isStart=false;//是否开始设置路线
    int locChangeNum=0;
    int directChangeNum=0;
    public static List<String> listInfo=null;
    final static private int angle = 10;
    int a=-1;

    final int INTERVAL=1000;
    private int sec = 0;
    private boolean isStartTime = false;

    int bearing = 0;
    int startAngle = -1;

    private WalkstepUtil Walkstep = null;
    SensorManager mSensorManager;

    private LinearLayout linearStartEnd = null;
    private LinearLayout linearUpload = null;
    private LinearLayout linearStep = null;

    private boolean isConChange =false;//是否连续转弯
    private List<Point> conChangeList = new ArrayList<>();

    int stepLength = 75;

    boolean isSetlastChangedDirect = false;
    int lastChangedDirect  = -1;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                listInfo.set(4,"当前走了: "+Walkstep.getStep()+"步，长度："+Walkstep.getStep()*stepLength/100+"米.");
                //listInfo.set(4,"sec:"+(sec++));
                showListInfo();
            }
            super.handleMessage(msg);
        };
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            if(isStartTime){
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }
    };

    Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if(isStart&&!isSetlastChangedDirect){
                    lastChangedDirect = a;
                    isSetlastChangedDirect  = true;
                    ToastUtil.show(mContext,"开始检测方向");
                }
                //Log.d("wjctag", "in interval");
                // if(a!=-1&&(a-bearing)>=angle||(a-bearing)<=-angle) {
                //Log.d("wjctag", "in --------------");
                if(calAngleType(a)!=calAngleType(bearing)){
                    isConChange = true;
                    bearing = a;
                    if(isStart){
                        conChangeList.add(new Point(myPosition.latitude, myPosition.longitude, 3, bearing, Walkstep.getStep() * stepLength));
                    }
                }
                else{
                    isConChange = false;
                }

                if(isStart&&!isConChange&&conChangeList.size()!=0){//说明连续变结束
                    Log.d("wjctag", "point:" + conChangeList.get(conChangeList.size() - 1).ToString2());

                    if(getAbsoulteAngle(conChangeList.get(conChangeList.size() - 1).getDirect(),lastChangedDirect)>20){
                        list.addPoint(conChangeList.get(conChangeList.size() - 1));
                        listInfo.set(3, "检测到方向第"+ (++directChangeNum)+"次改变，已保存方向信息" );
                        lastChangedDirect = conChangeList.get(conChangeList.size() - 1).getDirect();
                    }
                    conChangeList.clear();

                    Log.d("wjctag", "current direct:" + a + " type:" + calAngleType(a));
                }


                //listInfo.set(2, "current direct:" + a + " type:"+ calAngleType(a));
                showListInfo();
            }
            super.handleMessage(msg);
        };
    };
    Timer timer1 = new Timer();
    TimerTask task1 = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            if(isStart){
                Message message = new Message();
                message.what = 1;
                handler1.sendMessage(message);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_uploadroute);

        mContext=this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.uploadmap);
        mapView.onCreate(savedInstanceState);
        btn_addloc = (Button)findViewById(R.id.btn_addloc);
        btn_crossing= (Button)findViewById(R.id.btn_crossing);
        btn_light = (Button)findViewById(R.id.btn_light);
        btn_upload = (Button)findViewById(R.id.btn_upload);
        layout = (LinearLayout)findViewById(R.id.layout_btn);
        text_info=(TextView)findViewById(R.id.text_info);
        listInfo=new ArrayList<String>(5);//0位置 1改没改变 2方向
        listInfo.add("");
        listInfo.add("");
        listInfo.add("");
        listInfo.add("");
        listInfo.add("");

        linearStartEnd = (LinearLayout)findViewById(R.id.linearStartEnd);
        linearUpload = (LinearLayout)findViewById(R.id.linearUpload);
        linearStep = (LinearLayout)findViewById(R.id.linearStep);
        linearStartEnd.setVisibility(View.GONE);
        linearUpload.setVisibility(View.GONE);

        Button btn_step = (Button)findViewById(R.id.stepButton);
        btn_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = ((EditText)findViewById(R.id.editText_steplength)).getText().toString();
                if(s.equals(""))
                    s = "75";
                int length = Integer.parseInt(s);
                if(length<30){
                    ToastUtil.show(mContext,"步长设置过短，请检查");
                }
                else if(length>110){
                    ToastUtil.show(mContext,"步长设置过长，请检查");
                }
                else{
                    stepLength = length;
                    linearStep.setVisibility(View.GONE);
                    linearUpload.setVisibility(View.VISIBLE);
                    if(aMap!=null)
                        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
                }
            }
        });

        Button btn_confirm = (Button)findViewById(R.id.confirmButton);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = ((EditText)findViewById(R.id.editText_startPos1)).getText().toString();
                String end = ((EditText)findViewById(R.id.editText_endPos1)).getText().toString();
               if(!(start.equals("")&&end.equals(""))){

                   Log.d("wjctag","size:"+list.list.size());
                   http.uploadRoute(list, crossingList, lightList, start, end);
                   ToastUtil.show(mContext, "上传成功.");

                   list.list.clear();
                   crossingList.list.clear();
                   lightList.list.clear();
                   btn_index[0]=!btn_index[0];
                   setBtn();
                   isStart=false;
                   directChangeNum=0;
                   locChangeNum=0;
                   startAngle=-1;

                   linearStartEnd.setVisibility(View.GONE);
                   linearUpload.setVisibility(View.VISIBLE);
               }
                else{
                   ToastUtil.show(mContext,"请输入起点或终点");
               }
            }
        });

        mSensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        SensorEventListener directListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                a = (int)event.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerListener(directListener,
                mOrientation, SensorManager.SENSOR_DELAY_GAME);

        Walkstep = WalkstepUtil.getInstance(mSensorManager);

        LatLng p1 = new LatLng(31.883664,118.819055);
        LatLng p2 = new LatLng(31.883745,118.819083);
        float d=AMapUtils.calculateLineDistance(p1,p2);
        Log.d("wjctag", d + "");

        setBtn();

        if(!init()){
            Log.d("wjctag","upload init fail!!!");
        }

        timer.schedule(task, 200, 200); // 1s后执行task,经过1s再次执行
        timer1.schedule(task1, 4000, 800);

    }



    public boolean init(){
        initMap();
        initBtn();

        return true;
    }



    public void initMap(){

        if (aMap == null) {
            aMap = mapView.getMap();
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
            myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
            // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
            myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
        }

    }

    public void initBtn(){

        btn_addloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btn_index[0] = false;
                setBtn();
                isStart = true;
                isStartTime = true;

                bearing = a;
                Walkstep.clearStep();
                Walkstep.setIsStart(true);
                startAngle = a;
                list.addPointAnyway(myPosition, 0, bearing, 0);

            }
        });


        btn_crossing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isStart) {
                    list.addPoint(myPosition, 1, bearing, Walkstep.getStep() * stepLength);
                    crossingList.addPoint(myPosition);
                    btn_index[1] = !btn_index[1];
                    setBtn();
                }
            }
        });


        btn_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isStart){
                    list.addPoint(myPosition, 2,bearing,Walkstep.getStep()*stepLength);
                    lightList.addPoint(myPosition);
                    Log.d("wjctag", btn_light.getText().toString());
                    btn_index[2]=!btn_index[2];
                    setBtn();
                }

            }
        });


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = false;
                if (isLocChanged) ;
                if (list.list.size() < 3){
                    list.list.clear();
                    crossingList.list.clear();
                    lightList.list.clear();
                    btn_index[0]=!btn_index[0];
                    setBtn();
                    isStart=false;
                    directChangeNum=0;
                    locChangeNum=0;
                    startAngle=-1;
                    ToastUtil.show(mContext, "路线信息太少,上传失败");
                }

                else {
                    if (!(btn_index[1] && btn_index[2])){
                        list.list.clear();
                        crossingList.list.clear();
                        lightList.list.clear();
                        btn_index[0]=!btn_index[0];
                        setBtn();
                        isStart=false;
                        directChangeNum=0;
                        locChangeNum=0;
                        startAngle=-1;
                        ToastUtil.show(mContext, "路口或红绿灯未添加终点，上传失败");

                    }

                    else {
                        list.addPoint(myPosition, 0, bearing, Walkstep.getStep()*stepLength);
                        ToastUtil.show(mContext, "正在保存终点");
                        linearUpload.setVisibility(View.GONE);
                        linearStartEnd.setVisibility(View.VISIBLE);

                    }
                }
                //http.getRoute("","");

            }
        });

        layout.setVisibility(View.GONE);
        //setBtn(false);
    }

    public void setBtn(){
        if(btn_index[0]){
            btn_addloc.setText("设置起点");
            btn_addloc.setEnabled(true);
        }

        else{
            btn_addloc.setText("已经设置起点");
            btn_addloc.setEnabled(false);
        }

        if(btn_index[1])
            btn_crossing.setText("添加路口起点");
        else
            btn_crossing.setText("添加路口终点");
        if(btn_index[2])
            btn_light.setText("添加红绿灯起点");
        else
            btn_light.setText("添加红绿灯终点");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        list.list.clear();
        crossingList.list.clear();
        lightList.list.clear();
        isLocChanged=true;
         btn_index[0]=btn_index[1]=btn_index[2]=true;
        setBtn();
        isStart=false;//是否开始设置路线
        locChangeNum=0;
        directChangeNum=0;
        for(int i=0;i<listInfo.size();++i){
            listInfo.set(0,"");
        }
        showListInfo();

        a=-1;

        isStartTime = false;
        bearing = 0;
        startAngle = -1;

        isConChange =false;//是否连续转弯
        conChangeList.clear();
        //timer.schedule(task, 200, 200); // 1s后执行task,经过1s再次执行
        //timer1.schedule(task1, 800, 800);
    }

    @Override
    protected void onStop(){
         super.onStop();
        //timer.cancel();
        //timer1.cancel();
        Walkstep.clearStep();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Walkstep.Destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(AMapLocation loc) {

      // }
        if (mListener != null && loc != null) {
            if (loc != null
                    && loc.getErrorCode() == 0) {
                mListener.onLocationChanged(loc);// 显示系统小蓝点
                if(myPosition==null){
                    Log.d("wjctag", "move camera");
                    myPosition=new LatLng(loc.getLatitude(),loc.getLongitude());
                    Log.d("wjctag", "position:" + myPosition.latitude + " " + myPosition.longitude);
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(myPosition));
                    aMap.moveCamera(CameraUpdateFactory.zoomBy(10.0f));
                    /*aMap.addMarker(new MarkerOptions().anchor(0.5f, 1.0f)
                            .position(myPosition).title("myPosition")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true));*/
                }
                else{
                    if(!(myPosition.latitude==loc.getLatitude()&&myPosition.longitude==loc.getLongitude()))
                    {
                        isLocChanged=true;
                        myPosition=new LatLng(loc.getLatitude(),loc.getLongitude());
                        //listInfo.set(1,"位置改变"+(++locChangeNum));
                    }
                    else{
                        isLocChanged=true;
                        //isLocChanged=false;
                        //listInfo.set(1,"位置没改变");
                    }
                    showListInfo();
                }

                String s = loc.getLatitude()+"";
                String s1 = loc.getLongitude()+"";
                /*Log.d("wjctag",s.length()+"  s");
                Log.d("wjctag",s1.length()+"  s1");*/
                //if(s.length()>9&&s1.length()>10)
                layout.setVisibility(View.VISIBLE);
                   // setBtn(true);

                //Log.d("wjctag",loc.getLocationType()+" "+loc.getAccuracy());


                //if(loc.getLatitude()!=myPosition.latitude||loc.getLongitude()!=myPosition.longitude)
                //Log.d("wjctag", "位置:" + loc.getLatitude() + "," + loc.getLongitude() + " " + loc.getLocationType() + " " + loc.getAccuracy()+"方向："+bearing);

                //listInfo.set(0, "位置:" + loc.getLatitude() + "," + loc.getLongitude() + " " + loc.getLocationType() + " " + loc.getAccuracy());
                showListInfo();
            } else {
                String errText = "定位失败," + loc.getErrorCode() + ": " + loc.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2500);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();

        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

/*
    @Override
    public void onSensorChanged(SensorEvent event) {
         a = (int)event.values[0];
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }*/

    public int calAngleType(int a){
        a = (a-startAngle+360)%360;
        int type=((a+15)/30)%12;
        //Log.d("wjctag",a+" type:"+type);
        return type;
    }

    public static void showListInfo(){
        String s="";
        for(int i=0;i<4;++i){
            if(!listInfo.get(i).equals(""))
                s+=listInfo.get(i)+"\n";
        }
        s+=listInfo.get(4);
        text_info.setText(s);
    }

    public static int getAbsoulteAngle(int a,int b){
        int  x = (a-b+360)%360;
        int xx = 0;
        if(x>180)
            xx =360-xx;
        else
            xx=x;

        return xx;
    }

}


