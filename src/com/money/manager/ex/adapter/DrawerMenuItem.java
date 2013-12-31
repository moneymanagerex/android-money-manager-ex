package com.money.manager.ex.adapter;

public class DrawerMenuItem {
	private int mId;
	private String mItemText;
	
	public DrawerMenuItem(int id, String itemText) {
		setId(id);
		setItemText(itemText);
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public String getItemText() {
		return mItemText;
	}

	public void setItemText(String itemText) {
		this.mItemText = itemText;
	}
}
