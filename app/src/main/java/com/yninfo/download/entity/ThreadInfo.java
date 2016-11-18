package com.yninfo.download.entity;

import java.io.Serializable;

/**
 * Created by zhaozhiping on 17/11/2016.
 * 线程信息
 */

public class ThreadInfo implements Serializable {

    private int id;

    private String url;

    private long start;

    private long end;

    private long finished;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String url, long start, long end, long finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "finished='" + finished + '\'' +
                ", end='" + end + '\'' +
                ", start='" + start + '\'' +
                ", url='" + url + '\'' +
                ", id=" + id +
                '}';
    }
}
