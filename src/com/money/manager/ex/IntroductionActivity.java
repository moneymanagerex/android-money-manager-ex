package com.money.manager.ex;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class IntroductionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction_activity);
		TextView txtIntroduction = (TextView) findViewById(R.id.textViewIntroduction);
		txtIntroduction.setText(Html.fromHtml(MoneyManagerApplication.getRawAsString(this, R.raw.introduction)));
		
		Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/rabiohead.ttf");
	    txtIntroduction.setTypeface(myTypeface);
		
	}
}
