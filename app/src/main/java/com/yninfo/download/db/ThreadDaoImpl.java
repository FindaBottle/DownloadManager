package com.yninfo.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yninfo.download.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaozhiping on 17/11/2016.
 * 数据访问接口实现
 */

public class ThreadDaoImpl implements ThreadDao {

    private DBHelper dbHelper = null;

    public ThreadDaoImpl(Context context) {
        dbHelper = new DBHelper(context);
    }

    @Override
    public void insertThreadInfo(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "insert int thread_info (thread_id,url,start,end,finish) values (?,?,?,?,?)";
        db.execSQL(sql
        ,new Object[]{threadInfo.getId(),threadInfo.getUrl(),
                        threadInfo.getStart(),threadInfo.getEnd(),
                        threadInfo.getFinished()});
        db.close();
    }

    @Override
    public void deleteThreadInfo(String url, int thread_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from thread_info where url =? and thread_id =?";
        db.execSQL(sql
                ,new Object[]{url,thread_id});
        db.close();
    }

    @Override
    public void updateThreadInfo(String url, int thread_id, long finished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update thread_info set finished = ? where url =? and thread_id =?";
        db.execSQL(sql
                ,new Object[]{finished,url,thread_id});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        String sql = "select * from thread_info where url =?";
        Cursor cursor = db.rawQuery(sql,new String[]{url});
        while (cursor.moveToNext()){
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("id")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            info.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            info.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));

            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from thread_info where url =? and thread_id = ?";
        Cursor cursor = db.rawQuery(sql,new String[]{url,thread_id+""});
        boolean exists = cursor.moveToNext();
        db.close();
        return exists;
    }
}
