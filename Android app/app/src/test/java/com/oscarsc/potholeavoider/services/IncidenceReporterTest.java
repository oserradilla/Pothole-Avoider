package com.oscarsc.potholeavoider.services;

import android.location.Location;

import com.oscarsc.potholeavoider.incidences.Curve;
import com.oscarsc.potholeavoider.incidences.Pothole;
import com.oscarsc.potholeavoider.incidences.Slope;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.mockito.Mockito.*;

/**
 * Created by oscar on 16/02/2016.
 */
public class IncidenceReporterTest {
    Random random;

    @Before
    public void initialisation() {
        random = new Random();
    }

    @Test
    public void uploadDownloadIncidencesToServerTest() {
        MockLocation mockedLocation = new MockLocation("");
        mockedLocation.setLatitude(nextRandomLatitude());
        mockedLocation.setLongitude(nextRandomLongitude());

        ArrayList<Pothole> potholes = new ArrayList<>();
        for(int i=0; i<3; i++) {
            potholes.add(new Pothole(i, mockedLocation, 10));
        }
        ArrayList<Curve> curves = new ArrayList<>();
        for(int i=0; i<2; i++) {
            curves.add(new Curve(i, mockedLocation, 10, random.nextBoolean()));
        }
        ArrayList<Slope> slopes = new ArrayList<>();

        reportIncidencesTest(mockedLocation, potholes, curves, slopes);
        /*boolean incidencesReportedCorrectly =
                checkThatIncidencesAreInServer (potholes, curves, slopes);
        junit.framework.Assert.assertTrue("Incidences reported correctly", incidencesReportedCorrectly);*/
    }

    private void reportIncidencesTest(Location locationOfIncidences, ArrayList<Pothole> potholes, ArrayList<Curve> curves, ArrayList<Slope> slopes) {
        IncidenceReporter incidenceReporter = new IncidenceReporter();
        incidenceReporter.reportIncidencesToServer(potholes, curves, slopes);
    }

    private float nextRandomLatitude() {
        float minLatitude = -90.0f;
        float maxLatitude = 90.0f;
        float randomLatitude = random.nextFloat() * (maxLatitude - minLatitude) + minLatitude;
        return randomLatitude;
    }

    private float nextRandomLongitude() {
        float minLongitude = -180.0f;
        float maxLongitude = 180.0f;
        float randomLatitude = random.nextFloat() * (maxLongitude - minLongitude) + minLongitude;
        return randomLatitude;
    }
}
