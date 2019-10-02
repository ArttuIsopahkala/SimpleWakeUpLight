package com.ardeapps.simplewakeuplight;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.ardeapps.simplewakeuplight.PrefRes.AUTOMATIC_SCHEDULE;
import static com.ardeapps.simplewakeuplight.PrefRes.NEXT_ALARM_TIME;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MAX_LIGHT_PERCENT;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_WEEKDAYS;

public class AlarmActivity extends Activity {

    TextView stopAlarmText;
    TextView brightnessLevelText;
    TextView timeToAlarmText;
    RelativeLayout alarmContent;
    AdView adView;

    CountDownTimer brightnessTimer = null;
    ShutDownTask shutDownTask = new ShutDownTask();
    Timer shutDownTimer = new Timer();

    float brightness = 0;
    float brightnessToAdd = 0;

    final float maxBrightness = 255;
    final long period = 1000; // updates every second
    boolean openedFromUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Bundle b = getIntent().getExtras();
        openedFromUser = b != null && b.getBoolean("openedFromUser");

        userPowerManagerWakeup();
        useWindowFlags();
        useActivityScreenMethods();

        stopAlarmText = findViewById(R.id.stopAlarmText);
        brightnessLevelText = findViewById(R.id.brightnessLevelText);
        timeToAlarmText = findViewById(R.id.timeToAlarmText);
        alarmContent = findViewById(R.id.alarmContent);
        adView = findViewById(R.id.adView);

        MobileAds.initialize(this, getString(R.string.admob_app_id));

        int selectedColor = AppRes.getColorFromProgress(PrefRes.getInt(PrefRes.ALARM_COLOR_PROGRESS));
        alarmContent.setBackgroundColor(selectedColor);

        long delay;

        if(!openedFromUser) {
            // App could wake up later than planned because system queue other apps higher priority.
            // Calculate delay again.
            long nextAlarm = PrefRes.getLong(NEXT_ALARM_TIME);
            delay = nextAlarm - System.currentTimeMillis();

            adView.setVisibility(View.VISIBLE);
            AdRequest.Builder request  = new AdRequest.Builder();
            request.addTestDevice("BF9780C87D05D6C239527F8B026396CB");
            adView.loadAd(request.build());
        } else {
            delay = TimeUnit.MINUTES.toMillis(PrefRes.getInt(PrefRes.SELECTED_MINUTES));
            adView.setVisibility(View.GONE);
        }

        float maxLightPercent = PrefRes.getInt(SELECTED_MAX_LIGHT_PERCENT);
        float brightnessTime = maxLightPercent / 100 * delay;
        brightnessToAdd = (float) period / brightnessTime * maxBrightness;

        alarmContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(openedFromUser) {
                    onBackPressed();
                } else {
                    finishAffinity();
                }
            }
        });

        brightness = 0;

        brightnessTimer = new CountDownTimer(delay, period) {
            public void onTick(long millisUntilFinished) {
                if(brightness <= maxBrightness) {
                    changeScreenBrightness(brightness);
                    int percents = Math.round(brightness / maxBrightness * 100);
                    brightnessLevelText.setText(percents + "%");
                    brightness += brightnessToAdd;
                } else {
                    changeScreenBrightness(maxBrightness);
                    brightnessLevelText.setText("100%");
                }

                timeToAlarmText.setText(StringUtils.getMinSecTimeText(millisUntilFinished));
            }

            public void onFinish() {
                brightnessLevelText.setText("100%");
                timeToAlarmText.setText("00:00");
                shutDownTimer.schedule(shutDownTask, 1000 * 60 * 10); // shut down in 10 minutes
            }
        };
        brightnessTimer.start();
    }

    private void useActivityScreenMethods() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                setTurnScreenOn(true);
                setShowWhenLocked(true);
            } catch (NoSuchMethodError e) {
                Logger.log("Enable setTurnScreenOn and setShowWhenLocked is not present on device!");
            }
        }
    }

    private void useWindowFlags() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void userPowerManagerWakeup() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(pm != null) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "sunrisescreen:wakelock");
            wakeLock.acquire(TimeUnit.SECONDS.toMillis(5));
        }
    }

    //cancel timer
    void cancelTimer() {
        if(brightnessTimer != null)
            brightnessTimer.cancel();
    }

    private void changeScreenBrightness(final float screenBrightnessValue) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Window window = getWindow();
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.screenBrightness = screenBrightnessValue / maxBrightness;
                window.setAttributes(layoutParams);
            }
        });
    }

    private class ShutDownTask extends TimerTask {
        public void run() {
            finishAffinity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ALARM_SAVED is always true
        // Set next wake up if scheduling is manual
        if(!openedFromUser && !PrefRes.getBoolean(AUTOMATIC_SCHEDULE)) {
            if(PrefRes.getStringSet(SELECTED_WEEKDAYS).isEmpty()) {
                // Once repeat -> disable wake-up light
                AlarmService.disableManualOnceAlarm();
            } else {
                AlarmService.setNextWakeUp();
            }
        }
        cancelTimer();
    }
}
