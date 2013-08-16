package com.money.manager.ex.chart;

import android.os.Parcel;
import android.os.Parcelable;

public class ValuePieChart implements Parcelable {
	private String category;
	private double value;
	private String valueFormatted;
	
	public ValuePieChart() {
	} 
	
	public ValuePieChart(String category, double value) {
		setCategory(category);
		setValue(value);
	}
	
	public ValuePieChart(String category, double value, String valueFormatted) {
		setCategory(category);
		setValue(value);
		setValueFormatted(valueFormatted);
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the values
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param values the values to set
	 */
	public void setValue(double values) {
		this.value = values;
	}

	/**
	 * @return the valuesFormatted
	 */
	public String getValueFormatted() {
		return valueFormatted;
	}

	/**
	 * @param valuesFormatted the valuesFormatted to set
	 */
	public void setValueFormatted(String valuesFormatted) {
		this.valueFormatted = valuesFormatted;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getCategory());
		dest.writeDouble(getValue());
		dest.writeString(getValueFormatted());
	}
	
	public final static Parcelable.Creator<ValuePieChart> CREATOR = new Creator<ValuePieChart>() {
		
		@Override
		public ValuePieChart[] newArray(int size) {
			return new ValuePieChart[size];
		}
		
		@Override
		public ValuePieChart createFromParcel(Parcel source) {
			return new ValuePieChart(source.readString(), source.readDouble(), source.readString());
		}
	};
}
