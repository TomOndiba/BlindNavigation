package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.util.List;

/**
 * Created by Triste on 2016/8/19.
 */
public class MarkerActivity extends Activity implements AMap.OnMapClickListener, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerDragListener, AMap.OnMapLoadedListener,
        View.OnClickListener, AMap.InfoWindowAdapter,RouteSearch.OnRouteSearchListener {

////////////////////////////////////
    private MapView mapView=null;
    private TextView text_loc;
    private TextView text_locInfo;
    private AMap aMap=null;
    private LatLng myPosition=null;
///////////////////////////////////
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                Log.d("wjctag","move camera");
                String result = "("+loc.getLatitude()+","+loc.getLongitude()+")";//getLocationDetail();
                if(myPosition==null){
                    myPosition=new LatLng(loc.getLatitude(),loc.getLongitude());
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(myPosition));
                    aMap.moveCamera(CameraUpdateFactory.zoomBy(10.0f));
                    aMap.addMarker(new MarkerOptions().anchor(0.5f, 1.0f)
                            .position(myPosition).title("myPosition")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true));
                }

                text_locInfo.setText(result);
            } else {
                text_locInfo.setText("定位失败，loc is null");
            }
        }
    };
/////////////////////////////////////////
    private WalkRouteResult mWalkRouteResult;
    private Button btn_search=null;
    private Button btn_detail=null;
    private EditText edt_start=null;
    private EditText edt_end=null;
    private Context mContext=null;
    private RouteSearch mRouteSearch;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
/////////////////////////////////////////
    private AutoCompleteTextView searchText;
    private String keyword="";
    private EditText editCity;
    private PoiResult poiResult;
    private int currentPage=0;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;


////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        /*text_loc=(TextView)findViewById(R.id.textView_loc);
        text_locInfo=(TextView)findViewById(R.id.textView_locInfo);*/

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();

        if(init()){
            Log.d("wjctag","init success");
        }
    }

    private boolean init(){


        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setInterval(1000);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.setLocationListener(locationListener);
        mLocationClient.startLocation();
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

        btn_search=(Button)findViewById(R.id.button_search);
        btn_detail=(Button)findViewById(R.id.button_detail);
        edt_start=(EditText)findViewById(R.id.editText_startPos);
        edt_end=(EditText)findViewById(R.id.editText_endPos);
        mContext=this.getApplicationContext();

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRouteResult(RouteSearch.WalkDefault);
                mapView.setVisibility(View.VISIBLE);
            }
        });

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
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }

        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint,mEndPoint);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
        mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
    }







//--------------------------------------------------------
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
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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
    public void onMapClick(LatLng latLng) {
        double latitude=latLng.latitude;
        double longtitude=latLng.longitude;
       // text_loc.setText("latitude:"+latitude+",longtitude:"+longtitude);
        Log.d("wjctag", "latitude:" + latitude + ",longtitude:" + longtitude);
        aMap.addMarker(new MarkerOptions().anchor(0.5f, 1.0f)
                .position(latLng).title("" + latitude)
                .snippet(""+longtitude).draggable(true));
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
        }
        else{
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }
}
