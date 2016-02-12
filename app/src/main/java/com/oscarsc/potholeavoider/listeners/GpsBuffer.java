package com.oscarsc.potholeavoider.listeners;

import android.location.Location;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by oscar on 2/4/15.
 * Cyclic buffer
 */
public class GpsBuffer {
    private final static int MAX_BUFFER_SIZE = 40;//40 = 40 * 50 =2000 m = 2Km
    private final static int INTERVAL = 50;//meters
    Location buffer[];
    int head;
    int realSize;

    public GpsBuffer() {
        buffer = new Location[MAX_BUFFER_SIZE];

        head = 0;
        realSize = 0;
        /*Location one = new Location("");
        one.setLatitude(-3);
        one.setLongitude(-3);

        Location two = new Location("");
        two.setLatitude(3);
        two.setLongitude(3);
        Location three = new Location("");
        three.setLatitude(-4);
        three.setLongitude(1);
        boolean isOnEllipse=isOnEllipse(one,two,three);
        System.out.println(isOnEllipse);
        /*
        Direction direction = new Direction();
        Location[] lastLocations = new Location[3];
        Location one = new Location("");
        one.setLatitude(1);
        one.setLongitude(1);

        Location two = new Location("");
        two.setLatitude(2);
        two.setLongitude(-1);
        Location three = new Location("");
        three.setLatitude(3);
        three.setLongitude(0);
        lastLocations[0] = one;
        lastLocations[1] = two;
        lastLocations[2] = three;
        direction.setLastLocations(lastLocations);
        direction.calculateMinimumMeanSquareLine();
        direction.calculateAngleVector();
        double degrees = direction.getAngleInDegrees();
        System.out.println("Grados sobre el norte: " + degrees);
        */
    }

    /*
    * Overrides buffer position when realSize=MAX_BUFFER_SIZE
    * */

    public synchronized Location getFirstLocation(){
        Location firstLocation=null;
        if(realSize>0 && realSize<MAX_BUFFER_SIZE)
            firstLocation=buffer[0];
        if(realSize==MAX_BUFFER_SIZE){
            int firstLocationPosition = (head-1<0)? MAX_BUFFER_SIZE-1:head-1;
            firstLocation=buffer[firstLocationPosition];
        }
        return firstLocation;
    }

     public synchronized void putLocation(Location newLocation) {
        boolean advance = false;
        buffer[head] = newLocation;
        if (realSize != MAX_BUFFER_SIZE)
            advance = true;
        else {
            int lastPosition = head - 1 < 0 ? MAX_BUFFER_SIZE - 1 : head - 1;
            double distance = GpsListener.distance(newLocation, buffer[lastPosition], 'm');
            if (distance > INTERVAL)
                advance = true;
        }
        if (advance) {
            head = (head + 1) % MAX_BUFFER_SIZE;
            if (realSize != MAX_BUFFER_SIZE)
                realSize++;
        }
    }

    /*This function doesn't override any position of the buffer, it only reads
    * @param int position. from 1 to MAX_BUFFER_SIZE, where 1 is the last position and realSize the last one
    * */
    public synchronized Location getLocation(int position) {
        if (position > realSize || position < 1)
            return null;
        int posRet = head - position;
        if (posRet < 0)
            posRet += realSize;
        return buffer[posRet];
    }

    public synchronized int getRealSize() {
        return realSize;
    }

    public synchronized Location[] getListLastLocations() {
        Location[] lastLocations;
        if (realSize == MAX_BUFFER_SIZE) {
            lastLocations = new Location[MAX_BUFFER_SIZE];
            int tempHead = head;
            for (int i = 0; i < MAX_BUFFER_SIZE; i++) {
                tempHead = (tempHead + 1) % MAX_BUFFER_SIZE;
                lastLocations[i] = buffer[tempHead];
            }
        } else {
            lastLocations = Arrays.copyOf(buffer, realSize);
        }
        return lastLocations;
    }

    //TODO Test if this function works properly and if it does, integrate it in the system
    //TODO Detect when an incidence is passed and remove it from the screen
    public double getDirection() {
        Direction direction = new Direction();
        direction.setLastLocations(getListLastLocations());
        direction.calculateMinimumMeanSquareLine();
        direction.calculateAngleVector();
        double angleDegrees = direction.getAngleInDegrees();
        return angleDegrees;
    }

    public Location calculatePrevLocation() {
        Direction direction = new Direction();
        direction.setLastLocations(getListLastLocations());
        direction.calculateMinimumMeanSquareLine();
        Location firstLocation = getFirstLocation();
        Location prevLocation = direction.createInLineLocationFromLatitude(firstLocation.getLatitude());
        return prevLocation;
    }

    private final double ELLIPSE_RELATION_A_B = 1;

    public boolean isOnEllipse(Location prevLocation, Location currentLocation, Location incidenceLocation) {
        double deltaLat = (currentLocation.getLatitude() - prevLocation.getLatitude()) / 2;
        double deltaLon = (currentLocation.getLongitude() - prevLocation.getLongitude()) / 2;

        double ellipseA = Math.sqrt(Math.pow(deltaLat, 2) + Math.pow(deltaLon, 2));
        double ellipseB = ellipseA / ELLIPSE_RELATION_A_B;

        double centerPointLat = prevLocation.getLatitude() + deltaLat;
        double centerPointLon = prevLocation.getLongitude() + deltaLon;

        double ellipseResult = applyEllipseFormula(incidenceLocation.getLatitude(), incidenceLocation.getLongitude(),
                centerPointLat, centerPointLon, ellipseA, ellipseB);

        return ellipseResult <= 1;
    }

    private double applyEllipseFormula(double x, double y, double h, double k, double a, double b) {
        double ellipseFormulaResult = Math.pow((x - h) / a, 2) + Math.pow((y - k) / b, 2);
        return ellipseFormulaResult;
    }

    public boolean areVectorsInRange(Location firstLocation, Location currentLocation, Location prevLocation, Location incidenceLocation, double degreesRangeAngle) {
        Direction direction=new Direction();
        double curLocationsAngle = direction.calculateAngleGivenTwoLocations(firstLocation,currentLocation);
        double incidenceLocationsAngle = direction.calculateAngleGivenTwoLocations(prevLocation,incidenceLocation);
        curLocationsAngle=Math.toDegrees(curLocationsAngle);
        incidenceLocationsAngle=Math.toDegrees(incidenceLocationsAngle);
        double realDegreesAngle=curLocationsAngle-incidenceLocationsAngle<0? incidenceLocationsAngle-curLocationsAngle: curLocationsAngle-incidenceLocationsAngle;
        return realDegreesAngle<=degreesRangeAngle;
    }

    private class Direction {
        Location[] lastLocations;
        double m;
        double n;
        double angle;

        public void Direction() {
            m = 0;
            n = 0;
            angle = 0;
        }

        public void setLastLocations(Location[] lastLocations) {
            this.lastLocations = ArrayUtils.clone(lastLocations);
        }

        public void calculateMinimumMeanSquareLine() {
            int numElements = lastLocations.length;
            double sx = calculateSx();
            double sy = calculateSy();
            double sxx = calculateSxx();
            double sxy = calculateSxy();

            m = (numElements * sxy - sx * sy) / (numElements * sxx - sx * sx);
            n = (sxx * sy - sx * sxy) / (numElements * sxx - sx * sx);
        }

        private double calculateSx() {
            double sx = 0;
            for (Location location : lastLocations) {
                sx += location.getLongitude();
            }
            return sx;
        }

        private double calculateSy() {
            double sx = 0;
            for (Location location : lastLocations) {
                sx += location.getLatitude();
            }
            return sx;
        }

        private double calculateSxx() {
            double sxx = 0;
            for (Location location : lastLocations) {
                double x = location.getLongitude();
                sxx += Math.pow(x, 2);
            }
            return sxx;
        }

        private double calculateSxy() {
            double sxy = 0;
            for (Location location : lastLocations) {
                sxy += location.getLongitude() * location.getLatitude();
            }
            return sxy;
        }

        public void calculateAngleVector() {
            double firstLocationLatitude = lastLocations[0].getLatitude();
            double lastLocationLatitude = lastLocations[lastLocations.length - 1].getLatitude();
            Location startLocation = createInLineLocationFromLatitude(firstLocationLatitude);
            Location endLocation = createInLineLocationFromLatitude(lastLocationLatitude);
            angle = calculateAngleGivenTwoLocations(startLocation, endLocation);
        }

        private double calculateAngleGivenTwoLocations(Location startLocation, Location endLocation) {
            //Vector of Minimum Mean Square Line = ux i + uy j
            double ux = endLocation.getLatitude() - startLocation.getLatitude();
            double uy = endLocation.getLongitude() - startLocation.getLongitude();
            //Vector of Y axe = vx i + vy j
            double vx = 0;
            double vy = 1;
            double angle = calculateAngleOfTwoLines(ux, uy, vx, vy);
            return angle;
        }

        private double calculateAngleOfTwoLines(double ux, double uy, double vx, double vy) {
            double cosAlpha = ux * vx + uy * vy /
                    (Math.sqrt(Math.pow(ux, 2) + Math.pow(uy, 2)) * Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)));
            double alpha = Math.acos(cosAlpha);
            if ((ux < 0 && vx >= 0) ||
                    (vx < 0 && ux >= 0))
                alpha = -alpha;
            return alpha;
        }

        private Location createInLineLocationFromLatitude(double latitude) {
            Location locationInLine = new Location("");
            locationInLine.setLatitude(latitude);
            double longitude = calculateLonGivenLat(latitude);
            locationInLine.setLongitude(longitude);
            return locationInLine;
        }

        /*Uses the function y=mx+n*/
        private double calculateLonGivenLat(double latitude) {
            double longitude = m * latitude + n;
            return longitude;
        }

        public double getAngleInDegrees() {
            return Math.toDegrees(angle);
        }

    }
}
