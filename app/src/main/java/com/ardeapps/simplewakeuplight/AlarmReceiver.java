package com.ardeapps.simplewakeuplight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.ardeapps.simplewakeuplight.AppWidget.CHANGE_LIGHT;
import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_SAVED;
import static com.ardeapps.simplewakeuplight.PrefRes.AUTOMATIC_SCHEDULE;
import static com.ardeapps.simplewakeuplight.PrefRes.IS_APP_VISIBLE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(intent.getAction())) {
            if(PrefRes.getBoolean(AUTOMATIC_SCHEDULE)) {
                if(AlarmService.getNextWakeUp() != null && PrefRes.getBoolean(ALARM_SAVED)) {
                    AlarmService.setNextWakeUp();
                } else {
                    AlarmService.cancelNextWakeUp();
                }
            }
        } else if (CHANGE_LIGHT.equals(intent.getAction())) {
            AlarmService.changeLightFromWidget();
        } else {
            // Open wake-up alarm
            if(!PrefRes.getBoolean(IS_APP_VISIBLE)) {
                Intent i = new Intent(context, AlarmActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}