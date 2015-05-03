/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePreference extends DialogPreference implements TimePicker.OnTimeChangedListener {
	private String timeString;
	private String changedValueCanBeNull;

	public TimePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TimePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Produces a TimePicker set to the time produced by {@link #getTime()}. When overriding be sure to call the super.
	 * 
	 * @return a DatePicker with the date set
	 */
	@Override
	protected View onCreateDialogView() {
		TimePicker timePicker = new TimePicker(getContext());
		timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
		Calendar calendar = getTime();
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		timePicker.setOnTimeChangedListener(this);
		return timePicker;
	}

	/**
	 * Produces the time used for the time picker. If the user has not selected a time, produces the default from the XML's android:defaultValue. If the default
	 * is not set in the XML or if the XML's default is invalid it uses the value produced by {@link #defaultCalendar()}.
	 * 
	 * @return the Calendar for the time picker
	 */
	public Calendar getTime() {
		try {
			Date date = formatter().parse(defaultValue());
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return cal;
		} catch (java.text.ParseException e) {
			return defaultCalendar();
		}
	}

	/**
	 * Set the selected time to the specified string.
	 * 
	 * @param dateString
	 *            The date, represented as a string, in the format specified by {@link #formatter()}.
	 */
	public void setTime(String timeString) {
		this.timeString = timeString;
	}

	/**
	 * Produces the date formatter used for times in the XML. The default is HH:mm. Override this to change that.
	 * 
	 * @return the SimpleDateFormat used for XML times
	 */
	public static DateFormat formatter() {
		return new SimpleDateFormat("HH:mm");
	}

	/**
	 * Produces the date formatter used for showing the time in the summary. Override this to change it.
	 * 
	 * @return the SimpleDateFormat used for summary dates
	 */
	public static DateFormat summaryFormatter(Context context) {
		return android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	/**
	 * Called when the time picker is shown or restored. If it's a restore it gets the persisted value, otherwise it persists the value.
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object def) {
		if (restoreValue) {
			this.timeString = getPersistedString(defaultValue());
			setTheTime(this.timeString);
		} else {
			boolean wasNull = this.timeString == null;
			setTime((String) def);
			if (!wasNull)
				persistTime(this.timeString);
		}
	}

	/**
	 * Called when Android pauses the activity.
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		if (isPersistent())
			return super.onSaveInstanceState();
		else
			return new SavedState(super.onSaveInstanceState());
	}

	/**
	 * Called when Android restores the activity.
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			if (state == null || !state.getClass().equals(SavedState.class)) {
				super.onRestoreInstanceState(state);
				setTheTime(((SavedState) state).dateValue);
			} else {
				SavedState s = (SavedState) state;
				super.onRestoreInstanceState(s.getSuperState());
				setTheTime(s.dateValue);
			}
		} catch (Exception e) {
			Log.e(TimePreference.class.getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Called when the user changes the time.
	 */
	public void onTimeChanged(TimePicker view, int hour, int minute) {
		Calendar selected = new GregorianCalendar(1970, 0, 1, hour, minute);
		this.changedValueCanBeNull = formatter().format(selected.getTime());
	}

	/**
	 * Called when the dialog is closed. If the close was by pressing DialogInterface.BUTTON_POSITIVE it saves the value.
	 */
	@Override
	protected void onDialogClosed(boolean shouldSave) {
		if (shouldSave && this.changedValueCanBeNull != null) {
			setTheTime(this.changedValueCanBeNull);
			this.changedValueCanBeNull = null;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (getDialog().getCurrentFocus() != null)
			getDialog().getCurrentFocus().clearFocus();
	}

	private void setTheTime(String s) {
		setTime(s);
		persistTime(s);
	}

	private void persistTime(String s) {
		persistString(s);
		setSummary(summaryFormatter(getContext()).format(getTime().getTime()));
	}

	/**
	 * The default time to use when the XML does not set it or the XML has an error.
	 * 
	 * @return the Calendar set to the default date
	 */
	public static Calendar defaultCalendar() {
		return new GregorianCalendar(1970, 0, 1, 0, 0);
	}

	/**
	 * The defaultCalendar() as a string using the {@link #formatter()}.
	 * 
	 * @return a String representation of the default time
	 */
	public static String defaultCalendarString() {
		return formatter().format(defaultCalendar().getTime());
	}

	private String defaultValue() {
		if (this.timeString == null)
			setTime(defaultCalendarString());
		return this.timeString;
	}

	/**
	 * Produces the date the user has selected for the given preference, as a calendar.
	 * 
	 * @param preferences
	 *            the SharedPreferences to get the date from
	 * @param field
	 *            the name of the preference to get the date from
	 * @return a Calendar that the user has selected
	 */
	public static Calendar getTimeFor(SharedPreferences preferences, String field) {
		Date date = stringToDate(preferences.getString(field, defaultCalendarString()));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	private static Date stringToDate(String timeString) {
		try {
			return formatter().parse(timeString);
		} catch (ParseException e) {
			return defaultCalendar().getTime();
		}
	}

	private static class SavedState extends BaseSavedState {
		String dateValue;

		public SavedState(Parcel p) {
			super(p);
			dateValue = p.readString();
		}

		public SavedState(Parcelable p) {
			super(p);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(dateValue);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}