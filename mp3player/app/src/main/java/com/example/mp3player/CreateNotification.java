package com.example.mp3player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import Services.NotificationActionService;

public class CreateNotification {

    public  static final String CHANNEL_ID="channel11";
    public  static final String ACTION_PREVIUOS="actionpreviuos";
    public  static final String ACTION_PLAY="actionplay";
    public  static final String ACTION_NEXT="actionnext";
    public  static  Notification notification;


    public static void  createNotification(Context context,track track,int playbutton,int pos,int size){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),track.getImage());

            PendingIntent pendingIntentPrevios;
            int drw_previous;
            if(pos==0){
                pendingIntentPrevios=null;
                drw_previous=0;
            }
            else {
                Intent intentPrevious=new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIUOS);
                pendingIntentPrevios=PendingIntent.getBroadcast(context,0,
                        intentPrevious,PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous= R.drawable.ic_prev;
            }

            Intent intentPlay=new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
           PendingIntent pendingIntentPlay=PendingIntent.getBroadcast(context,0,
                    intentPlay,PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int drw_next;
            if(pos==size){
                pendingIntentNext=null;
                drw_next=0;
            }
            else {
                Intent intentPrevious=new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext=PendingIntent.getBroadcast(context,0,
                        intentPrevious,PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next=R.drawable.ic_next;
            }



            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(icon)
                    .addAction(drw_previous,"Previous",pendingIntentPrevios)
                    .addAction(playbutton,"Play",pendingIntentPlay)
                    .addAction(drw_next,"Next",pendingIntentNext)
                    .setOnlyAlertOnce(true)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.getSessionToken())
                    .setShowActionsInCompactView(0,1,2))
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
            notificationManagerCompat.notify(1,notification);

        }
    }
}
