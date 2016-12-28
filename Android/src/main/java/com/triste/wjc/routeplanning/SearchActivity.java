package com.triste.wjc.routeplanning;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Triste on 2016/8/21.
 */
public class SearchActivity extends FragmentActivity implements
        AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, TextWatcher,
        PoiSearch.OnPoiSearchListener, View.OnClickListener, Inputtips.InputtipsListener{
    private AMap aMap;
    private AutoCompleteTextView searchKey;// 输入搜索关键字
    private String keyWord = "";// 要输入的poi搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索

    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;

    private LatLng myPosition=null;


    private Marker currentMarker=null;
    private Button btn_confirm=null;
    private int type=-1;

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String result = "("+loc.getLatitude()+","+loc.getLongitude()+")";//getLocationDetail();
                if(myPosition==null){
                    Log.d("wjctag","move camera");
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


    ////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d("wjctag", "create searchActivity");

        init();

        Intent intent=getIntent();
        if(intent.getStringExtra("type").equals("start"))
            type=0;
        else
            type=1;

        Log.d("wjctag",intent.getStringExtra("type"));

    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.searchMap)).getMap();
            setUpMap();
        }
        btn_confirm=(Button)findViewById(R.id.button_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMarker != null) {
                    Log.d("wjctag","SearchActivity click button");
                    Intent intent = new Intent();
                    intent.putExtra("latitude", currentMarker.getPosition().latitude);
                    intent.putExtra("longtitude", currentMarker.getPosition().longitude);
                    intent.putExtra("title", currentMarker.getTitle());
                    intent.putExtra("snippet", currentMarker.getSnippet());
                    if (type == 0)
                        intent.putExtra("type", "start");
                    else
                        intent.putExtra("type", "end");
                    currentMarker=null;
                    intent.setClass(SearchActivity.this, SetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setInterval(1000);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.setLocationListener(locationListener);
        mLocationClient.startLocation();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        aMap.clear();
        searchKey.setText("");

        super.onNewIntent(intent);
        setIntent(intent);
        if(intent.getStringExtra("type").equals("start"))
            type=0;
        else
            type=1;

    }

    /**
     * 设置页面监听
     */
    private void setUpMap() {

        searchKey = (AutoCompleteTextView) findViewById(R.id.searchKey);
        searchKey.addTextChangedListener(this);// 添加文本输入框监听事件
        searchKey.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                keyWord = AMapUtil.checkEditText(searchKey);
                if ("".equals(keyWord)) {
                    ToastUtil.show(SearchActivity.this, "请输入搜索关键字");
                    return;
                } else {
                    doSearchQuery();
                }
            }
        });




        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
    }

    /**
     * 点击搜索按钮
     */
    public void searchButton() {
        keyWord = AMapUtil.checkEditText(searchKey);
        if ("".equals(keyWord)) {
            ToastUtil.show(SearchActivity.this, "请输入搜索关键字");
            return;
        } else {
            doSearchQuery();
        }
    }



    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        currentPage = 0;


        query = new PoiSearch.Query(keyWord, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        query.setCityLimit(true);

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        if(currentMarker!=null)
            changeMarker(currentMarker,false);
        currentMarker=marker;
        changeMarker(currentMarker,true);
        Log.d("wjctag","("+marker.getPosition().latitude+","+marker.getPosition().longitude+")");
        return false;
    }

    //boolean为true表示为true
    public void changeMarker(Marker marker,boolean b){
        if(b){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        else{
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return view;
    }





    /**
     * 获取当前app的应用名字
     */
    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager
                .getApplicationLabel(applicationInfo);
        return applicationName;
    }



    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, "");
            Inputtips inputTips = new Inputtips(SearchActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }


    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {

                    } else {
                        ToastUtil.show(SearchActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(SearchActivity.this,
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(SearchActivity.this, rCode);
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
        // TODO Auto-generated method stub

    }

    /**
     * Button点击事件回调方法
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 点击搜索按钮
             */
            case R.id.searchButton:
                searchButton();
                break;
            /**
             * 点击下一页按钮
             */

            default:
                break;
        }
    }



    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == 1000) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.route_inputs, listString);
            searchKey.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(SearchActivity.this, rCode);
        }


    }

}

