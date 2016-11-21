package com.yninfo.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.yninfo.download.adapter.FileListAdapter;
import com.yninfo.download.entity.FileInfo;
import com.yninfo.download.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
   private ListView lvFile;

    private List<FileInfo> fileInfos = null;

    private FileListAdapter mAdapter;

    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initData();

        //创建适配器
        mAdapter = new FileListAdapter(mContext,fileInfos);
        lvFile.setAdapter(mAdapter);

        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        intentFilter.addAction(DownloadService.ACTION_FINISH);
        registerReceiver(mReceiver,intentFilter);
    }

    private void initData() {
        fileInfos = new ArrayList<>();
        //创建一个文件信息对象
        FileInfo fileInfo = new FileInfo(0,
                "http://wap.apk.anzhi.com/data2/apk/201610/20/1539651cfe60bb5370df8871b6ab974b_03474600.apk",
                "腾讯新闻.apk",0,0);
        FileInfo fileInfo1 = new FileInfo(1,
                "http://wap.apk.anzhi.com/data2/apk/201409/02/com.cutt.zhiyue.android.app236492_11711100.apk",
                "参考消息.apk",0,0);
        FileInfo fileInfo2 = new FileInfo(2,
                "http://wap.apk.anzhi.com/data2/apk/201611/10/602752a5593e50a1de6dc3bf92a72779_95097600.apk",
                "百度地图.apk",0,0);
        FileInfo fileInfo3 = new FileInfo(3,
                "http://wap.apk.anzhi.com/data2/apk/201611/08/d27a08db473abf23a9fa0dc5cbca8bcf_04637200.apk",
                "糗事百科.apk",0,0);
        fileInfos.add(fileInfo);
        fileInfos.add(fileInfo1);
        fileInfos.add(fileInfo2);
        fileInfos.add(fileInfo3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 更新ui的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadService.ACTION_UPDATE)){
                long finished = intent.getLongExtra("finished",0);
                int id = intent.getIntExtra("id",0);
                /*if(finished==100){
                    Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
                    File file = new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFileName());
                    if(file.exists()){
                        Intent it = new Intent();
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        it.setAction(android.content.Intent.ACTION_VIEW);
                        it.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        startActivity(it);
                    }

                }*/
                Log.d(TAG, "onReceive: "+fileInfos.get(id).getFileName()+",finished:"+finished);
                mAdapter.updateProgress(id,(int)finished);
            }else if(DownloadService.ACTION_FINISH==intent.getAction()){
                //更新进度
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                mAdapter.updateProgress(fileInfo.getId(),100);
                Toast.makeText(mContext, fileInfo.getFileName()+"下载完成", Toast.LENGTH_SHORT).show();
            }
        }
    };




    private void initView() {
       lvFile = (ListView) findViewById(R.id.lv_file);
    }
}
