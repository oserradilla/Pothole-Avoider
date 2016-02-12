package com.oscarsc.potholeavoider;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Observable;

public class CurrentThemeHolder extends Observable{
    //Constants
    public final static int GENERAL_DAY_THEME = R.style.AppTheme_Light;
    public final static int GENERAL_NIGHT_THEME = R.style.AppTheme_Dark;
    public final static int MAIN_AC_DAY_THEME = R.style.CustomActionBarTheme_Light;
    public final static int MAIN_AC_NIGHT_THEME = R.style.CustomActionBarTheme_Dark;
    //Variables
    private static CurrentThemeHolder instance;
    /*Singleton functions*/
    private CurrentThemeHolder() {}

    public void send(Object data){
        setChanged();
        this.notifyObservers(data);
    }

    public synchronized static CurrentThemeHolder getInstance(){
        if (instance == null){
            instance = new CurrentThemeHolder();
        }
        return instance;
    }
    /*Theme functions*/
    private int generalTheme =0; //identifier of the theme
    private int mainActivityTheme=0;
    private static boolean isNight=false;
    private boolean isAuto=false;

    public int getGeneralTheme() {
        return generalTheme;
    }
    public int getMainAcTheme() {
        return mainActivityTheme;
    }
    public void setNight(boolean isNight){
        this.isNight=isNight;
        generalTheme =isNight? GENERAL_NIGHT_THEME : GENERAL_DAY_THEME;
        mainActivityTheme =isNight? MAIN_AC_NIGHT_THEME : MAIN_AC_DAY_THEME;
    }
    public static boolean isNight(){
        return isNight;
    }
    public void setCurrentTheme(Activity activity){
        //Constants
        final String on=activity.getResources().getString(R.string.on);
        final String off=activity.getResources().getString(R.string.off);
        final String auto=activity.getResources().getString(R.string.auto);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String themeState=prefs.getString("night_mode_key",off);
        if(themeState.equals(on)) {
            setNight(true);
            isAuto=false;
        }else if(themeState.equals(off)) {
            setNight(false);
            isAuto=false;
        }else if(themeState.equals(auto))
            isAuto=true;
        send(null);
    }
    public boolean isAuto(){
        return isAuto;
    }
}