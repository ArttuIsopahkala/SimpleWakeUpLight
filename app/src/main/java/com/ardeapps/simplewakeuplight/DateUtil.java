package com.ardeapps.simplewakeuplight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MANUAL_MILLIS;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_WEEKDAYS;

/**
 * Created by Arttu on 6.5.2017.
 */
public class DateUtil {

    public static boolean isDateTomorrow(long milliseconds) {
        Calendar cal = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date start = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date end = cal.getTime();
        Date reference = new Date(milliseconds);

        return reference.after(start) && reference.before(end);
    }

    public static long getNextManualWakeup() {
        ArrayList<Integer> selectedDays = new ArrayList<>();
        for(String weekday : PrefRes.getStringSet(SELECTED_WEEKDAYS)) {
            selectedDays.add(Integer.parseInt(weekday));
        }
        Calendar nextAlarm = Calendar.getInstance();
        nextAlarm.set(Calendar.HOUR_OF_DAY, 0);
        nextAlarm.set(Calendar.MINUTE, 0);
        nextAlarm.set(Calendar.SECOND, 0);
        nextAlarm.set(Calendar.MILLISECOND, 0);
        // Alarm once or repeat
        if(!selectedDays.isEmpty()) {
            // Convert day of week to be correct for this app
            int dayOfWeek = nextAlarm.get(Calendar.DAY_OF_WEEK); // sunday = 1

            // Find next selected day
            while (!selectedDays.contains(dayOfWeek)) {
                if(dayOfWeek == 7) {
                    dayOfWeek = 1;
                } else  {
                    dayOfWeek++;
                }
                nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        long millis = PrefRes.getLong(SELECTED_MANUAL_MILLIS);
        nextAlarm.add(Calendar.MILLISECOND, (int) millis);

        // Set for tomorrow if time with delay is in the past
        long delay = TimeUnit.MINUTES.toMillis(PrefRes.getInt(PrefRes.SELECTED_MINUTES));
        if((nextAlarm.getTimeInMillis() - delay) < System.currentTimeMillis()) {
            nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
        }

        return nextAlarm.getTimeInMillis();
    }

    public static long getStartOfWeekInMillis() {
        Calendar cal = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static long getEndOfWeekInMillis() {
        Calendar cal = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTimeInMillis();
    }
}
