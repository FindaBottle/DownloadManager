package com.yninfo.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yninfo.download.entity.FileInfo;
import com.yninfo.download.service.DownloadService;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private TextView tvName;
    private ProgressBar pbProgress;
    private Button btnStop;
    private Button btnStart;

    private Context mContext;

    FileInfo fileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        //创建一个文件信息对象
        fileInfo = new FileInfo(0,
                "http://wap.apk.anzhi.com/data2/apk/201610/20/1539651cfe60bb5370df8871b6ab974b_03474600.apk",
                "腾讯新闻.apk",0,0);
        setListener();
        pbProgress.setMax(100);
        tvName.setText(fileInfo.getFileName() );
        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver,intentFilter);
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
                pbProgress.setProgress((int) finished);
               // Log.d(TAG, "下载完成："+finished);
                if(finished==100){
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

                }

            }
        }
    };


    private void setListener() {
        View.OnClickListener clr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.btnStart:
                        Intent it = new Intent(mContext, DownloadService.class);
                        it.setAction(DownloadService.ACTION_START);
                        it.putExtra("fileInfo",fileInfo);
                        startService(it);
                        break;
                    case R.id.btnStop:
                        Intent it1 = new Intent(mContext, DownloadService.class);
                        it1.setAction(DownloadService.ACTION_STOP);
                        it1.putExtra("fileInfo",fileInfo);
                        startService(it1);
                        break;
                }
            }
        };
        btnStop.setOnClickListener(clr);
        btnStart.setOnClickListener(clr);
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.tvName);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
    }
}
