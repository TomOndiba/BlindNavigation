package com.triste.wjc.routeplanning;


import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Triste on 2016/10/16.
 */
public class PointsList {
    public List<Point> list = null;
    public PointsList(){
        list = new ArrayList<Point>();
    }

    int naviIndex=0;
    int crossingIndex=0;
    int lightIndex = 0;

    static public int currentDirect = -1;


    public PointsList(String s){
        list = new ArrayList<Point>();
        String []a = s.split(";");
        for(int i=0;i<a.length;++i){
            list.add(new Point(a[i]));
        }
    }

    public void addPoint(LatLng lat){
        if(UploadRouteActivity.isLocChanged){
            list.add(new Point(lat.latitude,lat.longitude));
            UploadRouteActivity.listInfo.set(3,"红绿灯或路口信息添加成功");
            UploadRouteActivity.showListInfo();
        }

        else{
            UploadRouteActivity.listInfo.set(3,"位置未改变，未能添加成功");
            UploadRouteActivity.showListInfo();
        }

    }

    public void addPoint(LatLng lat,int type,int d,int length){
        if(UploadRouteActivity.isLocChanged){
            list.add(new Point(lat.latitude,lat.longitude,type,d,length));
            UploadRouteActivity.listInfo.set(3,"红绿灯或路口信息添加成功");
            UploadRouteActivity.showListInfo();
        }
        else{
            UploadRouteActivity.listInfo.set(3,"位置未改变，未能添加成功");
            UploadRouteActivity.showListInfo();
        }
    }

    public void addPoint(Point p){
        list.add(new Point(p.getX(),p.getY(),p.getType(),p.getDirect(),p.getLength()));
    }

    public void addPointAnyway(LatLng lat,int type,int d,int length){
            list.add(new Point(lat.latitude,lat.longitude,type,d,length));
    }

    public String ToString(){
        String s="";
        for(int i=0;i<list.size()-1;++i){
            if(list.get(i).getType()==-1)
                s+=list.get(i).ToString1()+";";
            else{
                //过滤掉不小心转弯的点
                if(list.get(i).getType()==3){
                    if(list.get(i+1).getType()==3){
                        if(UploadRouteActivity.getAbsoulteAngle(list.get(i+1).getDirect(),list.get(i).getDirect())<20&&
                                list.get(i+1).getLength()-list.get(i).getLength()<=100)
                            ;/////////////////
                        else
                            s+=list.get(i).ToString2()+";";
                    }
                    else{
                        if(list.get(i-1).getType()!=3){
                            if(UploadRouteActivity.getAbsoulteAngle(list.get(i-1).getDirect(),list.get(i).getDirect())<20)
                                ;/////////////
                            else
                                s+=list.get(i).ToString2()+";";
                        }
                        else{
                            s+=list.get(i).ToString2()+";";
                        }
                    }

                }
                else
                    s+=list.get(i).ToString2()+";";
            }

        }
        if(list.size()>0){
            if(list.get(list.size()-1).getType()==-1)
                s+=list.get(list.size()-1).ToString1()+";";
            else
                s+=list.get(list.size()-1).ToString2()+";";
        }
        return s;
    }

    public String print(){
        String s="";
        for(int i=0;i<list.size();++i){
            s+=list.get(i).print()+"\n";
        }
            return s;
    }

    public void initDirect(){
        currentDirect = list.get(0).getDirect();
    }

    public String getNextTip(int direct){
        String s="";
        ++naviIndex;
        if(naviIndex<=list.size()){
            Point point = list.get(naviIndex);
            Point lastPoint = list.get(naviIndex-1);

            switch (point.getType()){

                case 0:
                    s+="到达终点";
                    break;
                case 1:
                    currentDirect = point.getDirect();
                    ++crossingIndex;
                    if(crossingIndex%2==1)
                        s+="前方有路口";
                    else
                        s+="路口结束";
                    break;
                case 2:
                    currentDirect = point.getDirect();
                    ++lightIndex;
                    if(lightIndex%2==1)
                        s+="前方有红绿灯";
                    else
                        s+="红绿灯结束";
                    break;
                case 3:
                    currentDirect = point.getDirect();
                    s=getTipByAngles(point.getDirect(),direct);
                    break;
            }
            return s;
        }
        return s;

    }


    public static String getTipByAngles(int a,int b){
        String s="";
        int angle = a-b;
        int tt = (360+angle)%360;
        int ttt=0;
        if(tt>180)
            ttt = 360-tt;
        else{
            ttt = tt;
        }
        if(ttt<15)
            return s;

        if(angle>=0){//右转
            if(angle<180)
                s+="右转"+angle+"度";
            else
                s+="左转"+(360-angle)+"度";
        }
        else{//左转
            angle=-angle;
            if(angle<180)
                s+="左转"+angle+"度";
            else
                s+="右转"+(360-angle)+"度";
        }
        return s;
    }

    public static String getTipByAngles1(int a,int b){
        String s="";
        int angle = a-b;
        int tt = (360+angle)%360;
        int ttt=0;
        if(tt>180)
            ttt = 360-tt;
        else{
            ttt = tt;
        }
        if(ttt<15)
            return s;

        if(angle>=0){//右转
            if(angle<180)
                s+="右偏"+angle+"度";
            else
                s+="左偏"+(360-angle)+"度";
        }
        else{//左转
            angle=-angle;
            if(angle<180)
                s+="左偏"+angle+"度";
            else
                s+="右偏"+(360-angle)+"度";
        }
        return s;
    }


}
