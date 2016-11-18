package com.yninfo.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yninfo.download.entity.FileInfo;
import com.yninfo.download.utils.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhaozhiping on 17/11/2016.
 */

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    public static final String DOWNLOAD_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;

    private DownloadTask mTask = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 获得activity传来的参数
        if (ACTION_START.equals(intent.getAction())) {
            Log.d(TAG, "onStartCommand: 开始下载");
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d(TAG, "onStartCommand: "+fileInfo.toString());
            //  启动初始化线程
            new InitThread(fileInfo).start();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            Log.d(TAG, "onStartCommand: 结束下载");
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            if(mTask!=null){
                mTask.isPaused = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHanler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo file = (FileInfo) msg.obj;
                    Log.d(TAG, "init: 收到文件" + file.toString());
                    mTask = new DownloadTask(DownloadService.this,file);
                    mTask.download();
                    break;
            }
        }
    };

    class InitThread extends Thread {
        private FileInfo fileInfo = null;

        public InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                long length = -1;
                if (conn.getResponseCode() == HttpStatus.SC_OK) {
                    //获得文件长度
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    Log.d(TAG, "run: 文件长度小于等于0");
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //本地创建文件
                File file = new File(dir, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");


                //设置文件长度
                raf.setLength(length);
                fileInfo.setLength(length);
                mHanler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();
            } catch (Exception e) {
                Log.d(TAG, "run: 文件下载异常: " + e.toString());
            } finally {
                try {
                    if (raf != null)
                        raf.close();
                    if(conn!=null)
                        conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
