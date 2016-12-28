package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Triste on 2016/9/8.
 */
public class BaseActivity extends Activity implements AMapNaviListener, AMapNaviViewListener{

    private static final String TAG = "wjctag";
    private SpeechTip mSpeechTip=null;

    List<Double> typeList=new ArrayList<Double>();


/*    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();

                    if (x > 0) {//RIGHT
                        Log.d("wjctag","RIGHT");
                        Intent intent=new Intent();
                        intent.setClass(BaseActivity.this, ChooseFunction.class);
                        startActivity(intent);

                    }
                    else if (x < 0) {
                        Log.d("wjctag","LEFT");
                        *//*if (mAMapNavi.isGpsReady())
                        {
                            ToastUtil.show(getApplicationContext(), "gps ready");
                            mAMapNavi.calculateWalkRoute(naviLatLngsList.get(index), naviLatLngsList.get(naviLatLngsList.size() - 1));
                            Log.d("wjctag", "gpsready");
                        }*//*
                    }
                    else if(y>0){
                        Log.d("wjctag","gucdyvygsgvxygs");
                    }
                    else if(y<0){

                    }
                    return true;
                }
            };
    private GestureDetector gestureDetector;*/



    protected AMapNaviView mAMapNaviView;
    protected AMapNavi mAMapNavi;
    protected TTSController mTtsManager;
    protected NaviLatLng mEndLatlng = new NaviLatLng(39.925846, 116.432765);
    protected NaviLatLng mStartLatlng = new NaviLatLng(39.925041, 116.437901);
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList;

    private List<NaviLatLng> naviLatLngsList = new ArrayList<NaviLatLng>();
    private List<LatLng> latLngsList = new ArrayList<LatLng>();
    private int index=0;
    private boolean isRemind=false;
    TextView text_gps=null;
    private boolean isGPSReady=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_basic_navi);

        mSpeechTip=SpeechTip.getInstance(getApplicationContext());
        
        //实例化语音引擎
        mTtsManager = TTSController.getInstance(getApplicationContext());
        mTtsManager.init();
        mTtsManager.startSpeaking();

        //MySocket.getInstance().setHandler(myHandler);
        //
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        mAMapNavi.addAMapNaviListener(mTtsManager);

        //设置模拟导航的行车速度
        mAMapNavi.setEmulatorNaviSpeed(30);
        if(mAMapNavi.startGPS()){
            ToastUtil.show(getApplicationContext(),"start gps");
        }
        sList.add(mStartLatlng);
        eList.add(mEndLatlng);

        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        try {
            Intent intent=getIntent();
            String DEFAULT_FILENAME=intent.getStringExtra("file");

            Log.v("wjctag", Environment.getExternalStorageDirectory().toString());
            Log.v("wjctag", Environment.getExternalStorageState());
            //String  = "test1.txt";
            String file = Environment.getExternalStorageDirectory() + "/" + ReadWriteIndex.DIR_NAME+"/"+DEFAULT_FILENAME;
            ReadOrWriteObject rOd = new ReadOrWriteObject(file);

            Log.d("wjctag", "--------------------");

            double d =0 ;
            double d1=0;
            double d2=0;
            rOd.openFile(ReadOrWriteObject.FileRead);
            int index=0;
            while(true){
                d = rOd.readDouble() ;
                if(d == Double.MIN_NORMAL){//读取文件的终止符,双精度最小值，在文件中已经关闭了相关的流
                    break;
                }
                d1=rOd.readDouble();
                if(d1==Double.MIN_NORMAL){
                    break;
                }
                d2=rOd.readDouble();
                if(d2==Double.MIN_NORMAL){
                    break;
                }
                else{
                    NaviLatLng naviLatLng=new NaviLatLng(d,d1);
                    LatLng latLng=new LatLng(d,d1);
                    latLngsList.add(latLng);
                    naviLatLngsList.add(naviLatLng);
                    typeList.add(d2);
                    Log.d("wjctag",d+" ");
                }
            }
            rOd.closeFile();
            rOd = null ;

        } catch (Exception e) {
            Log.v("EagleTag", "file　create　error");
        }

        for(int i=0;i<latLngsList.size();++i){
            Log.d("wjctag","point"+i+":"+latLngsList.get(i).latitude+","+latLngsList.get(i).longitude);
        }

        for(int i=0;i<latLngsList.size()-2;++i){
            Log.d("wjctag","distance:"+ AMapUtils.calculateLineDistance(latLngsList.get(i), latLngsList.get(i + 1)));
        }



       /* Button btn = (Button)findViewById(R.id.button_navi);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("wjctag", "1111111111111111111111111111111111111111111");

                if (mAMapNavi.isGpsReady()) {
                    ToastUtil.show(getApplicationContext(), "gps ready");
                    mAMapNavi.calculateWalkRoute(naviLatLngsList.get(index), naviLatLngsList.get(naviLatLngsList.size() - 1));
                    Log.d("wjctag", "gpsready");
                }

            }
        });*/

        text_gps=(TextView)findViewById(R.id.textView_gps);

        mSpeechTip.speakSlow("正在准备导航");

       // while (!isGPSReady)
        {
            //if(mAMapNavi.isGpsReady())
            {
                ToastUtil.show(getApplicationContext(), "gps ready");
                mAMapNavi.calculateWalkRoute(naviLatLngsList.get(index), naviLatLngsList.get(naviLatLngsList.size() - 1));
                Log.d("wjctag", "gpsready");
                text_gps.setVisibility(View.GONE);
            }

        }
        //gestureDetector = new GestureDetector(BaseActivity.this,onGestureListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();

//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
        mTtsManager.stopSpeaking();
//
//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
        mAMapNavi.stopNavi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mSpeechTip.release();//释放资源
        this.mSpeechTip = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        mTtsManager.destroy();

    }

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int type) {
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

        if(location!=null){
            Log.d("wjctag","now location:"+location.getCoord().getLatitude()+location.getCoord().getLongitude());

            float distance=10000.0f;
            float MIN=10000.f;
            int index=-1;
            for(int i=0;i<latLngsList.size();++i){
                LatLng point=new LatLng(location.getCoord().getLatitude(),location.getCoord().getLongitude());
                distance=AMapUtils.calculateLineDistance(latLngsList.get(i), point);
                if(distance<MIN){
                    index=i;
                    MIN=distance;
                }
            }
            if(isRemind){//在红绿灯区域并已提醒
                if(MIN>20){
                    isRemind=false;
                }
            }
            else{//不在红绿灯区域，即将提醒
                if(MIN<20){
                    isRemind=true;
                    if(typeList.get(index)==0.0)
                        mSpeechTip.speak("前方有红绿灯");
                    else if(typeList.get(index)==1.0)
                        mSpeechTip.speak("前方有路口");

                }
            }

        }
    }

    @Override
    public void onGetNavigationText(int type, String text) {

    }

    @Override
    public void onEndEmulatorNavi() {
    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {
        mAMapNavi.startNavi(NaviType.EMULATOR);
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int wayID) {

    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
    }

    @Override
    public void onNaviSetting() {
    }

    @Override
    public void onNaviMapMode(int isLock) {

    }

    @Override
    public void onNaviCancel() {
        finish();
    }


    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }


    @Override
    public void onScanViewButtonClick() {
    }

    @Deprecated
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {

        NaviLatLng location=naviinfo.getCoord();
        Log.d("wjctag","moni:"+location.getLatitude()+","+location.getLongitude());
        if(location!=null){
           // Log.d("wjctag","now location:"+location.getLatitude()+location.getLongitude());

            float distance=10000.0f;
            float MIN=10000.f;
            int index=-1;
            for(int i=0;i<latLngsList.size();++i){
                LatLng point=new LatLng(location.getLatitude(),location.getLongitude());
                distance=AMapUtils.calculateLineDistance(latLngsList.get(i), point);
                if(distance<MIN){
                    MIN=distance;
                    index=i;
                }

            }
            if(isRemind){//在红绿灯区域并已提醒
                if(MIN>5){
                    isRemind=false;
                }
            }
            else{//不在红绿灯区域，即将提醒
                if(MIN<5){
                    isRemind=true;
                    if(typeList.get(index)==0.0)
                        mSpeechTip.speak("前方有红绿灯");
                    else if(typeList.get(index)==1.0)
                        mSpeechTip.speak("前方有路口");

                }
            }
            Log.d("wjctag","distance:"+MIN);
        }
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
    }

    @Override
    public void hideCross() {
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }


    @Override
    public void onLockMap(boolean isLock) {
    }

    @Override
    public void onNaviViewLoaded() {
        Log.d("wlx", "导航页面加载成功");
        Log.d("wlx", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }



}

