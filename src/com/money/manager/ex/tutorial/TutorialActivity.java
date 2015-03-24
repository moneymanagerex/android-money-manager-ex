package com.money.manager.ex.tutorial;

import com.money.manager.ex.R;
import com.money.manager.ex.preferences.PreferencesConstant;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.relex.circleindicator.CircleIndicator;

/**
 * Horizontal Swipe View
 * See: http://developer.android.com/training/implementing-navigation/lateral.html
 */
public class TutorialActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the zygote background to speed up rendering.
        // tip from http://cyrilmottier.com/2013/01/23/android-app-launching-made-gorgeous/
        //getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tutorial);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator_default);

        ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager_default);
        TutorialPagerAdapter pagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), this);
        viewpager.setAdapter(pagerAdapter);
        circleIndicator.setViewPager(viewpager);

        TextView skipText = (TextView) findViewById(R.id.skipTextView);
        skipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeTutorial();
            }
        });
    }

    private void closeTutorial(){
        // Mark tutorial as seen, in the settings.
        Context context = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(PreferencesConstant.PREF_SHOW_TUTORIAL);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, false);
        editor.commit();

        // close
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
