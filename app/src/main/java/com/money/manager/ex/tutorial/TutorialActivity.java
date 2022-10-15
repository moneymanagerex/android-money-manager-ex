/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.GeneralSettingsActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;

/**
 * Horizontal Swipe View
 * See: http://developer.android.com/training/implementing-navigation/lateral.html
 */
public class TutorialActivity
    extends FragmentActivity {

    public static final int REQUEST_GENERAL_PREFERENCES = 1;

    public static final int RESULT_OK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the zygote background to speed up rendering. Only when activities have
        // their own background set.
        // tip from http://cyrilmottier.com/2013/01/23/android-app-launching-made-gorgeous/
        //getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_tutorial);

        ButterKnife.bind(this);

        CircleIndicator circleIndicator = findViewById(R.id.indicator_default);

        ViewPager viewpager = findViewById(R.id.viewpager_default);
        TutorialPagerAdapter pagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        viewpager.setAdapter(pagerAdapter);
        circleIndicator.setViewPager(viewpager);

//        TextView skipText = (TextView) findViewById(R.id.skipTextView);
//        skipText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onCloseClicked();
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GENERAL_PREFERENCES:
                // back from general preferences.

                setResult(AppCompatActivity.RESULT_OK);

                // Mark tutorial as seen.
                new AppSettings(this).getBehaviourSettings().setShowTutorial(false);

                startMainActivity();
                break;
        }
    }

    @OnClick(R.id.skipTextView)
    void onCloseClicked(){
        // show general preferences (language)
        Intent intent = new Intent(this, GeneralSettingsActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivityForResult(intent, REQUEST_GENERAL_PREFERENCES);
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
        // automatically e clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startMainActivity() {
        // start the Main Activity.
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // close
        finish();
    }
}
