package com.triste.wjc.routeplanning;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Triste on 2016/9/11.
 */
public class SpeechTip extends Activity implements TextToSpeech.OnInitListener {

    private static final String TAG = "wjctag";
    private TextToSpeech textToSpeech; // TTS对象
     private Context mContext=null;
    public static SpeechTip speechTip=null;

    private SpeechTip(Context context){
        this.mContext=context;
        textToSpeech = new TextToSpeech(context,this);
        //textToSpeech.setSpeechRate()
    }


    public static SpeechTip getInstance(Context context){
        if(speechTip==null){
            speechTip=new SpeechTip(context);
        }
        /*else{
            if(!speechTip.mContext.equals(context)){
                speechTip.textToSpeech=null;
                speechTip = new SpeechTip(context);
            }
        }*/
        return speechTip;
    }



    public void speak(String text){
        if(!text.equals(""))
        {
            textToSpeech.setSpeechRate(1.5f);
            textToSpeech.speak(String.valueOf(text),TextToSpeech.QUEUE_FLUSH ,null);//TextToSpeech.QUEUE_ADD
        }
    }

    public void speakQueue(String text){
        if(!text.equals(""))
        {
            textToSpeech.setSpeechRate(1.5f);
            textToSpeech.speak(String.valueOf(text),TextToSpeech.QUEUE_ADD ,null);//TextToSpeech.QUEUE_ADD
        }
    }

    public void speakSlow(String text){
        if(!text.equals(""))
        {
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.speak(String.valueOf(text),TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void stop(){
        textToSpeech.stop();
    }

    public void release(){
        //this.stop();
        /*textToSpeech = null;
        speechTip = null;*/
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            /*if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
            }*/
            if(result == TextToSpeech.LANG_MISSING_DATA)
                Toast.makeText(this, "数据丢失", Toast.LENGTH_SHORT).show();
            else if(result == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(this, "不支持", Toast.LENGTH_SHORT).show();
        }
    }
}
