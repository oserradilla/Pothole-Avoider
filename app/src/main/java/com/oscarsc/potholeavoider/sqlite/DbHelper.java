package com.oscarsc.potholeavoider.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	private static final String DB_NAME = "potholeavoider.sqlite";
	private static final int DB_SCHEME_VERSION = 1;
	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_SCHEME_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/*It's important the order of creating tables because the table coordinate contains a parameter which is a 
		Foreign Key that refers to a parameter of the poth table*/
		System.out.println(TablePothole.CREATE_TABLE);
		System.out.println(TableCoordenate.CREATE_TABLE);
		db.execSQL(TablePothole.CREATE_TABLE);
		db.execSQL(TableCoordenate.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
