package com.money.manager.ex.core;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Surface;

public class ActivityUtils {
		
	public static int forceCurrentOrientation(Activity activity) {
		int prevOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		if (activity != null) {
		
			prevOrientation = activity.getRequestedOrientation(); // save current position
		    
			if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    	if (activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_0 || activity.getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_90) {
		    		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    	} else {
		    		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		    	}
		    } else if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		    } else {
		    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		    }
		}
		return prevOrientation;
	}
	
	public static void restoreOrientation(Activity activity, int orientation) {
		if (activity != null) {
			activity.setRequestedOrientation(orientation);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}
