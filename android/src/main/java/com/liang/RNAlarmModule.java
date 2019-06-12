
package com.liang;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TimeUtils;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.liang.RNALarmCeiver;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.R.attr.track;
import static android.R.attr.type;

public class RNAlarmModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private SharedPreferences sharedPreferences;

  public RNAlarmModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.sharedPreferences = reactContext.getSharedPreferences(getName(),Context.MODE_PRIVATE);
  }

  @Override
  public String getName() {
    return "RNAlarm";
  }

  @ReactMethod
  public void setAlarm(String triggerTime, String title, @Nullable String musicUri, @Nullable Callback successCallback, @Nullable Callback errorCallback) {

    int REQUEST_CODE=101;
    long triggerTimeMillis = Long.parseLong(triggerTime);
    Calendar currentTimeCal = Calendar.getInstance();
    currentTimeCal.setTime(new Date());

    try {
      AlarmManager alarmManager = (AlarmManager) reactContext.getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(RNAlarmConstants.REACT_NATIVE_ALARM);
      intent.setClass(reactContext, RNALarmCeiver.class);
      intent.putExtra("ALARM_TITLE", title);
      intent.putExtra("MUSIC_URI", musicUri);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(reactContext, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      //compare alarm and currentTime
      if (triggerTimeMillis - currentTimeCal.getTimeInMillis() > 0) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
          alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
          triggerTimeMillis, pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= 19) {
          alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } else {
          alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        }

      successCallback.invoke();
      return;

      } else {
        if (errorCallback != null) {
          errorCallback.invoke("-1");
          return;
        }
      }

    } catch (IllegalViewOperationException e) {
        if(errorCallback == null ){
          System.out.print(e.toString());
        } else {
          errorCallback.invoke(e.getMessage());
        }
    } catch (NumberFormatException e) {
        if (errorCallback == null ) {
          System.out.print(e.toString());
        } else {
          errorCallback.invoke(e.getMessage());
        }
      }
  }

  @ReactMethod
  public void clearAlarm() {
    Intent intent = new Intent(RNAlarmConstants.REACT_NATIVE_ALARM);
    intent.setClass(reactContext, RNALarmCeiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(reactContext, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmManager = (AlarmManager) reactContext.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pendingIntent);
  }
}
