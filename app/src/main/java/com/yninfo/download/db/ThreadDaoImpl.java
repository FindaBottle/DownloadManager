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
        dbHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThreadInfo(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "insert into thread_info (thread_id,url,start,end,finished) values (?,?,?,?,?)";
        db.execSQL(sql
        ,new Object[]{threadInfo.getId(),threadInfo.getUrl(),
                        threadInfo.getStart(),threadInfo.getEnd(),
                        threadInfo.getFinished()});
        db.close();
    }

    @Override
    public synchronized void deleteThreadInfo(String url, int thread_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from thread_info where url =? and thread_id =?";
        db.execSQL(sql
                ,new Object[]{url,thread_id});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?",
                new Object[]{url});
        db.close();
    }

    @Override
    public synchronized void updateThreadInfo(String url, int thread_id, long finished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update thread_info set finished = ? where url =? and thread_id =?";
        db.execSQL(sql
                ,new Object[]{finished,url,thread_id});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        String sql = "select * from thread_info where url =?";
        Cursor cursor = db.rawQuery(sql,new String[]{url});
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                ThreadInfo info = new ThreadInfo();
                info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
                info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
                info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));

                list.add(info);
            }
        }

        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url, thread_id+""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
