package com.ardeapps.simplewakeuplight;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_SAVED;
import static com.ardeapps.simplewakeuplight.PrefRes.AUTOMATIC_SCHEDULE;
import static com.ardeapps.simplewakeuplight.PrefRes.NEXT_ALARM_TIME;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MINUTES;


public class AlarmService {

    public static void setNextWakeUp() {
        Context context = AppRes.getContext();
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent wakeUpIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            long nextAlarm;
            if(PrefRes.getBoolean(AUTOMATIC_SCHEDULE)) {
                nextAlarm = am.getNextAlarmClock().getTriggerTime();
            } else {
                nextAlarm = DateUtil.getNextManualWakeup();
            }

            PrefRes.putLong(NEXT_ALARM_TIME, nextAlarm);
            long wakeUpTime = nextAlarm - TimeUnit.MINUTES.toMillis(PrefRes.getInt(SELECTED_MINUTES));
            // Set alarm only if light shouldn't be open already
            if(wakeUpTime > System.currentTimeMillis()) {
                am.set(AlarmManager.RTC_WAKEUP, wakeUpTime, wakeUpIntent);
            }
            AppRes.updateWidget();
        }
    }

    public static Long getNextWakeUp() {
        if(PrefRes.getBoolean(AUTOMATIC_SCHEDULE)) {
            Context context = AppRes.getContext();
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null && am.getNextAlarmClock() != null) {
                return am.getNextAlarmClock().getTriggerTime();
            }
            return null;
        } else {
            return DateUtil.getNextManualWakeup();
        }
    }

    public static void cancelNextWakeUp() {
        // Do not remove NEXT_ALARM_TIME here. Just keep it up to date.
        Context context = AppRes.getContext();
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent wakeUpIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(wakeUpIntent);
            AppRes.updateWidget();
        }
    }

    public static void changeLightFromWidget() {
        boolean alarmSaved = !PrefRes.getBoolean(ALARM_SAVED);
        PrefRes.putBoolean(ALARM_SAVED, alarmSaved);
        if(alarmSaved) {
            if(getNextWakeUp() != null) {
                setNextWakeUp();
                // Show toast when light is set on
                int selectedDelay = PrefRes.getInt(SELECTED_MINUTES);
                long wakeUpTime = getNextWakeUp() - TimeUnit.MINUTES.toMillis(selectedDelay);
                Logger.toast(StringUtils.getHourMinTextForWidget(wakeUpTime));
            } else {
                cancelNextWakeUp();
            }
        } else {
            cancelNextWakeUp();
        }
    }

    public static void disableManualOnceAlarm() {
        PrefRes.putBoolean(ALARM_SAVED, false);
        cancelNextWakeUp();
    }
}
