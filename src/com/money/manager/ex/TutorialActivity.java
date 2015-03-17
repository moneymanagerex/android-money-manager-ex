package com.money.manager.ex;

import com.money.manager.ex.adapter.TutorialPagerAdapter;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

/**
 *
 * See: http://developer.android.com/training/implementing-navigation/lateral.html
 */
public class TutorialActivity extends FragmentActivity {
    TutorialPagerAdapter mTutorialPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
//        mDemoCollectionPagerAdapter =
//                new DemoCollectionPagerAdapter(
//                        getSupportFragmentManager());
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
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
