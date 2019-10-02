package com.ardeapps.simplewakeuplight;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Arttu on 4.5.2017.
 */
public class StringUtils {

    public static boolean isEmptyString(String text) {
        return text == null || text.trim().equals("");
    }

    public static String getMinutesText(long milliseconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public static String getMinSecTimeText(long milliseconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public static String getHourMinTextForWidget(long millis) {
        Context ctx = AppRes.getContext();
        long millisUntilWakeUp = millis - System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(millisUntilWakeUp);
        long hours = TimeUnit.MILLISECONDS.toHours(millisUntilWakeUp) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilWakeUp) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours) + 1;
        if(minutes == 60) {
            minutes = 0;
            hours++;
        }
        if(hours == 24) {
            hours = 0;
            days++;
        }
        String daysString = "";
        if(days > 0) {
            daysString = days + " " + ctx.getString(days == 1 ? R.string.day : R.string.days);
            daysString += (minutes > 0 || hours > 0) ? ", " : " ";
        }
        String hoursString = "";
        if(hours > 0) {
            hoursString = hours + " " + ctx.getString(hours == 1 ? R.string.hour : R.string.hours) + " ";
        }
        String and = "";
        if(minutes > 0 && hours > 0) {
            and = ctx.getString(R.string.and) + " ";
        }
        String minutesString = "";
        if(minutes > 0) {
            minutesString += minutes + " " + ctx.getString(minutes == 1 ? R.string.minute : R.string.minutes) + " ";
        }

        return daysString + hoursString + and + minutesString + ctx.getString(R.string.widget_on_message);
    }

    public static String getHourMinTimeText(long milliseconds) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(milliseconds);

        int minutes = c.get(Calendar.MINUTE);
        String minutesString = minutes < 10 ? "0" + minutes : minutes + "";

        int hours = c.get(Calendar.HOUR_OF_DAY);
        String hoursString = hours < 10 ? "0" + hours : hours + "";

        return hoursString + ":" + minutesString;
    }

    public static String getDateTimeText(long milliseconds) {
        Context context = AppRes.getContext();
        String dateString;
        if (DateUtils.isToday(milliseconds)) {
            dateString = getHourMinTimeText(milliseconds);
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(milliseconds);
            if (DateUtil.isDateTomorrow(milliseconds)) {
                dateString = context.getString(R.string.tomorrow);
            } else {
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                dateString = getWeekDay(dayOfWeek);
            }
            dateString += " " + context.getString(R.string.clock) + " " + getHourMinTimeText(milliseconds);
        }
        return dateString;
    }

    private static String getWeekDay(int dayOfWeek) {
        Context context = AppRes.getContext();
        if (dayOfWeek == Calendar.MONDAY) {
            return context.getString(R.string.monday);
        } else if (dayOfWeek == Calendar.TUESDAY) {
            return context.getString(R.string.tuesday);
        } else if (dayOfWeek == Calendar.WEDNESDAY) {
            return context.getString(R.string.wednesday);
        } else if (dayOfWeek == Calendar.THURSDAY) {
            return context.getString(R.string.thursday);
        } else if (dayOfWeek == Calendar.FRIDAY) {
            return context.getString(R.string.friday);
        } else if (dayOfWeek == Calendar.SATURDAY) {
            return context.getString(R.string.saturday);
        } else if (dayOfWeek == Calendar.SUNDAY) {
            return context.getString(R.string.sunday);
        } else {
            return "";
        }
    }

}
