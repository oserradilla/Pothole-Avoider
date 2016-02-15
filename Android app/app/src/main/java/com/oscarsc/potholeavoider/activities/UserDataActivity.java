package com.oscarsc.potholeavoider.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.internal.widget.ActionBarContextView;
import android.view.MenuItem;
import android.view.View;

import com.oscarsc.potholeavoider.CurrentThemeHolder;
import com.oscarsc.potholeavoider.DisplayingActivityClass;
import com.oscarsc.potholeavoider.R;

public class UserDataActivity extends Activity {
    CurrentThemeHolder currentTheme=CurrentThemeHolder.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(currentTheme.getGeneralTheme());
        setContentView(R.layout.activity_user_data);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        finish();
        return super.onOptionsItemSelected(item);
    }
}
