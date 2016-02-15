package com.oscarsc.potholeavoider.text_to_speech;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

import com.oscarsc.potholeavoider.CurrentThemeHolder;

public class MyTextToSpeech {
	private static TextToSpeech tts;
    static boolean enabled;
	public static String language;

    private static MyTextToSpeech instance=null;


	public MyTextToSpeech(Context context){
        tts=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                String displayLanguage=Locale.getDefault().getDisplayLanguage().toLowerCase();
                switch(displayLanguage){
                    case "español":
                        Locale esp = new Locale("spa", "ESP");
                        tts.setLanguage(esp);
                        language="spanish";
                        break;
                    case "english":
                    default:
                        tts.setLanguage(Locale.UK);
                        language="english";
                        break;
                }
				 /* Locale esp = new Locale("spa", "ESP");
				  if (tts.isLanguageAvailable(esp) > 0){
						tts.setLanguage(esp);
						language="spanish";
				  }
				  if(tts.isLanguageAvailable(Locale.ENGLISH) > 0)
				      tts.setLanguage(Locale.ENGLISH);		*/
            }});
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        enabled=preferences.getBoolean("voice_key",true);
        instance=this;
    }
    private static void enableVoice(){
        enabled=true;
    }
    private static void disableVoice(){
        tts.stop();
        enabled=false;
    }
	public synchronized void speakText(String text) {
		//Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        if(enabled)
		    tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	public void pause() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

    public static void changeVoice(boolean voiceKey) {
        if(voiceKey)
            enableVoice();
        else
            disableVoice();
    }
    public void shutDown(){
        tts.shutdown();
    }

    public void destroy() {
        tts.stop();
        tts.shutdown();
    }
    public static MyTextToSpeech getInstance(){
        return instance;
    }
}