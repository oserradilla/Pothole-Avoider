package potholeavoider.getspeed;


import java.math.BigDecimal;
import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements GPSCallback{
        private GPSManager gpsManager = null;
        private double speed = 0.0;
    Boolean isGPSEnabled=false;
    LocationManager locationManager;
    double currentSpeed,kmphSpeed;
    TextView txtview;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txtview=(TextView)findViewById(R.id.info_message);
        txtview.setText(getString(R.string.info));
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(MainActivity.this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled)
        {
        	gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        }
        else
        {
        	gpsManager.showSettingsAlert();
        }
    }
        @Override
        public void onGPSUpdate(Location location) 
        {
                float speed = location.getSpeed();
                int speedKmph = Math.round(speed*3.6f);
                txtview.setText(String.valueOf(speedKmph)+"km/h");
        }

        @Override
        protected void onDestroy() {
                gpsManager.stopListening();
                gpsManager.setGPSCallback(null);
                gpsManager = null;
                super.onDestroy();
        }
}