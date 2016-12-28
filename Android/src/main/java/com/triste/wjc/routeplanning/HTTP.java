package com.triste.wjc.routeplanning;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Triste on 2016/10/15.
 */
public class HTTP {
    private OkHttpClient client;// = new OkHttpClient();
    public final static int CONNECT_TIMEOUT =2;
    public final static int READ_TIMEOUT=2;
    public final static int WRITE_TIMEOUT=2;
    private final String URL_HEADER="http://115.28.147.142:9555";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public String getRouteResponse="";

    public String uploadResponse="";

    public static boolean isResponceSuccess = false;

    public HTTP(){

        client =new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
    }

    public void getRoute(String start,String end){
        Log.d("wjctag","getrsss");
       // start = "起点五";
        //end = "终点五";
        JSONObject json = new JSONObject();
        String body="";
        try {
            json.put("startName",start);
            json.put("endName",end);
            body=json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(URL_HEADER+"/get_route")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(e.getCause().equals(SocketTimeoutException.class))//如果超时并未超过指定次数，则重新连接
                {
                    Log.d("wjctag","timeout");
                   // client.newCall(call.request()).enqueue(this);
                }else {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                getRouteResponse =response.body().string();
                SetNaviInfoActivity.handleRoute(getRouteResponse);
                Log.d("wjctag","re:"+getRouteResponse);
                /*Log.d("wjctag", sss);
                try {
                    if(!sss.equals("")){
                        JSONObject json = new JSONObject(sss);
                        String points = json.getString("points");
                        String crossings = json.getString("crossings");
                        String lights = json.getString("lights");
                        String startName = json.getString("startName");
                        String endName = json.getString("endName");
                        String startPos = json.getString("startPos");
                        String endPos = json.getString("endPos");
                        String middlePos = json.getString("middlePos");
                        int radius = json.getInt("radius");

                        Log.d("wjctag",points);
                        Log.d("wjctag",crossings);
                        Log.d("wjctag",lights);
                        Log.d("wjctag",startName);
                        Log.d("wjctag",endName);
                        Log.d("wjctag",startPos);
                        Log.d("wjctag",endPos);
                        Log.d("wjctag",middlePos);
                        Log.d("wjctag",radius+"");

                        Log.d("wjctag",json.getString("status"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }

        });
    }

    public void uploadRoute(PointsList points,PointsList crossings,PointsList lights,String start,String end){
        JSONObject json = new JSONObject();
        String body="";
        try {
            json.put("points",points.ToString());
            json.put("crossings",crossings.ToString());
            json.put("lights",lights.ToString());
            json.put("startName",start);
            json.put("endName",end);
            json.put("startPos",points.list.get(0).ToString2());
            json.put("endPos",points.list.get(points.list.size()-1).ToString2());
            double x = (points.list.get(0).getX()+points.list.get(points.list.size()-1).getX())/2;
            double y = (points.list.get(0).getY()+points.list.get(points.list.size()-1).getY())/2;
            json.put("middlePos",new Point(x,y).ToString1());
            json.put("radius",2134);
            body=json.toString();


        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(URL_HEADER+"/upload_route")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String sss=response.body().string();
                Log.d("wjctag", sss);
                try {
                    if(!sss.equals("")){
                        JSONObject json = new JSONObject(sss);
                        Log.d("wjctag",json.getString("status"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    public void test(){
        Request request = new Request.Builder()
                .url(URL_HEADER+"/test")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Log.d("wjctag", response.body().string());
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    Log.d("wjctag",json.getString("info"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

}
