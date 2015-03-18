package com.money.manager.ex.tutorial;

import com.money.manager.ex.R;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import me.relex.circleindicator.CircleIndicator;

/**
 * Horizontal Swipe View
 * See: http://developer.android.com/training/implementing-navigation/lateral.html
 */
public class TutorialActivity extends FragmentActivity implements TutorialPage1Fragment.OnFragmentInteractionListener {
    TutorialPagerAdapter mTutorialPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        //ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager_default);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_default);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator_default);

        //TutorialPagerAdapter pagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        mTutorialPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());

        //viewpager.setAdapter(pagerAdapter);
        mViewPager.setAdapter(mTutorialPagerAdapter);

        //circleIndicator.setViewPager(viewpager);
        circleIndicator.setViewPager(mViewPager);
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("Tutorial", "fragment interaction: " + uri.toString());
    }
}
