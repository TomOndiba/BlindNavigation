package com.triste.wjc.routeplanning;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Triste on 2016/9/12.
 */
public class ReadWriteIndex {

    public static final String DIR_NAME="BlindNavi";
    private static final String INDEX_NAME="route.txt";
    public int index=0;
    public List<IndexRecord> list=new ArrayList<IndexRecord>();

    ReadWriteIndex(){

    }

    public int getIndex(){
        return index;
    }

    public void readIndex(){
        try {

            list.clear();

            File destDir = new File(Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String filepath= Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME+"/" + INDEX_NAME;

            File file=new File(filepath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int lineIndex=0;//判断第几行,readLine方法整行读取

                lineTxt = bufferedReader.readLine();
                if(lineTxt==null){
                    index=100;
                }
                else{
                    index=Integer.parseInt(lineTxt);//第一行的数字

                    while((lineTxt = bufferedReader.readLine()) != null){
                        String line2=bufferedReader.readLine();
                        String line3=bufferedReader.readLine();
                        IndexRecord temp=new IndexRecord(lineTxt,line2,line3);
                        list.add(temp);

                    }
                }

                read.close();
            }else{
                Log.d("wjctag", "找不到指定的文件");
            }
        } catch (Exception e) {
            Log.d("wjctag","读取文件内容出错");
            e.printStackTrace();
        }
    }

    public void saveIndex(int index,IndexRecord recotrd){
        String filepath= Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME+"/" + INDEX_NAME;

        try {
            this.readIndex();
            File file=new File(filepath);
            if(file.exists()){
                Log.d("wjctag", "File temp.txt already exists.");
            }
            PrintWriter output= null;
            output = new PrintWriter(new FileWriter(file));

            output.println(index+"");
            output.println(recotrd.fileName);
            output.println(recotrd.startName);
            output.println(recotrd.endName);
            Log.d("wjctag","save:"+recotrd.fileName);

            for (int i=0;i<list.size();++i){
                output.println(list.get(i).fileName);
                output.println(list.get(i).startName);
                output.println(list.get(i).endName);
                Log.d("wjctag","save:"+i+" "+list.get(i).fileName+" "+list.get(i).startName+" "+list.get(i).endName);
            }

            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void deleteRoute(int which){//从0开始

        try {

            File destDir = new File(Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String filepath= Environment.getExternalStorageDirectory() + "/"+ReadWriteIndex.DIR_NAME+"/" + INDEX_NAME;

            File file=new File(filepath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int lineIndex=0;//判断第几行,readLine方法整行读取

                lineTxt = bufferedReader.readLine();


                while((lineTxt = bufferedReader.readLine()) != null){
                    String line2=bufferedReader.readLine();
                    String line3=bufferedReader.readLine();

                    if(lineIndex!=which){
                        IndexRecord temp=new IndexRecord(lineTxt,line2,line3);
                        list.add(temp);
                    }
                }
                read.close();

                PrintWriter output= null;
                output = new PrintWriter(new FileWriter(file));

                output.println(lineTxt);

                for (int i=0;i<list.size();++i){
                    output.println(list.get(i).fileName);
                    output.println(list.get(i).startName);
                    output.println(list.get(i).endName);
                }

                output.close();


            }else{
                Log.d("wjctag", "找不到指定的文件");
            }
        } catch (Exception e) {
            Log.d("wjctag","读取文件内容出错");
            e.printStackTrace();
        }


    }

    public void Print(){
        Log.d("wjctag","index:"+index);
        for(int i=0;i<list.size();++i){
            Log.d("wjctag","filename:"+list.get(i).fileName);
            Log.d("wjctag","startname:"+list.get(i).startName);
            Log.d("wjctag","endtime:"+list.get(i).endName);
        }

    }

}
