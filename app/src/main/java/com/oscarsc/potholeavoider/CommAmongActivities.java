package com.oscarsc.potholeavoider;

import java.util.Observable;

/**/


/**/
/*Singleton Class*/
public class CommAmongActivities extends Observable{
    static CommAmongActivities instance = null;
    static int numInstances=0;

    public void send(Object data){
        setChanged();
        this.notifyObservers(data);
    }
    public synchronized static CommAmongActivities getInstance(){
        if (instance == null){
            instance = new CommAmongActivities();
        }
        return instance;
    }
    public synchronized int getNumInstances(){
        return numInstances;
    }
}
