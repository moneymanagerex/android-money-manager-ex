/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.reports;

import android.os.Parcel;
import android.os.Parcelable;

public class ValuePieEntry implements Parcelable {
    private String mText;
    private double mValue;
    private String mValueFormatted;

    public ValuePieEntry() {
    }

    public ValuePieEntry(String text, double value) {
        setText(text);
        setValue(value);
	}

    public ValuePieEntry(String text, double value, String valueFormatted) {
        setText(text);
        setValue(value);
		setValueFormatted(valueFormatted);
	}

	/**
     * @return the text
     */
    public String getText() {
        return mText;
    }

	/**
     * @param text the text to set
     */
    public void setText(String text) {
        mText = text;
    }

	/**
	 * @return the values
	 */
	public double getValue() {
        return mValue;
    }

	/**
     * @param value the values to set
     */
    public void setValue(double value) {
        mValue = value;
    }

	/**
	 * @return the valuesFormatted
	 */
	public String getValueFormatted() {
        return mValueFormatted;
    }

	/**
     * @param valueFormatted the valuesFormatted to set
     */
    public void setValueFormatted(String valueFormatted) {
        mValueFormatted = valueFormatted;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getText());
        dest.writeDouble(getValue());
		dest.writeString(getValueFormatted());
	}

    public final static Parcelable.Creator<ValuePieEntry> CREATOR = new Creator<ValuePieEntry>() {

        @Override
        public ValuePieEntry[] newArray(int size) {
            return new ValuePieEntry[size];
        }
		
		@Override
        public ValuePieEntry createFromParcel(Parcel source) {
            return new ValuePieEntry(source.readString(), source.readDouble(), source.readString());
        }
	};
}
