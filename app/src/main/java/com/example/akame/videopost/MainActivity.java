package com.example.akame.videopost;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String rootPath;
    private int REQUEST_VIDEO_CODE = 0x11;
    private ImageView ivTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivTest = findViewById(R.id.iv_test);
        rootPath = Environment.getExternalStorageDirectory().toString();
        LogUtil.e(rootPath);
        findViewById(R.id.btn).setOnClickListener(v -> {
//            doMp4Append();
            /*getVideoFile();*/
            getVideoFiles();
        });
    }

    /**
     * 执行MP4的追加合成
     */
    private void doMp4Append() {
        try {
            List<String> mp4PathList = new ArrayList<>();
            mp4PathList.add(rootPath + "/v001" + ".mp4");
            mp4PathList.add(rootPath + "/v002" + ".mp4");
            mp4PathList.add(rootPath + "/v003" + ".mp4");
            String outPutPath = rootPath + "/test.mp4";
            Mp4ParseUtil.appendMp4List(mp4PathList, outPutPath);
            LogUtil.e("合并追加完成");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("异常");
        }
    }

    private void getVideoFile() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("TAG", "============" + uri.toString());
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // 视频路径：MediaStore.Audio.Media.DATA
            String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            // 视频缩略图路径：MediaStore.Images.Media.DATA
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Log.e("TAG", videoPath + "================" + imagePath);
        }
    }

    private List<Bitmap> bitmapList = new ArrayList<>();

    private void getVideoFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projections = {
                        MediaStore.Video.Media.DISPLAY_NAME,//名称
                        MediaStore.Video.Media.DURATION,//时长
                        MediaStore.Video.Media.SIZE,//大小
                        MediaStore.Video.Media.DATA,//路径
                };
                Cursor cursor = getContentResolver().query(uri, projections, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC ");
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    String duration = cursor.getString(1);
                    String size = cursor.getString(2);//查询出来一byte
                    String data = cursor.getString(3);
                    Log.e("TAG", "==name==" + name + "==duration==" + duration + "==size==" + size + "==data==" + data);
                    Bitmap bitmap = null;
                    // 获取视频的缩略图
                    bitmap = ThumbnailUtils.createVideoThumbnail(data, MediaStore.Video.Thumbnails.MINI_KIND);
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight(),
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    bitmapList.add(bitmap);
                }
                handler.sendEmptyMessage(0x11);
                cursor.close();
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11 && bitmapList.size() > 0) {
                ivTest.setImageBitmap(bitmapList.get(0));
            }
        }
    };
}
