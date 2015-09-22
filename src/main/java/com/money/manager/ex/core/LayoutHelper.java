package com.money.manager.ex.core;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;

/**
 * Helper functions related to layout.
 */
public class LayoutHelper {

    public LayoutHelper(Activity context){
        mActivity = context;
    }

    private Activity mActivity;

    public boolean isTablet(){
        LinearLayout fragmentDetail = (LinearLayout) mActivity.findViewById(R.id.fragmentDetail);
        boolean result = fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE;
        return result;
    }
}
