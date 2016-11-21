package com.yninfo.download.db;

import com.yninfo.download.entity.ThreadInfo;

import java.util.List;

/**
 * Created by zhaozhiping on 17/11/2016.
 */

public interface ThreadDao {

    /**
     * 插入线程信息
     * @param threadInfo
     */
    public void insertThreadInfo(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     * @param url
     * @param thread_id
     */
    public void deleteThreadInfo(String url,int thread_id);

    public void deleteThread(String url);

    /**
     * 更新下载进度
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThreadInfo(String url, int thread_id,long finished);

    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param thread_id
     * @return
     */
    public boolean isExists(String url ,int thread_id);
}
