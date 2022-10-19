package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.icu.text.CaseMap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Services.OnClearFromRecentService;

public class PlayerActivity extends AppCompatActivity implements Playable{

    Button btnplay, btnnext,btnprev;
    TextView txtsname, txtsstart,txtsstop;
    SeekBar seekmusic;
    ImageView imageView;
    NotificationManager notificationManager;
    List<track> tracks;
    int positionn =0;
    boolean isPlaying=false;


    String sname;
    public static final  String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        btnprev=findViewById(R.id.btnprev);
        btnplay=findViewById(R.id.playbtn);
        btnnext=findViewById(R.id.btnnext);
        txtsname=findViewById(R.id.txtsn);
        txtsstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);
        seekmusic=findViewById(R.id.seekbar);
        imageView=findViewById(R.id.imageview);

        if(mediaPlayer!= null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }









        Intent i = getIntent();
        Bundle bundle =i.getExtras();

        mySongs=(ArrayList) bundle.getParcelableArrayList("songs");
        String songName =i.getStringExtra("songname");
        position =bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri =Uri.parse(mySongs.get(position).toString());
        sname =mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer =MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateseekbar=new Thread()
        {
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentposition=0;

                while(currentposition<totalDuration)
                {
                    try {
                        sleep(500);
                        currentposition=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException|IllegalStateException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.purple_200),PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }

        });



       btnplay.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(mediaPlayer.isPlaying())
               {
                   btnplay.setBackgroundResource(R.drawable.ic_play);
                   mediaPlayer.pause();


               }
               else
               {
                   btnplay.setBackgroundResource(R.drawable.ic_pause);
                   mediaPlayer.start();

                   
               }
           }
       });

       String endTime=createTime(mediaPlayer.getDuration());
       txtsstop.setText(endTime);

       final Handler handler = new Handler();
       final int delay = 1000;

       handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               String currentTime =createTime(mediaPlayer.getCurrentPosition());
               txtsstart.setText(currentTime);
               handler.postDelayed(this,delay);
           }
       },delay);

       mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           @Override
           public void onCompletion(MediaPlayer mediaPlayer) {
               btnnext.performClick();
           }
       });



      btnnext.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              mediaPlayer.stop();
              mediaPlayer.release();
              position=((position+1)%mySongs.size());
              Uri u=Uri.parse(mySongs.get(position).toString());
              mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
              sname=mySongs.get(position).getName();
              txtsname.setText(sname);
              mediaPlayer.start();
              btnplay.setBackgroundResource(R.drawable.ic_pause);
              startAnimation(imageView);
              CreateNotification.createNotification(PlayerActivity.this,tracks.get(1),R.drawable.ic_pause,
                      3,tracks.size()-1);
          }
      });
      btnprev.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              mediaPlayer.stop();
              mediaPlayer.release();
              position=((position-1)<0)?(mySongs.size()-1):(position-1);
              Uri u=Uri.parse(mySongs.get(position).toString());
              mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
              sname=mySongs.get(position).getName();
              txtsname.setText(sname);
              mediaPlayer.start();
              btnplay.setBackgroundResource(R.drawable.ic_pause);
              startAnimation(imageView);
              CreateNotification.createNotification(PlayerActivity.this,tracks.get(1),R.drawable.ic_pause,
                      2,tracks.size()-1);
          }
      });

      popluateTracks();
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
          createChannel();
          registerReceiver(broadcastReceiver, new IntentFilter("TRACK_TRACKS"));
          startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
      }


    }
    private void createChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel= new NotificationChannel(CreateNotification.CHANNEL_ID,
            "KOD DEV",notificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager !=null){
                notificationManager.createNotificationChannel(channel);

            }
        }
    }

    private  void popluateTracks(){
        tracks = new ArrayList<>();

        tracks.add(new track(sname, "",R.drawable.t1));
        tracks.add(new track(sname, "",R.drawable.t2));
        tracks.add(new track(sname, "",R.drawable.t4));
        tracks.add(new track(sname, "",R.drawable.t3));
        tracks.add(new track(sname, "",R.drawable.t4));
        tracks.add(new track(sname, "",R.drawable.t2));
        tracks.add(new track(sname, "",R.drawable.t1));
        tracks.add(new track(sname, "",R.drawable.t2));



    }
    public void startAnimation(View view)
    {
        ObjectAnimator animator= ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 50;
        time += min + ":";

        if (sec < 10) {
            time+="0";
        }
        time+=sec;
    return time;
    }

BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getExtras().getString("actionname");

        switch (action){
            case CreateNotification.ACTION_PREVIUOS:
                onTrackPrevious();
            case CreateNotification.ACTION_PLAY:
                onTrackPlay();
            if(isPlaying){
                onTrackPause();
            }
            else{
                onTrackPlay();
            }
            break;
            case CreateNotification.ACTION_NEXT:
                onTrackNext();
                break;
        }
    }
};

    @Override
    public void onTrackPrevious() {

        positionn--;
        CreateNotification.createNotification(PlayerActivity.this, tracks.get(positionn),
                R.drawable.ic_pause,positionn,tracks.size()-1);
        mediaPlayer.stop();
        mediaPlayer.release();
        position=((position-1)<0)?(mySongs.size()-1):(position-1);
        Uri u=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtsname.setText(sname);
        mediaPlayer.start();
        btnplay.setBackgroundResource(R.drawable.ic_pause);





    }

    @Override
    public void onTrackPlay() {


        mediaPlayer.pause();
        btnplay.setBackgroundResource(R.drawable.ic_play);
        CreateNotification.createNotification(PlayerActivity.this, tracks.get(positionn),
                R.drawable.ic_pause,positionn,tracks.size()-1);

        if(mediaPlayer.isPlaying())
        {


        }
        else
        {
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();


        }
        return;

    }

    @Override
    public void onTrackPause() {







    }

    @Override
    public void onTrackNext() {

        positionn++;
        CreateNotification.createNotification(PlayerActivity.this, tracks.get(positionn),
                R.drawable.ic_pause,positionn,tracks.size()-1);
        mediaPlayer.stop();
        mediaPlayer.release();
        position=((position+1)%mySongs.size());
        Uri u=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtsname.setText(sname);
        mediaPlayer.start();
        btnplay.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        unregisterReceiver(broadcastReceiver);
    }
}
