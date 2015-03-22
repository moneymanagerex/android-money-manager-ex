package com.money.manager.ex.tutorial;

import com.money.manager.ex.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
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
    //TutorialPagerAdapter mTutorialPagerAdapter;
    //ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator_default);

        //
        ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager_default);
        TutorialPagerAdapter pagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), this);
        viewpager.setAdapter(pagerAdapter);
        circleIndicator.setViewPager(viewpager);

//        this.handleLastPage(viewpager);

        /*
        mViewPager = (ViewPager) findViewById(R.id.viewpager_default);
        mTutorialPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTutorialPagerAdapter);
        circleIndicator.setViewPager(mViewPager);
        */

        this.handleSkip();
    }

    /**
     * Add a handler to change text when we reach the last page.
     */
    private void handleLastPage(ViewPager pager){
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 4) {
                    // change the "skip" text into "close".
                    TextView skipText = (TextView) findViewById(R.id.skipTextView);
                    skipText.setText(R.string.close);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void handleSkip(){
        TextView skipText = (TextView) findViewById(R.id.skipTextView);

        skipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close
                finish();
            }
        });
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
