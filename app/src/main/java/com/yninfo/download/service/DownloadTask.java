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
import java.util.ArrayList;
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

    private int mThreadCount = 1;   //线程数量

    private List<DownloadThread> mThreadList = null;

    public DownloadTask(Context mContext, FileInfo mFileInfo,int threadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = threadCount;
        mDao = new ThreadDaoImpl(mContext);
    }

    public void download(){
         //读取数据库的线程信息,获取下载进度
        List<ThreadInfo> threadInfos =  mDao.getThreads(mFileInfo.getUrl());
        if (threadInfos.size()==0) {
            //获得每个线程下载的长度
            long length = mFileInfo.getLength()/mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                //创建线程信息
                ThreadInfo threadInfo = new ThreadInfo(i,mFileInfo.getUrl(),
                        length*i,(i+1)*length-1,0);
                if(i==mThreadCount-1){
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //添加到线程信息集合中
                threadInfos.add(threadInfo);
                mDao.insertThreadInfo(threadInfo);
            }
        }
        //启动多个线程进行加载
        mThreadList = new ArrayList<>();
        for (ThreadInfo info: threadInfos
             ) {
            DownloadThread thread = new DownloadThread(info);
            thread.start();
            //添加到线程集合
            mThreadList.add(thread);
        }
    }

    /**
     * 判断是否所有线程都执行完毕
     *
     */
    private synchronized void checkAllThreadFinish(){
        boolean allFinished = true;
        //遍历线程集合，判断线程是否都执行完毕
        for (DownloadThread thread: mThreadList
             ) {
            if(!thread.isFinished){
                allFinished = false;
                break;
            }
        }
        if(allFinished){
            // 删除下载记录
            mDao.deleteThread(mFileInfo.getUrl());
            //发送广播通知下载结束
            Intent it = new Intent();
            it.setAction(DownloadService.ACTION_FINISH);
            it.putExtra("fileInfo",mFileInfo);
            mContext.sendBroadcast(it);
        }
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {

        private ThreadInfo threadInfo = null;

        public boolean isFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {


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
                        //累加整个文件的完成进度
                        mFinished+=len;
                        //累加每个线程完成的进度
                        threadInfo.setFinished(threadInfo.getFinished()+len);
                        if(System.currentTimeMillis()-time>500){
                            time = System.currentTimeMillis();
                            //把下载进度广播到activity
                      //      Log.d(TAG, "finished: "+mFinished+",length:"+mFileInfo.getLength()+",progress:"+mFinished  * 100/mFileInfo.getLength());
                            intent.putExtra("finished",mFinished  * 100/mFileInfo.getLength());
                            intent.putExtra("id",mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //下载暂停时，保存下载进度
                        if(isPaused){
                            mDao.updateThreadInfo(threadInfo.getUrl(),threadInfo.getId(),threadInfo.getFinished());
                            return;
                        }

                    }

                    isFinished = true;
                    //检查下载任务是否执行完毕
                    checkAllThreadFinish();

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
