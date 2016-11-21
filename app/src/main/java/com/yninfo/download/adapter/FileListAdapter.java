package com.yninfo.download.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yninfo.download.R;
import com.yninfo.download.entity.FileInfo;
import com.yninfo.download.service.DownloadService;

import java.util.List;

/**
 * Created by zhaozhiping on 20/11/2016.
 */

public class FileListAdapter extends BaseAdapter {
    private Context mContext;
    private List<FileInfo> list;

    public FileListAdapter(Context context,List<FileInfo> fileInfos) {
        mContext = context;
        list = fileInfos;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return list.get(i).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(view==null){
            //加载试图
            view = LayoutInflater.from(mContext).inflate(R.layout.item_file,null);
            //获得布局中的控件
            holder = new ViewHolder();
            holder.tvName = (TextView) view.findViewById(R.id.tvName);
            holder.pbProgress = (ProgressBar) view.findViewById(R.id.pbProgress);
            holder.btnStop = (Button) view.findViewById(R.id.btnStop);
            holder.btnStart = (Button) view.findViewById(R.id.btnStart);

            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        //设置控件
        final FileInfo fileInfo = list.get(position);
        holder.tvName.setText(fileInfo.getFileName());
        holder.pbProgress.setMax(100);
        holder.pbProgress.setProgress((int)fileInfo.getFinished());
        holder.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(mContext, DownloadService.class);
                it.setAction(DownloadService.ACTION_START);
                it.putExtra("fileInfo",fileInfo);
                mContext.startService(it);
            }
        });
        holder.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it1 = new Intent(mContext, DownloadService.class);
                it1.setAction(DownloadService.ACTION_STOP);
                it1.putExtra("fileInfo",fileInfo);
                mContext.startService(it1);
            }
        });
        return view;
    }

    //更新列表项中的进度条
    public void updateProgress(int id,int finished){
        //TODO 当前取文件信息有缺陷，仅针对于有文件id等于position的情况
        FileInfo fileInfo = list.get(id);
        fileInfo.setFinished(finished);
        notifyDataSetChanged();
    }

    static class ViewHolder{
         TextView tvName;
         ProgressBar pbProgress;
         Button btnStop;
         Button btnStart;
    }
}
