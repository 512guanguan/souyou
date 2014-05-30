package com.llb.souyou.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "souyou";
	private static final int DB_VERSION = 1;

	public  SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	/**
	 * 构造函数，用来打开或者新建数据库
	 * @param context
	 * @param name 数据库名字
	 * @param factory CursorFactory
	 * @param version int 数据库版本
	 */
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		//分类表的缓存category(id,cate_id,parent_id,title,imageurl,desc,time)
		db.execSQL("create table if not exists  category (id integer primary key autoincrement," +
				"cate_id integer,parent_id integer,title text,imageurl text, desc text,time integer)");
		//下载状态表 download(id,app_id,imageurl,title,rate,app_down,size,status,progress);
		//下载状态status:-1=未有下载操作   0=failed 1=success但未安装  2=paused 3=loading
		db.execSQL("create table if not exists download(" +
				"app_id integer primary key,imageurl text,title text,rate real,app_down integer," +
				"size integer,status integer,progress integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if (oldVersion == newVersion) {
			return;
		}
	}

	public void insert(String Table_Name, ContentValues values) {
		if (db == null)
			db = getWritableDatabase();
		db.insert(Table_Name, null, values);
//		db.close();
	}
	/**
	 * 
	 * @param Table_Name
	 * @param id
	 * @return 影响行数
	 */
	public int delete(String Table_Name, int id) {
		if (db == null)
			db = getWritableDatabase();
		return db.delete(Table_Name, BaseColumns._ID + "=?",
				new String[] { String.valueOf(id) });
	}
	/**
	 * @param Table_Name
	 * @param values
	 * @param WhereClause
	 * @param whereArgs
	 * @return 影响行数
	 */
	public int update(String Table_Name, ContentValues values,
			String WhereClause, String[] whereArgs) {
		if (db == null) {
			db = getWritableDatabase();
		}
		return db.update(Table_Name, values, WhereClause, whereArgs);
	}
	public void replace(String table,ContentValues values){
		if(db==null){
			db=getWritableDatabase();
		}
		db.replace(table, null, values);
	}

	public Cursor query(String Table_Name, String[] columns, String whereStr,
			String[] whereArgs) {
		if (db == null) {
			db = getReadableDatabase();
		}
		return db.query(Table_Name, columns, whereStr, whereArgs, null, null,
				null);
	}

	public Cursor rawQuery(String sql, String[] args) {
		if (db == null) {
			db = getReadableDatabase();
		}
		return db.rawQuery(sql, args);
	}

	public void ExecSQL(String sql) {
		if (db == null) {
			db = getWritableDatabase();
		}
		db.execSQL(sql);
	}

	public void closeDb() {
		if (db != null) {
			db.close();
			db = null;
		}
	}

}
