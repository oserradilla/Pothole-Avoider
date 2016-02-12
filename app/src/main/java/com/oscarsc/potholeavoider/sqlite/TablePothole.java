package com.oscarsc.potholeavoider.sqlite;

public class TablePothole {
	public static final String TABLE_NAME = "pothole";
	public static final String CN_POTH_ID ="poth_id";
	public static final String CN_POTH_DESC = "poth_desc";
	public static final String CREATE_TABLE = "create table "+TABLE_NAME+"( "
			+CN_POTH_ID+" integer primary key autoincrement, "
			+CN_POTH_DESC+" text"
					+ ");";
	/*public static final String INS_BASE = "insert into "+TABLE_NAME
			+"values(";
	public static String makeInsert(String ){
		return INS_BASE+"default,"+;
	}*/
}
