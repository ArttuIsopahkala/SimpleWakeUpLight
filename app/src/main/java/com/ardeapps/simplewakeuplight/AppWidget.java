package com.ardeapps.simplewakeuplight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_SAVED;
import static com.ardeapps.simplewakeuplight.PrefRes.AUTOMATIC_SCHEDULE;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MINUTES;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_WEEKDAYS;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    public static final String CHANGE_LIGHT = "com.ardeapps.simplewakeuplight.CHANGE_LIGHT";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        // Open app
        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent pOpenApp = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetContainer, pOpenApp);

        if(PrefRes.getBoolean(ALARM_SAVED)) {
            views.setImageViewResource(R.id.lightIcon, R.drawable.icon_sun_on);
            views.setTextColor(R.id.appNameText, context.getColor(R.color.color_text_light));
            views.setTextColor(R.id.alarmText, context.getColor(R.color.color_text_light));
            views.setTextColor(R.id.delayText, context.getColor(R.color.color_text_light));

            views.setTextColor(R.id.mondayText, getWeekdayTextColor(context, "2"));
            views.setTextColor(R.id.tuesdayText, getWeekdayTextColor(context, "3"));
            views.setTextColor(R.id.wednesdayText, getWeekdayTextColor(context, "4"));
            views.setTextColor(R.id.thursdayText, getWeekdayTextColor(context, "5"));
            views.setTextColor(R.id.fridayText, getWeekdayTextColor(context, "6"));
            views.setTextColor(R.id.saturdayText, getWeekdayTextColor(context, "7"));
            views.setTextColor(R.id.sundayText, getWeekdayTextColor(context, "1"));
        } else {
            views.setImageViewResource(R.id.lightIcon, R.drawable.icon_sun_off);
            views.setTextColor(R.id.appNameText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.alarmText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.delayText, context.getColor(R.color.color_text_light_third));

            views.setTextColor(R.id.mondayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.tuesdayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.wednesdayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.thursdayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.fridayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.saturdayText, context.getColor(R.color.color_text_light_third));
            views.setTextColor(R.id.sundayText, context.getColor(R.color.color_text_light_third));
        }

        int selectedDelay = PrefRes.getInt(SELECTED_MINUTES);
        String alarmText = "";
        String delayText = selectedDelay + "min";
        final Long nextAlarm = AlarmService.getNextWakeUp();
        if(nextAlarm != null) {
            long wakeUpTime = nextAlarm - TimeUnit.MINUTES.toMillis(selectedDelay);
            alarmText = StringUtils.getHourMinTimeText(wakeUpTime);
        }
        views.setTextViewText(R.id.alarmText, alarmText);

        if(PrefRes.getBoolean(AUTOMATIC_SCHEDULE)) {
            views.setViewVisibility(R.id.delayText, View.VISIBLE);
            views.setViewVisibility(R.id.weekdaysContainer, View.INVISIBLE);
            views.setTextViewText(R.id.delayText, delayText);
        } else {
            // Alarm once or repeat
            if(PrefRes.getStringSet(SELECTED_WEEKDAYS).isEmpty()) {
                views.setViewVisibility(R.id.delayText, View.VISIBLE);
                views.setViewVisibility(R.id.weekdaysContainer, View.INVISIBLE);
                if(nextAlarm != null && DateUtil.isDateTomorrow(nextAlarm)) {
                    views.setTextViewText(R.id.delayText, context.getText(R.string.tomorrow));
                } else {
                    views.setTextViewText(R.id.delayText, context.getText(R.string.today));
                }
            } else {
                views.setViewVisibility(R.id.delayText, View.INVISIBLE);
                views.setViewVisibility(R.id.weekdaysContainer, View.VISIBLE);
            }
        }

        Intent changeLight = new Intent(context, AlarmReceiver.class);
        changeLight.setAction(CHANGE_LIGHT);
        PendingIntent pChangeLight = PendingIntent.getBroadcast(context.getApplicationContext(), 0, changeLight, 0);
        views.setOnClickPendingIntent(R.id.lightIconContent, pChangeLight);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getWeekdayTextColor(Context ctx, String weekday) {
        ArrayList<String> selectedDays = new ArrayList<>(PrefRes.getStringSet(SELECTED_WEEKDAYS));
        return ctx.getColor(selectedDays.contains(weekday) ? R.color.color_active : R.color.color_text_light_secondary);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

