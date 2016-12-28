package com.triste.wjc.routeplanning;

import android.util.Log;

/**
 * Created by Triste on 2016/10/16.
 */
public class Point {

    private double x;
    private double y;
    private int type=-1;//0 普通点 1路口 2红绿灯 3自动加的方向改变的位置
    private int direct=-1;
    private int length = -1;

    public Point(){}

    public Point(double x,double y){
        this.x=x;
        this.y=y;
    }

    public Point(double x,double y,int t,int a,int length){
        this.x=x;
        this.y=y;
        this.type=t;
        this.direct=a;
        this.length = length;
    }

    public Point(String s){
        String []a=s.split(",");
        x=Double.parseDouble(a[0]);
        y=Double.parseDouble(a[1]);
        if(a.length>2){
            type=Integer.parseInt(a[2]);
            direct=Integer.parseInt(a[3]);
            length = Integer.parseInt(a[4]);
        }

    }

    public void setLength(int t){
        length = t;
    }

    public String ToString1(){
        return x+","+y;
    }

    public String ToString2(){
        return x+","+y+","+type+","+direct+","+length;
    }

    public String print(){
        Log.d("wjctag",x+" "+y+" "+type+" "+direct+" "+length);
        return x+" "+y+" "+type+" "+direct+" "+length;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public int getType(){
        return type;
    }

    public int getDirect(){
        return direct;
    }

    public int getLength(){
        return length;
    }
}
