package com.example.shiyan41;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.musicplayer50.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class LocalMusicActivity extends AppCompatActivity {

    private TabledatabaseHelper dbHelper;
    private MusicService musicService;
    private MusicAdapter adapter;
    private Boolean Exist = false;
    ListView listView;


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };

    private List<Music> Lmusic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localmusic);

        Intent intent = new Intent(this, MusicService.class);
        //调用bindService()，传递ServiceConnection
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        //自检SD卡是否获得权限
        if (ContextCompat.checkSelfPermission(LocalMusicActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        //隐藏顶部项目名称
       getSupportActionBar().hide();
        //设置窗体全屏
       this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        listView = (ListView) findViewById(R.id.listView);





        dbHelper = new TabledatabaseHelper(this,"login.db",null,1);


        Findmusic findmusic = new Findmusic();
        Lmusic = findmusic.getmusics(this.getContentResolver());   //找到资源，music型组
        adapter = new MusicAdapter(LocalMusicActivity.this,R.layout.musicitem,Lmusic); //新建想对应的适配器
        listView.setAdapter(adapter);
        //将music写进db
        listView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Music music = Lmusic.get(position);
                String url = music.getUrl();
                String title = music.getTitle();
                String artist = music.getArtist();

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                Cursor cursor = db.query("login",null,null,null,null,null,null);

                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToNext();

                    if(title.equals(cursor.getString(cursor.getColumnIndex("title")))) {
                        //已经存在
                        Exist = true;
                        break;
                    }
                }
                if(Exist==false) {
                    values.put("title", title);
                    values.put("artist", artist);
                    values.put("url", url);
                    db.insert("login", null, values);
                    values.clear();

                    Exist = false;
                }
                cursor.close();

                Intent intent = new Intent("startnew");
                intent.putExtra("url",url);
                intent.putExtra("title",title);
                intent.putExtra("artist",artist);

                final Intent eintent = new Intent(createExplicitFromImplicitIntent(LocalMusicActivity.this,intent));
                bindService(eintent,conn, Service.BIND_AUTO_CREATE);
                startService(eintent);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
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
