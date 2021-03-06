package com.yninfo.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhaozhiping on 17/11/2016.
 * 数据库帮助类
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static int version = 1;
    private static DBHelper instance = null;

    public static  DBHelper getInstance(Context context){
        if(instance==null)
            instance = new DBHelper(context);
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateThreadInfoSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(getDropThreadInfoSql());
        db.execSQL(getCreateThreadInfoSql());
    }

    private String getCreateThreadInfoSql(){
        String sql = " create table thread_info(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " thread_id INTEGER,url text,start INTEGER,end INTEGER,finished INTEGER," +
                "create_time timestamp not null default CURRENT_TIMESTAMP,finish_time timestamp )  ";
        return sql;
    }

    private String getDropThreadInfoSql(){
        return "drop table  if exists thread_info;";
    }
}
