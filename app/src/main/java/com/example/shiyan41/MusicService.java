package com.example.shiyan41;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class MusicService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MyBinder myBinder;

    private static final int SET_SEEKBAR_MAX = 3;
    private static final int UPDATE_PROGRESS = 1;
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();

    }




    @Override
    public IBinder onBind(Intent intent) {
        // return the binder to the activity
        myBinder = new MyBinder();
        return myBinder;
    }
    //控制播放或暂停
    public void start() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Intent intent1 = new Intent("pauseimage");
                sendBroadcast(intent1);
            } else {
                mediaPlayer.start();
                Intent intent2 = new Intent("playimage");
                sendBroadcast(intent2);
            }
        }
    }
    //得到一首音乐的地址，播放这首音乐
    public void startnew(String path) throws Exception {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();}

        mediaPlayer.release();
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent12 = new Intent("nextsong");
                sendBroadcast(intent12);

            }
        });
        Intent intent1 = new Intent("playimage");
        sendBroadcast(intent1);
        handler.sendEmptyMessage(SET_SEEKBAR_MAX);
        handler.sendEmptyMessage(UPDATE_PROGRESS);
        /*mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 开始播放音乐
                mp.start();
                handler.sendEmptyMessage(SET_SEEKBAR_MAX);
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });*/
    }
  //handler 发送和处理消息 跨线程通信
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_PROGRESS:
                // 设置当前播放进度
                Intent intent = new Intent("seekbarprogress");
                intent.putExtra("seekbarprogress",
                        mediaPlayer.getCurrentPosition());//获取当前播放位置
                sendBroadcast(intent);
                // 需要随着播放设置
                handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                break;
            case SET_SEEKBAR_MAX:
                intent = new Intent("seekbarmaxprogress");
                intent.putExtra("seekbarmaxprogress", mediaPlayer.getDuration());
                sendBroadcast(intent);//发送广播
                // 因为进度条只需要设置一次就够了,所以不需要反复发送Message;
                break;
            default:
                break;
              }
            };
           };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("startnew")) {
            try {


                startnew(intent.getStringExtra("url"));
               //播放音乐
                Intent titleintent = new Intent("gettitle");
                titleintent.putExtra("title",intent.getStringExtra("title"));
                titleintent.putExtra("url",intent.getStringExtra("url"));
                titleintent.putExtra("artist",intent.getStringExtra("artist"));
                sendBroadcast (titleintent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (intent.getAction().equals("changed")) {// 如果是拖动的Action,就跳至拖动的进度
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(intent.getIntExtra("seekbarprogress", 0));
            }
        }
        return super.onStartCommand(intent,flags,startId);
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}