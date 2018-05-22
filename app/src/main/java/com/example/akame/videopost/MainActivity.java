package com.example.akame.videopost;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootPath = Environment.getExternalStorageDirectory().toString();
        LogUtil.e(rootPath);
    }

    /**
     * 执行MP4的追加合成
     */
    private void doMp4Append() {
        try {
            List<String> mp4PathList = new ArrayList<>();
            mp4PathList.add(rootPath + "/resource/" + "video1" + ".mp4");
            mp4PathList.add(rootPath + "/resource/" + "video2" + ".mp4");
            String outPutPath = rootPath + "/output/" + "outVideo" + ".mp4";
            Mp4ParseUtil.appendMp4List(mp4PathList, outPutPath);
            LogUtil.e("合并追加完成");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("异常");
        }
    }

}
