package com.oscarsc.potholeavoider.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.oscarsc.potholeavoider.CommAmongActivities;
import com.oscarsc.potholeavoider.CurrentThemeHolder;
import com.oscarsc.potholeavoider.DisplayingActivityClass;
import com.oscarsc.potholeavoider.MainActivityHandler;
import com.oscarsc.potholeavoider.R;
import com.oscarsc.potholeavoider.ble.Ble;
import com.oscarsc.potholeavoider.listeners.GpsListener;
import com.oscarsc.potholeavoider.services.IncidenceCommunicator;
import com.oscarsc.potholeavoider.services.WeatherCommunicator;
import com.oscarsc.potholeavoider.text_to_speech.MyTextToSpeech;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private PowerManager.WakeLock wl;
    int mainTheme;
    IncidenceCommunicator incidenceCommunicator;
    WeatherCommunicator weatherCommunicator;
    MyTextToSpeech tts;
    MainActivityHandler mainHandler;
    GpsListener gpsManager;
    CommAmongActivities commAmongActivities=CommAmongActivities.getInstance();
    CurrentThemeHolder currentTheme=CurrentThemeHolder.getInstance();
    DisplayingActivityClass displayingActivity= DisplayingActivityClass.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayingActivity.setDisplayingClass(getClass());
        currentTheme.addObserver(this);
        currentTheme.setCurrentTheme(this);
        setTheme(currentTheme.getMainAcTheme());
        mainTheme=currentTheme.getMainAcTheme();
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayShowTitleEnabled(false);
        init();
    }
    private void init(){
        startGps();
        configureDrawer();
        setMainFragment();
        createMainHandler();
        initTte();
        startServices();
        commAmongActivities.addObserver(incidenceCommunicator);
        enableAlwaysScreenOn();
    }
    private void enableAlwaysScreenOn(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
        wl.acquire();
    }
    private void disableAlwaysScreenOn(){
        wl.release();
    }
    private void resizeMainLayout() {
        FrameLayout drawer_elements_layout=(FrameLayout) findViewById(R.id.content_frame);
        System.out.println(drawer_elements_layout.getLayoutParams().width);
        System.out.println(drawer_elements_layout.getLayoutParams().height);

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int x = metrics.widthPixels;
        int y = metrics.heightPixels;

        drawer_elements_layout.getLayoutParams().width=100;
        drawer_elements_layout.getLayoutParams().height=100;
		/*Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		RelativeLayout main=(RelativeLayout) findViewById(R.id.mainLayout);
		main.getLayoutParams().width=width;
		main.getLayoutParams().height=height;*/
    }
    private void createMainHandler() {
        mainHandler=new MainActivityHandler(this);
    }

    private void initTte() {
        tts=new MyTextToSpeech(getApplication().getBaseContext());
    }

    private void setMainFragment() {
        // update the main content by replacing fragments
        Fragment fragment = new MainFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
    }
    private void startGps(){
        gpsManager = new GpsListener(this);
    }
    private void startServices() {
		/*
		 * Create and launch the service that is going to check for near
		 * incidences (connected to the database) and notify them to the user
		 * using visual and audition communication
		 */
        incidenceCommunicator = new IncidenceCommunicator(this,mainHandler, tts,gpsManager);
        gpsManager.addObserver(incidenceCommunicator);
        weatherCommunicator = new WeatherCommunicator(mainHandler);

        // TODO añadir analyzer como bluetoothListener
        // TODO añadir incidenceCommunicator como LocationListener (gps
        // onlocation changed)

        Ble ble=new Ble(this, tts, gpsManager);
    }

    private IconText[] createData() {
        IconText[] data = new IconText[5];
        data[0] = new IconText(R.drawable.ic_account_circle_black_36dp, getResources()
                .getString(R.string.prefUserTitle));
        data[1] = new IconText(R.drawable.ic_assessment_black_36dp, getResources()
                .getString(R.string.prefStatisticsTitle));
        data[2] = new IconText(R.drawable.ic_settings_black_36dp, getResources()
                .getString(R.string.preferencesTitle));
        data[3] = new IconText(R.drawable.ic_navigation_black_36dp, getResources()
                .getString(R.string.navigationTitle));
        data[4] = new IconText(R.drawable.ic_help_black_36dp, getResources()
                .getString(R.string.helpTitle));
        return data;
    }
    public void setMyTheme(){
        int theme=currentTheme.getMainAcTheme();
        setTheme(theme);
        currentTheme.deleteObserver(this);
    }
    @Override
    public void update(Observable observable, Object data) {
        if(observable.equals(currentTheme)){

        }
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    @SuppressWarnings("rawtypes")
    private void selectItem(int position) {
        // Launch an activity depending on the selected item on the drawer
        Intent intent=null;
        Class intentClass = null;
        switch (position) {
            case 0:
                intent = new Intent(this, UserDataActivity.class);
                intentClass=UserDataActivity.class;
                break;
            case 1:
                intent = new Intent(this, StatisticsActivity.class);
                intentClass=StatisticsActivity.class;
                break;
            case 2:
                intent = new Intent(this, PreferencesActivity.class);
                intentClass=PreferencesActivity.class;
                break;
            case 3:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:"));
                break;
            case 4:
                intent = new Intent(this, HelpActivity.class);
                intentClass=HelpActivity.class;
                break;
            default:
                break;
        }
        if(intent!=null && intentClass!=null)
            startActivityForResult(intent, position);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        displayingActivity.setDisplayingClass(getClass());
        if (requestCode == 2) {
            int theme=currentTheme.getMainAcTheme();
            if(theme!=mainTheme)
                recreate();
        }
    }

    /**
     * Activity functions
     */
	/*
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutDown();
        disableAlwaysScreenOn();
        mainHandler.finish();
		/*if(analizer!=null)
			stopService(analizer);*/
    }

    /****
     * Drawer part
     ****/
    private void configureDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        IconText[] data = createData();

        mDrawerList.setAdapter(new MyAdapter(this, R.layout.drawer_list_item,
                data));

        // set up the drawer's list view with items and click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

		/* ActionBarDrawerToggle ties together the the proper interactions
		   between the sliding drawer and the action bar app icon */
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.drawer_open, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public static class MainFragment extends Fragment {

        public MainFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.drawer_elements, null);

            return rootView;
        }
    }

    private class IconText {
        private int icon;
        private String text;

        public IconText(int icon, String text) {
            this.icon = icon;
            this.text = text;
        }

        public int getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }
    }

    private class MyAdapter extends ArrayAdapter<IconText> {
        IconText data[] = null;

        public MyAdapter(Context mContext, int layoutResourceId, IconText[] data) {
            super(mContext, layoutResourceId, data);
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.drawer_list_item, parent, false);

            ImageView iv = (ImageView) convertView
                    .findViewById(R.id.imageView_drawer);
            TextView tv = (TextView) convertView.findViewById(R.id.text_drawer);

            iv.setImageResource(data[position].getIcon());
            tv.setText(data[position].getText());

            return convertView;
        }
    }
}