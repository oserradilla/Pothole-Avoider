package com.oscarsc.potholeavoider.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.oscarsc.potholeavoider.CommAmongActivities;
import com.oscarsc.potholeavoider.CurrentThemeHolder;
import com.oscarsc.potholeavoider.DisplayingActivityClass;
import com.oscarsc.potholeavoider.R;

public class PreferencesActivity extends Activity implements OnPreferenceClickListener{
    CurrentThemeHolder currentTheme=CurrentThemeHolder.getInstance();
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(currentTheme.getGeneralTheme());
        setContentView(R.layout.activity_preferences);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
}
