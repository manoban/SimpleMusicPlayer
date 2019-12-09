package com.example.shiyan41;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

//import com.example.musicplayer50.R;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
//Service通过广播的形式发送boardcast，我们写一个boardcastReceiver即可。通常的情况下，将
//boardcastReceiver写成Activity的内部类，这个onReceiver可以直接调用activity的方法来更新界面。

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button localmusic;
    private Button play;
    private Button playlist;
    private Button playnext;
    private Button playlast;
    private Button suiji;
    private SeekBar seekBar;
    private TextView textView2;
    private TextView textView;
    private MusicService musicService;
    private TabledatabaseHelper dbHelper;
    private String CurrentTitle = "CurrentTitle";
    private Animation panAnim;
    private ImageView pan;



    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("seekbarmaxprogress");
        filter.addAction("seekbarprogress");
        filter.addAction("gettitle");
        filter.addAction("pauseimage");
        filter.addAction("playimage");
        filter.addAction("nextsong");
        //注册广播接收器
        registerReceiver(broadcastReceiver, filter);

        //创建MusicIntent from MusicService
        Intent MusicIntent = new Intent(this, MusicService.class);
        //绑定
        bindService(MusicIntent, conn, Context.BIND_AUTO_CREATE);
        //动画
        panAnim = AnimationUtils.loadAnimation(MainActivity.this,R.anim.role);

        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        textView2 = (TextView)findViewById(R.id.textView2);
        textView = (TextView)findViewById(R.id.textView);
        play = (Button)findViewById(R.id.play);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        localmusic = (Button)findViewById(R.id.localmusic);
        playlist = (Button) findViewById(R.id.playlist);
        playnext = (Button) findViewById(R.id.playnext);
        playlast = (Button) findViewById(R.id.playlast);
        suiji = (Button)findViewById(R.id.suiji);
        pan = (ImageView) findViewById(R.id.pan);
        playlast.setOnClickListener(this);
        playnext.setOnClickListener(this);
        play.setOnClickListener(this);
        localmusic.setOnClickListener(this);
        playlist.setOnClickListener(this);
        suiji.setOnClickListener(this);
//        pan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.role);
//                pan.startAnimation(animation);
//
//            }
//        });



        dbHelper = new TabledatabaseHelper(this,"login.db",null,1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            //在改变进度条位置时，改变进度
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent();
                    intent.setAction("changed");
                    intent.putExtra("seekbarprogress", progress);
                    Intent newIntent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent));
                    bindService(newIntent,conn, Service.BIND_AUTO_CREATE);
                    startService(newIntent);
                }
            }
        });




    }
    //设置动画start stop
    public void startrotate(){
        pan.startAnimation(panAnim);
    }
    public void stoprotate(){
        pan.clearAnimation();
    }



    @Override
    public  void onClick(View v){
            switch (v.getId()) {
                case R.id.play:
                    musicService.start();
                    startrotate();
                    break;
                case R.id.localmusic:
                    Intent intent = new Intent(MainActivity.this,LocalMusicActivity.class);
                    startActivity(intent);
                    break;
                case R.id.playlist:
                    Intent intent3 = new Intent(MainActivity.this,playlist.class);
                    startActivity(intent3);
                    break;
                case R.id.playnext:
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.query("login",null,null,null,null,null,null);
                    cursor.moveToFirst();
                        do{
                        if(CurrentTitle.equals(cursor.getString(cursor.getColumnIndex("title")))) {

                            cursor.moveToNext();
                            if(cursor.isAfterLast()) {

                                cursor.moveToFirst();
                                String url = cursor.getString(cursor.getColumnIndex("url"));
                                String title = cursor.getString(cursor.getColumnIndex("title"));
                                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                                Intent intent2 = new Intent("startnew");
                                intent2.putExtra("url",url);
                                intent2.putExtra("title",title);
                                intent2.putExtra("artist",artist);
                                final Intent eiiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent2));
                                bindService(eiiintent,conn, Service.BIND_AUTO_CREATE);
                                startService(eiiintent);
                                break;
                        }   else{

                                String url = cursor.getString(cursor.getColumnIndex("url"));
                                String title = cursor.getString(cursor.getColumnIndex("title"));
                                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                                cursor.moveToNext();
                                Intent intent2 = new Intent("startnew");
                                intent2.putExtra("url",url);
                                intent2.putExtra("title",title);
                                intent2.putExtra("artist",artist);
                                final Intent eiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent2));
                                bindService(eiintent,conn, Service.BIND_AUTO_CREATE);
                                startService(eiintent);
                                break;
                            }
                        }
                    }while(cursor.moveToNext());
                    cursor.close();
                    break;
                case R.id.playlast:
                    SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                    Cursor cursorr = db1.query("login",null,null,null,null,null,null);
                    cursorr.moveToFirst();
                    do{
                        if(CurrentTitle.equals(cursorr.getString(cursorr.getColumnIndex("title")))) {

                                cursorr.moveToPrevious();
                                if(cursorr.isBeforeFirst()){
                                    cursorr.moveToLast();
                                    String url = cursorr.getString(cursorr.getColumnIndex("url"));
                                    String title = cursorr.getString(cursorr.getColumnIndex("title"));
                                    String artist = cursorr.getString(cursorr.getColumnIndex("artist"));
                                    Intent intent8 = new Intent("startnew");
                                    intent8.putExtra("url",url);
                                    intent8.putExtra("title",title);
                                    intent8.putExtra("artist",artist);
                                    final Intent eeiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent8));
                                    bindService(eeiintent,conn, Service.BIND_AUTO_CREATE);
                                    startService(eeiintent);
                                    break;
                        }
                        else{
                                    String url = cursorr.getString(cursorr.getColumnIndex("url"));
                                    String title = cursorr.getString(cursorr.getColumnIndex("title"));
                                    String artist = cursorr.getString(cursorr.getColumnIndex("artist"));
                                    cursorr.moveToNext();
                                    Intent intent8 = new Intent("startnew");
                                    intent8.putExtra("url",url);
                                    intent8.putExtra("title",title);
                                    intent8.putExtra("artist",artist);
                                    final Intent eeiintent;
                                    eeiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent8));
                                    bindService(eeiintent,conn, Service.BIND_AUTO_CREATE);
                                    startService(eeiintent);
                                    break;
                                }

                        }
                    }while(cursorr.moveToNext());
                    cursorr.close();
                    break;
                case R.id.suiji:

                    SQLiteDatabase db2 = dbHelper.getWritableDatabase();
                    Cursor cursor2 = db2.query("login",null,null,null,null,null,null);
                    int x = (int)(Math.random()*cursor2.getCount());
                    for(int i=0;i<cursor2.getCount()-x;i++){
                        cursor2.moveToNext();
                    }
                    String url = cursor2.getString(cursor2.getColumnIndex("url"));
                    String title = cursor2.getString(cursor2.getColumnIndex("title"));
                    String artist = cursor2.getString(cursor2.getColumnIndex("artist"));
                    Intent intent2 = new Intent("startnew");
                    intent2.putExtra("url",url);
                    intent2.putExtra("title",title);
                    intent2.putExtra("artist",artist);
                    final Intent eeiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent2));
                    bindService(eeiintent,conn, Service.BIND_AUTO_CREATE);
                    startService(eeiintent);

                    startrotate();
                    break;
                default:
                    break;
            }}
        private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
           @Override
           public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals("seekbarmaxprogress")) {
                      seekBar.setMax(intent
                        .getIntExtra("seekbarmaxprogress", 100));
               }
                 else if (intent.getAction().equals("seekbarprogress")) {
                      seekBar.setProgress(intent
                        .getIntExtra("seekbarprogress", 0));
               }
                 else if (intent.getAction().equals("pauseimage")) {
                     play.setBackgroundResource(R.drawable.pause);
                     stoprotate();
                 }
                 else if (intent.getAction().equals("playimage")) {
                     play.setBackgroundResource(R.drawable.play);
                 }
                 else if (intent.getAction().equals("gettitle")) {
                       CurrentTitle = intent.getStringExtra("title");
                      textView2.setText(intent.getStringExtra("artist"));
                      textView.setText(intent.getStringExtra("title"));}
                 else if (intent.getAction().equals("nextsong")) {

                     SQLiteDatabase dbb = dbHelper.getWritableDatabase();
                     Cursor cursorr = dbb.query("login",null,null,null,null,null,null);
                     cursorr.moveToFirst();
                     do{

                         if(CurrentTitle.equals(cursorr.getString(cursorr.getColumnIndex("title")))) {

                             cursorr.moveToNext();
                             if(cursorr.isAfterLast()) {

                                 cursorr.moveToFirst();
                                 String url = cursorr.getString(cursorr.getColumnIndex("url"));
                                 String title = cursorr.getString(cursorr.getColumnIndex("title"));
                                 String artist = cursorr.getString(cursorr.getColumnIndex("artist"));
                                 Intent intent2 = new Intent("startnew");
                                 intent2.putExtra("url",url);
                                 intent2.putExtra("title",title);
                                 intent2.putExtra("artist",artist);
                                 final Intent eiiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this,intent2));
                                 bindService(eiiintent,conn, Service.BIND_AUTO_CREATE);
                                 startService(eiiintent);
                                 break;
                             }   else {

                                 String url = cursorr.getString(cursorr.getColumnIndex("url"));
                                 String title = cursorr.getString(cursorr.getColumnIndex("title"));
                                 String artist = cursorr.getString(cursorr.getColumnIndex("artist"));
                                 cursorr.moveToLast();
                                 Intent intent2 = new Intent("startnew");
                                 intent2.putExtra("url", url);
                                 intent2.putExtra("title", title);
                                 intent2.putExtra("artist", artist);
                                 final Intent eiintent = new Intent(createExplicitFromImplicitIntent(MainActivity.this, intent2));
                                 bindService(eiintent, conn, Service.BIND_AUTO_CREATE);
                                 startService(eiintent);
                                 break;
                             }
                         }
                     }while(cursorr.moveToNext());
                     cursorr.close();
                 }
           }
        };
           @Override
           protected void onDestroy() {
               super.onDestroy();
               unbindService(conn);
               unregisterReceiver(broadcastReceiver);
           }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    }

