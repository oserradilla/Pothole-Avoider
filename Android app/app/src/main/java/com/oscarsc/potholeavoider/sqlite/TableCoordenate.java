package com.oscarsc.potholeavoider.sqlite;


public class TableCoordenate {
	public static final String TABLE_NAME = "coordinate";
	public static final String CN_LATITUDE = "latitude";
	public static final String CN_LONGITUDE = "longitude";
	public static final String CN_VER_SLOPE = "ver_slope";
	public static final String CN_HOR_SLOPE ="hor_slope";
	public static final String CN_POTH_ID ="poth_id";
	public static final String PK_LAT_LONG ="constraint COORDINATE_LATITUDE_LONGITUDE_PK primary key("+
				CN_LATITUDE+","+CN_LONGITUDE+")";
	public static final String FK_COOR_POTHID = "constraint COORDINATE_POTHID_FK foreign key("+
			CN_POTH_ID+") references "+TablePothole.TABLE_NAME+"("+TablePothole.CN_POTH_ID+")";
	
	public static final String CREATE_TABLE = "create table "+TABLE_NAME+"( "
			+CN_LATITUDE+" decimal, "
			+CN_LONGITUDE+" decimal, "
			+CN_VER_SLOPE+" decimal, "
			+CN_HOR_SLOPE+" decimal,"
			+CN_POTH_ID+" integer,"
			+PK_LAT_LONG+" , "
			+FK_COOR_POTHID
					+ ");";
	public static final String INS_BASE = "insert into "+TABLE_NAME
			+"values(";
	
	public static String makeInsert(double latitude,double longitude, double ver_slope, double hor_slope,
				long poth_id){
		return INS_BASE+latitude+","
				+longitude+","
				+ver_slope+","
				+hor_slope+","
				+poth_id
				+");";
	}
	
}
