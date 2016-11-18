package com.yninfo.download.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yninfo.download.db.ThreadDao;
import com.yninfo.download.db.ThreadDaoImpl;
import com.yninfo.download.entity.FileInfo;
import com.yninfo.download.entity.ThreadInfo;
import com.yninfo.download.utils.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by zhaozhiping on 17/11/2016.
 * 下载任务类
 */

public class DownloadTask {

    private Context mContext = null;

    private FileInfo mFileInfo = null;

    private ThreadDao mDao = null;

    private long mFinished = 0;

    public boolean isPaused = false;

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDaoImpl(mContext);
    }

    public void download(){
         //读取数据库的线程信息
        List<ThreadInfo> threadInfos =  mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size()==0) {
            threadInfo = new ThreadInfo();
            threadInfo.setUrl(mFileInfo.getUrl());
            threadInfo.setStart(0);
            threadInfo.setEnd(mFileInfo.getLength());
            threadInfo.setFinished(0);
        }else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程开始下载
        new DownloadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {

        private ThreadInfo threadInfo = null;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            //数据库中插入线程信息
            if(mDao.isExists(threadInfo.getUrl(),threadInfo.getId())){
                mDao.insertThreadInfo(threadInfo);
            }

            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                long start = threadInfo.getStart()+threadInfo.getFinished();
                conn.setRequestProperty("Range","bytes="+start+"-"+threadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                raf.seek(start);
                Intent intent = new Intent();
                intent.setAction(DownloadService.ACTION_UPDATE);
                mFinished+=threadInfo.getFinished();
                //开始下载
                if(conn.getResponseCode()== HttpStatus.SC_PARTIAL_CONTENT){
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024*4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len=input.read(buffer))!=-1){
                        //写入文件
                        raf.write(buffer,0,len);
                        //把下载进度广播到activity
                        mFinished+=len;
                        if(System.currentTimeMillis()-time>500){
                            time = System.currentTimeMillis();
                            Log.d(TAG, "finished: "+mFinished+",length:"+mFileInfo.getLength()+",progress:"+mFinished  * 100/mFileInfo.getLength());
                            intent.putExtra("finished",mFinished  * 100/mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //下载暂停时，保存下载进度
                        if(isPaused){
                            mDao.updateThreadInfo(threadInfo.getUrl(),threadInfo.getId(),mFinished);
                            return;
                        }

                    }
                    intent.putExtra("finished",100l);
                    mContext.sendBroadcast(intent);
                    //删除线程信息
                    mDao.deleteThreadInfo(threadInfo.getUrl(),threadInfo.getId());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    if (raf != null)
                        raf.close();
                    if(conn!=null)
                        conn.disconnect();
                    if(input!=null)
                        input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: 输入流关闭异常"+e.toString());
                }
            }


        }
    }
}
