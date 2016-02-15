package com.oscarsc.potholeavoider.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.oscarsc.potholeavoider.CommAmongActivities;
import com.oscarsc.potholeavoider.CurrentThemeHolder;
import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.services.IncidenceCommunicator;
import com.oscarsc.potholeavoider.text_to_speech.MyTextToSpeech;

import java.util.ArrayList;
import java.util.Set;

public class Preferences extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    CommAmongActivities commAmongActivities=CommAmongActivities.getInstance();
    CurrentThemeHolder currentTheme=CurrentThemeHolder.getInstance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        CommAmongActivities.getInstance();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
       /* SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
       // preferences.
        String strSavedMem1 = preferences.getString("language_key", "English");
        Log.v("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", strSavedMem1);
        preferences.getString("language_key", "English");*/
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch(key){
            case "voice_key":
                boolean voiceKey=sharedPreferences.getBoolean("voice_key",true);
                MyTextToSpeech.changeVoice(voiceKey);
                break;
            case "incidence_key":
                Set<String> stringSet=sharedPreferences.getStringSet("incidence_key",null);
                commAmongActivities.send(stringSet);
                //IncidenceCommunicator.incidencesSettingsChanged(stringSet);
                break;
            case "night_mode_key":
                boolean lastTheme=currentTheme.isNight();
                currentTheme.setCurrentTheme(getActivity());
                if(!lastTheme == currentTheme.isNight())
                    this.getActivity().recreate();
                if(currentTheme.isAuto()){

                }
        }
    }
}