package com.oscarsc.potholeavoider.gps;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by oscar on 30/03/2016.
 */
public class SpeedLastValue {

    private AtomicInteger lastSpeed = new AtomicInteger(-1);

    public void setLastSpeed(int lastSpeed) {
        this.lastSpeed.set(lastSpeed);
    }

    public int getLastSpeed() {
        return lastSpeed.get();
    }
}
