package com.liang;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.support.v4.app.NotificationCompat;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Toast;
import android.util.Log;

import com.facebook.common.util.UriUtil;

import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;
import android.content.res.Resources;

public class RNALarmCeiver extends BroadcastReceiver {
  static MediaPlayer player = new MediaPlayer();

  @Override
  public void onReceive(Context context, Intent intent) {
    String musicUri = intent.getStringExtra("MUSIC_URI");
    String title = intent.getStringExtra("ALARM_TITLE");

    int soundResId = context.getResources().getIdentifier(musicUri, "raw", context.getPackageName());
    Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + soundResId);

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String NOTIFICATION_CHANNEL_ID = "LOTUS_CHANNEL";

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Meditation Timer", NotificationManager.IMPORTANCE_MAX);

      // Configure the notification channel.
      notificationChannel.setDescription("Lotus notification timers");
      notificationChannel.enableLights(true);
      // notificationChannel.setLightColor(Color.RED);
      notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
      notificationChannel.enableVibration(true);
      notificationManager.createNotificationChannel(notificationChannel);
    }

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
    notificationBuilder.setAutoCancel(true)
      .setDefaults(Notification.DEFAULT_ALL)
      .setWhen(System.currentTimeMillis())
      .setSound(soundUri, AudioManager.STREAM_ALARM)
      .setSmallIcon(android.R.drawable.sym_def_app_icon)
      .setTicker("Hearty365")
      .setPriority(Notification.PRIORITY_MAX)
      .setContentTitle(title);
      // .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
      // .setContentInfo("Info")

    notificationManager.notify(/*notification id*/1, notificationBuilder.build());

    try {
      player.setDataSource(context, soundUri);
      player.setLooping(false);
      player.prepareAsync();
      player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          player.start();
          new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
              if (player.isPlaying()) {
                player.stop();
                player.reset();
              }
            }
          }.start();
        }
      });

    } catch (IOException e) {
        e.printStackTrace();
        Log.e("EXCEPTION","IO_EXCEPTION"+e);
    } catch (Exception e) {
        e.printStackTrace();
        Log.e("EXCEPTION","EXCEPTION:"+e);
    }
  }
}
