package com.money.manager.ex.database;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * Class to parse in content provider for generate rawQuery
 */
public class SQLDataSet extends Dataset {
	public SQLDataSet() {
		super(null, DatasetType.SQL, "sql");
	}
	
	@Override
	public String[] getAllColumns() {
		return null;
	}
}
