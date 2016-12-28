package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Triste on 2016/11/1.
 */
public class ChooseUploadMode extends Activity {

    private Button btn_uploadMode1;
    private Button btn_uploadMode2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chooseuploadmode);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btn_uploadMode1 = (Button)findViewById(R.id.button_uploadmode1);
        btn_uploadMode2 = (Button)findViewById(R.id.button_uploadmode2);

        btn_uploadMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ChooseUploadMode.this, UploadRouteActivity.class);
                startActivity(intent);
            }
        });

        btn_uploadMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ChooseUploadMode.this, SetActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {

        super.onResume();
        Log.d("wjctag", "uploadmode resume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("wjctag", "uploadmode restart");
       /* if(speechTip==null){
            speechTip=SpeechTip.getInstance(getApplicationContext());
        }
        speechTip.speak("返回选择功能界面");*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("wjctag", "uploadmode start");
    }
}
