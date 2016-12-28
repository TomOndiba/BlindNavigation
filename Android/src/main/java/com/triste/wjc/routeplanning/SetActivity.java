package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Triste on 2016/8/22.
 */
public class SetActivity extends Activity implements  AMap.OnMapClickListener, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerDragListener, AMap.OnMapLoadedListener,
        View.OnClickListener, AMap.InfoWindowAdapter,RouteSearch.OnRouteSearchListener{

    private AMap aMap=null;
    private MapView mapView=null;
    private LatLng myPosition=null;
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;

    private Context mContext=null;
    private EditText edt_start;
    private EditText edt_end;
    private LatLng startPosition=null;
    private LatLng endPosition=null;
    private MarkerOptions startOption;
    private MarkerOptions endOption;

    private Button searchButton=null;
    private LinearLayout hide=null;

    private RouteSearch mRouteSearch;

    private WalkRouteResult mWalkRouteResult;

    private List<Marker> markerList=new ArrayList<Marker>();
    private List<Double> typeList=new ArrayList<Double>();
    private List<Marker> tempList=new ArrayList<Marker>();
    private Marker currentMarker=null;
    private static int markerIndex=0;
    private static int trafficIndex=0;
    Double type=0.0;

    private boolean isRoute=false;

    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576

    private Spinner spinner=null;

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String result = "("+loc.getLatitude()+","+loc.getLongitude()+")";//getLocationDetail();
                if(myPosition==null){
                    Log.d("wjctag", "move camera");
                   myPosition=new LatLng(loc.getLatitude(),loc.getLongitude());
                     /*aMap.moveCamera(CameraUpdateFactory.changeLatLng(myPosition));
                    aMap.moveCamera(CameraUpdateFactory.zoomBy(10.0f));
                    aMap.addMarker(new MarkerOptions().anchor(0.5f, 1.0f)
                            .position(myPosition).title("myPosition")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true));*/
                }

            } else {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d("wjctag", "create setActivity");

        spinner=(Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("wjctag","你选择的是"+position);
                if(position==0)
                    type=0.0;
                else if(position==1)
                    type=1.0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("wjctag","你选择的是"+0);
            }
        });

        /*try
        {
            Log.v("wjctag",Environment.getExternalStorageDirectory().toString());
            Log.v("wjctag", Environment.getExternalStorageState());
            String DEFAULT_FILENAME="test.txt";
            *//*File file = new File(Environment.getExternalStorageDirectory(),
                    DEFAULT_FILENAME);*//*
            String file=Environment.getExternalStorageDirectory()+"/"+DEFAULT_FILENAME;
            ReadOrWriteObject rOd = new ReadOrWriteObject(file);


            rOd.openFile(ReadOrWriteObject.FileWrite);
            for(int i =0;i<1000;i++)
                rOd.writeDouble(i);
            rOd.flush();
            rOd.closeFile();


            double d =0 ;
            rOd.openFile(ReadOrWriteObject.FileRead);
            while(true){
                d = rOd.readDouble() ;
                if(d == Double.MIN_NORMAL){//读取文件的终止符,双精度最小值，在文件中已经关闭了相关的流
                    break;
                }
                else{
                    Log.d("wjctag",d+" ");
                }
            }
            rOd.closeFile();
            rOd = null ;
        }
        catch(Exception e)
        {
            Log.v("EagleTag","file　create　error");
        }
*/
        mContext=this.getApplicationContext();

        mapView = (MapView) findViewById(R.id.setmap);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();

        if(init()){
            Log.d("wjctag","init success");
        }

        edt_start=(EditText)findViewById(R.id.editText_startPos);
        edt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("wjctag", "click edittext");
                Intent intent=new Intent();
                intent.putExtra("type","start");
                intent.setClass(SetActivity.this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        edt_end=(EditText)findViewById(R.id.editText_endPos);
        edt_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("wjctag", "click edittext");
                Intent intent=new Intent();
                intent.putExtra("type", "end");
                intent.setClass(SetActivity.this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        searchButton=(Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRouteResult(RouteSearch.WalkDefault);
                mapView.setVisibility(View.VISIBLE);
            }
        });

        hide=(LinearLayout)findViewById(R.id.hide);
        Button btn_hide=(Button)findViewById(R.id.button_hide);
        btn_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hide.getVisibility()==View.VISIBLE){
                    hide.setVisibility(View.GONE);
                }else{
                    hide.setVisibility(View.VISIBLE);
                }
            }
        });

        Button btn_add = (Button)findViewById(R.id.button_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarker();
            }
        });

        Button btn_delete = (Button)findViewById(R.id.button_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMarker();
            }
        });

        Button btn_save = (Button)findViewById(R.id.button_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("wjctag","click save button");

                for(int i=0;i<markerList.size();++i){
                    Marker marker=markerList.get(i);
                    marker.showInfoWindow();
                    Log.d("wjctag","between"+marker.getPosition().latitude+" "+marker.getPosition().longitude);
                }
                Log.d("wjctag", "end save button");
                try {

                    ReadWriteIndex rw=new ReadWriteIndex();
                    rw.readIndex();
                    int index1=rw.getIndex();
                    ++index1;
                    String DEFAULT_FILENAME = "route"+index1+".txt";

                    File destDir = new File(ReadWriteIndex.DIR_NAME);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    Log.v("wjctag", Environment.getExternalStorageDirectory().toString());
                    Log.v("wjctag", Environment.getExternalStorageState());

                    String file = Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME+"/" + DEFAULT_FILENAME;
                    ReadOrWriteObject rOd = new ReadOrWriteObject(file);

                    rOd.openFile(ReadOrWriteObject.FileWrite);
                    rOd.writeDouble(startPosition.latitude);
                    rOd.writeDouble(startPosition.longitude);
                    rOd.writeDouble(2.0);
                    Log.d("wjctag", "ss:" + startPosition.latitude + " " + startPosition.longitude);

                    for (int i = 0; i < markerList.size(); i++) {
                        Marker marker1 = markerList.get(i);

                        rOd.writeDouble(marker1.getPosition().latitude);
                        rOd.writeDouble(marker1.getPosition().longitude);
                        rOd.writeDouble((double)typeList.get(i));
                        Log.d("wjctag","ss:"+marker1.getPosition().latitude+" "+marker1.getPosition().longitude);
                    }
                    rOd.writeDouble(endPosition.latitude);
                    rOd.writeDouble(endPosition.longitude);
                    rOd.writeDouble(3.0);
                    Log.d("wjctag", "ss:" + endPosition.latitude + " " + endPosition.longitude);

                    rOd.flush();
                    rOd.closeFile();

                    Log.d("wjctag","--------------------");

                    double d =0 ;
                    rOd.openFile(ReadOrWriteObject.FileRead);
                    while(true){
                        d = rOd.readDouble() ;
                        if(d == Double.MIN_NORMAL){//读取文件的终止符,双精度最小值，在文件中已经关闭了相关的流
                            break;
                        }
                        else{
                            Log.d("wjctag",d+" ");
                        }
                    }
                    rOd.closeFile();
                    rOd = null ;


                    ////////////
                    IndexRecord record=new IndexRecord(DEFAULT_FILENAME,edt_start.getText().toString(),edt_end.getText().toString());
                    rw.saveIndex(index1,record);

                    rw.readIndex();
                    rw.Print();

                    markerIndex=0;
                    markerList.clear();
                    typeList.clear();

                    ToastUtil.show(mContext, "save success");

                } catch (Exception e) {
                    Log.v("EagleTag", "file　create　error");
                }

            }
        });
    }

    public boolean init(){

        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setInterval(1000);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.setLocationListener(locationListener);
        mLocationClient.startLocation();

        setUpMap();

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        return true;
    }

    private void setUpMap(){
        aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        aMap.setOnMapClickListener(this);

    }

    public void searchRouteResult(int mode){
        if ( startPosition== null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (endPosition == null) {
            ToastUtil.show(mContext, "终点未设置");
        }

        final RouteSearch.FromAndTo fromAndTo =
                new RouteSearch.FromAndTo(new LatLonPoint(startPosition.latitude,startPosition.longitude)
                        ,new LatLonPoint(endPosition.latitude,endPosition.longitude));
        Log.d("wjctag", startPosition.latitude + " " + startPosition.longitude);
        Log.d("wjctag", endPosition.latitude + " " + endPosition.longitude);
        //final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint,mEndPoint);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
        mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("wjctag", "intent");

        double latitude=intent.getDoubleExtra("latitude", 32.0);
        double longtitude=intent.getDoubleExtra("longtitude", 100.0);
        String title=intent.getStringExtra("title");
        String snippet=intent.getStringExtra("snippet");
        if(intent.getStringExtra("type").equals("start")){
            startPosition=new LatLng(latitude,longtitude);
            startOption=new MarkerOptions().anchor(0.5f, 1.0f)
                    .position(startPosition).title(title).snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)).draggable(true);
            edt_start.setText(title);
        }
        else{
            endPosition=new LatLng(latitude,longtitude);
            endOption=new MarkerOptions().anchor(0.5f, 1.0f)
                    .position(endPosition).title(title).snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)).draggable(true);
            edt_end.setText(title);
        }
        aMap.clear();
        if(startOption!=null)
            aMap.addMarker(startOption);
        if(endOption!=null)
            aMap.addMarker(endOption);

    }
////////////////////////////////////////////////
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(isRoute){
            double latitude=latLng.latitude;
            double longtitude=latLng.longitude;
            // text_loc.setText("latitude:"+latitude+",longtitude:"+longtitude);
            Log.d("wjctag", "latitude:" + latitude + ",longtitude:" + longtitude);
            currentMarker=aMap.addMarker(new MarkerOptions().anchor(0.5f, 1.0f)
                    .position(latLng).title("" + latitude)
                    .snippet(""+longtitude).draggable(true));
            tempList.add(currentMarker);
        }
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("wjctag", "click marker");
        currentMarker=marker;
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    private LatLng SearchPointConvert(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }


    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        aMap.clear();
        if(errorCode==1000){
            if (result != null && result.getPaths() != null){
                if(result.getPaths().size()>0){
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    /*WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.setNodeIconVisibility(false);
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();*/

                    float width=2.5f;

                    List<WalkStep> steps=walkPath.getSteps();
                    aMap.addPolyline(new PolylineOptions().add(SearchPointConvert(mWalkRouteResult.getStartPos())
                            , SearchPointConvert((steps.get(0).getPolyline().get(0)))).color(Color.BLUE).width(width));

                    for(int i=0;i<steps.size();++i){
                        WalkStep step=steps.get(i);
                        List<LatLonPoint> points=step.getPolyline();
                        for(int j=0;j<points.size()-1;++j){
                            aMap.addPolyline(new PolylineOptions().add(SearchPointConvert(points.get(j))
                                    ,SearchPointConvert(points.get(j+1))).color(Color.BLUE).width(width));
                        }
                    }
                    aMap.addPolyline(new PolylineOptions().add(SearchPointConvert((steps.get(steps.size()-1).getPolyline().get(steps.get(steps.size()-1).getPolyline().size()-1)))
                            , SearchPointConvert(mWalkRouteResult.getTargetPos())).color(Color.BLUE).width(width));

                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(SearchPointConvert(mWalkRouteResult.getStartPos())));

                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    Log.d("wjctag",des);



                }
            }
            isRoute=true;
        }
        else{
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    private void addMarker(){
        if(currentMarker!=null){
            markerList.add(currentMarker);
            typeList.add(type);
            if(type==0.0){
                switch(trafficIndex){
                    case 0:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_1));
                        break;
                    case 1:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_2));
                        break;
                    case 2:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_3));
                        break;
                    case 3:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_4));
                        break;
                    case 4:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_5));
                        break;
                    case 5:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_6));
                        break;
                    case 6:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_7));
                        break;
                    case 7:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_8));
                        break;
                    case 8:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_9));
                        break;
                    case 9:
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poi_marker_10));
                        break;

                }

                ++trafficIndex;
            }
            else if(type==1.0){
                currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
            ++markerIndex;
            currentMarker=null;
        }
    }

    private void deleteMarker(){


        if(currentMarker!=null){
            if(markerList.contains(currentMarker)){
                --markerIndex;
                markerList.remove(markerIndex);
                if(typeList.get(markerIndex)==0.0)
                    --trafficIndex;
                typeList.remove(markerIndex);
            }
            currentMarker.hideInfoWindow();
            currentMarker.destroy();
            currentMarker=null;
        }
    }



}
