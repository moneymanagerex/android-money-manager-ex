package com.money.manager.ex.adapter;

public class DrawerMenuItem {
	private int mId;
	private String mItemText;
	private Integer mIcon;
	
	public DrawerMenuItem(int id, String itemText) {
		setId(id);
		setItemText(itemText);
	}
	
	public DrawerMenuItem(int id, String itemText, Integer icon) {
		setId(id);
		setItemText(itemText);
		setIcon(icon);
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

	public Integer getIcon() {
		return mIcon;
	}

	public void setIcon(Integer mIcon) {
		this.mIcon = mIcon;
	}
}
