package com.oscarsc.potholeavoider;

/**
 * Created by oscar on 1/17/15.
 */
public class DisplayingActivityClass {
    static DisplayingActivityClass instance = null;
    public Class displayingClass=null;
    private DisplayingActivityClass(){}

    public synchronized static DisplayingActivityClass getInstance(){
        if (instance == null){
            instance = new DisplayingActivityClass();
        }
        return instance;
    }
    public Class getDisplayingClass(){return displayingClass;}
    public void setDisplayingClass(Class displayingClass){this.displayingClass= displayingClass;}
}
