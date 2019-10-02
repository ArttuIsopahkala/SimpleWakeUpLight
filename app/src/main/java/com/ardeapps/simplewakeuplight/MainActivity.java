package com.ardeapps.simplewakeuplight;

import android.app.Activity;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_COLOR_PROGRESS;
import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_SAVED;
import static com.ardeapps.simplewakeuplight.PrefRes.APP_STARTED_FIRST_TIME;
import static com.ardeapps.simplewakeuplight.PrefRes.AUTOMATIC_SCHEDULE;
import static com.ardeapps.simplewakeuplight.PrefRes.IS_APP_VISIBLE;
import static com.ardeapps.simplewakeuplight.PrefRes.NEXT_ALARM_TIME;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MANUAL_MILLIS;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MAX_LIGHT_PERCENT;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MINUTES;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_WEEKDAYS;

public class MainActivity extends Activity {

    TextView nextWakeUpText;
    TextView nextWakeUpValueText;
    TextView alarmText;
    SeekBar colorPicker;
    View colorPreview;
    Switch useAlarmSwitch;
    Spinner scheduleTypeSpinner;
    Button testAlarm;
    Button infoButton;
    ImageView alarmIcon;
    Button scheduleButton;
    SeekBar durationPicker;
    TextView durationText;
    SeekBar maxLightPicker;

    // This checks spinners not trigger in initial
    private boolean userIsInteracting = false;
    // This checks onResume is not called after onCreate
    private boolean isAppOpened = false;

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextWakeUpText = findViewById(R.id.nextWakeUpText);
        nextWakeUpValueText = findViewById(R.id.nextWakeUpValueText);
        useAlarmSwitch = findViewById(R.id.useAlarmSwitch);
        scheduleTypeSpinner = findViewById(R.id.scheduleTypeSpinner);
        colorPicker = findViewById(R.id.colorPicker);
        colorPreview = findViewById(R.id.colorPreview);
        testAlarm = findViewById(R.id.testAlarm);
        infoButton = findViewById(R.id.infoButton);
        alarmText = findViewById(R.id.alarmText);
        alarmIcon = findViewById(R.id.alarmIcon);
        scheduleButton = findViewById(R.id.scheduleButton);
        durationPicker = findViewById(R.id.durationPicker);
        durationText = findViewById(R.id.durationText);
        maxLightPicker = findViewById(R.id.maxLightPicker);

        ArrayList<String> options = new ArrayList<>(Arrays.asList(getString(R.string.main_alarm_automatic), getString(R.string.main_alarm_manual)));
        setSpinnerAdapter(scheduleTypeSpinner, options);

        boolean appStartedFirstTime = !PrefRes.containsKey(APP_STARTED_FIRST_TIME) || PrefRes.getBoolean(APP_STARTED_FIRST_TIME);

        if (appStartedFirstTime) {
            Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent intent = new Intent();

            // Create Implicit intent and assign Shortcut Application Name, Icon
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                            getApplicationContext(), R.mipmap.ic_launcher));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);

            PrefRes.putBoolean(APP_STARTED_FIRST_TIME, false);

            InfoDialog infoDialog = new InfoDialog();
            infoDialog.showDialog(MainActivity.this);
        }

        // Set default values
        if(!PrefRes.containsKey(ALARM_SAVED)) {
            PrefRes.putBoolean(ALARM_SAVED, true);
        }
        if(!PrefRes.containsKey(AUTOMATIC_SCHEDULE)) {
            PrefRes.putBoolean(AUTOMATIC_SCHEDULE, true);
        }
        if(!PrefRes.containsKey(SELECTED_MINUTES)) {
            PrefRes.putInt(SELECTED_MINUTES, 30); // 30min
        }
        if(!PrefRes.containsKey(SELECTED_MAX_LIGHT_PERCENT)) {
            PrefRes.putInt(SELECTED_MAX_LIGHT_PERCENT, 100); // 0min, not delay
        }
        if(!PrefRes.containsKey(ALARM_COLOR_PROGRESS)) {
            PrefRes.putInt(ALARM_COLOR_PROGRESS, 1650); // sun yellow
        }
        if (!PrefRes.containsKey(SELECTED_MANUAL_MILLIS)) {
            PrefRes.putLong(SELECTED_MANUAL_MILLIS, TimeUnit.HOURS.toMillis(6));
        }
        if (!PrefRes.containsKey(SELECTED_WEEKDAYS)) {
            PrefRes.putStringSet(SELECTED_WEEKDAYS, new HashSet<String>());
        }

        openMainApp();
    }

    private void setSpinnerAdapter(Spinner spinner, ArrayList<String> items) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter.notifyDataSetChanged();
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private void openMainApp() {
        setAlarm();

        // MAIN SWITCH
        useAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                if (isChecked) {
                    PrefRes.putBoolean(ALARM_SAVED, true);
                    final Long nextAlarm = AlarmService.getNextWakeUp();
                    if (nextAlarm != null) {
                        PrefRes.putLong(NEXT_ALARM_TIME, nextAlarm);
                    }
                } else {
                    PrefRes.putBoolean(ALARM_SAVED, false);
                }
                setAlarm();
                AppRes.updateWidget();
            }
        });

        // SCHEDULE TYPE SPINNER
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                startActivity(intent);
            }
        });

        boolean isAutomaticSchedule = PrefRes.getBoolean(AUTOMATIC_SCHEDULE);
        scheduleTypeSpinner.setSelection(isAutomaticSchedule ? 0 : 1);
        scheduleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(userIsInteracting) {
                    boolean isAutomatic = position == 0;
                    PrefRes.putBoolean(AUTOMATIC_SCHEDULE, isAutomatic);
                    // Päivitetään alarm
                    setAlarm();
                    AppRes.updateWidget();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // MAX LIGHT SPEED
        maxLightPicker.post(new Runnable() {
            @Override
            public void run() {
                maxLightPicker.setMax(100);
                float progress = PrefRes.getInt(SELECTED_MAX_LIGHT_PERCENT);
                maxLightPicker.setProgress((int)progress);
                setMaxLightPickerColor(progress);
            }
        });
        maxLightPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PrefRes.putInt(SELECTED_MAX_LIGHT_PERCENT, progress);
                }
                setMaxLightPickerColor(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // DURATION
        durationPicker.post(new Runnable() {
            @Override
            public void run() {
                durationPicker.setMax(59);
                int progress = PrefRes.getInt(SELECTED_MINUTES) - 1;
                durationPicker.setProgress(progress);
            }
        });
        durationPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int minutes, boolean fromUser) {
                minutes++; // From 1 to 60
                if (fromUser) {
                    PrefRes.putInt(SELECTED_MINUTES, minutes);
                }
                durationText.setText(minutes + "min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Päivitetään alarm
                setAlarm();
                AppRes.updateWidget();
            }
        });


        // COLOR OF LIGHT
        colorPicker.post(new Runnable() {
            @Override
            public void run() {
                LinearGradient colorGradient = new LinearGradient(0.f, 0.f, colorPicker.getMeasuredWidth() - colorPicker.getThumb().getIntrinsicWidth(), 0.f,
                        new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
                                0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
                        null, Shader.TileMode.CLAMP
                );
                ShapeDrawable shape = new ShapeDrawable(new RectShape());
                shape.getPaint().setShader(colorGradient);
                colorPicker.setProgressDrawable(shape);
                colorPicker.setMax(256 * 7 - 1);
                int progress = PrefRes.getInt(ALARM_COLOR_PROGRESS);
                colorPicker.setProgress(progress);
            }
        });

        colorPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PrefRes.putInt(ALARM_COLOR_PROGRESS, progress);
                }
                int selectedColor = AppRes.getColorFromProgress(progress);

                GradientDrawable gd = new GradientDrawable();

                // Set the color array to draw gradient
                gd.setColors(new int[]{
                        selectedColor,
                        selectedColor,
                        getColor(R.color.color_main)
                });

                // Set the GradientDrawable gradient type linear gradient
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);

                // Set GradientDrawable shape is a rectangle
                gd.setShape(GradientDrawable.RECTANGLE);
                gd.setGradientRadius(400);
                gd.setGradientCenter(0.5f, 1);
                gd.setUseLevel(true);
                gd.setLevel(10000);

                colorPreview.setBackground(gd);

                setMaxLightPickerColor(PrefRes.getInt(SELECTED_MAX_LIGHT_PERCENT));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // FOOTER BUTTONS
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialog infoDialog = new InfoDialog();
                infoDialog.showDialog(MainActivity.this);
            }
        });

        testAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("openedFromUser", true);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

    }

    private void setMaxLightPickerColor(float percent) {
        //float percent = PrefRes.getInt(SELECTED_MAX_LIGHT_PERCENT) / 100;
        if(percent == 0) {
            percent = 1;
        }
        int color = AppRes.getColorFromProgress(PrefRes.getInt(ALARM_COLOR_PROGRESS));
        float width = (colorPicker.getMeasuredWidth() - colorPicker.getThumb().getIntrinsicWidth()) * (percent / 100);
        LinearGradient colorGradient = new LinearGradient(0.f, 0.f, width, 0.f,
                AppRes.manipulateColor(color, 0.2f), color, Shader.TileMode.CLAMP
        );
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(colorGradient);
        maxLightPicker.setProgressDrawable(shape);
    }

    private void setAlarm() {
        // Main switch is here because it must be update onResume and from widget
        useAlarmSwitch.post(new Runnable() {
            @Override
            public void run() {
                useAlarmSwitch.setChecked(PrefRes.getBoolean(ALARM_SAVED));
            }
        });

        boolean isAutomatic = PrefRes.getBoolean(AUTOMATIC_SCHEDULE);
        final Long nextAlarm = AlarmService.getNextWakeUp();
        if (isAutomatic) {
            scheduleButton.setVisibility(View.GONE);

            // AUTOMATIC SCHEDULE
            if (nextAlarm != null) {
                alarmIcon.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_alarm_on));
                alarmText.setText(getString(R.string.main_next_alarm, StringUtils.getDateTimeText(nextAlarm)));
            } else {
                alarmIcon.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_alarm_off));
                alarmText.setText(R.string.main_no_alarm_detected);
            }

            int selectedDelay = PrefRes.getInt(SELECTED_MINUTES);
            nextWakeUpValueText.setVisibility(View.GONE);
            if (PrefRes.getBoolean(ALARM_SAVED)) {
                if (nextAlarm != null) {
                    nextWakeUpText.setText(R.string.main_next_wakeup_light);
                    long wakeUpTime = nextAlarm - TimeUnit.MINUTES.toMillis(selectedDelay);
                    nextWakeUpValueText.setText(StringUtils.getDateTimeText(wakeUpTime));
                    nextWakeUpValueText.setVisibility(View.VISIBLE);
                    AlarmService.setNextWakeUp();
                } else {
                    nextWakeUpText.setText(R.string.main_alarm_saved_later);
                    AlarmService.cancelNextWakeUp();
                }
            } else {
                nextWakeUpText.setText(R.string.main_wakeup_light_not_set);
                AlarmService.cancelNextWakeUp();
            }
        } else {
            // MANUAL SCHEDULE
            scheduleButton.setVisibility(View.VISIBLE);

            if (nextAlarm != null) {
                alarmText.setText(getString(R.string.main_next_alarm_manual, StringUtils.getDateTimeText(nextAlarm)));
            }

            int selectedDelay = PrefRes.getInt(SELECTED_MINUTES);
            nextWakeUpValueText.setVisibility(View.GONE);
            if (PrefRes.getBoolean(ALARM_SAVED)) {
                if (nextAlarm != null) {
                    nextWakeUpText.setText(R.string.main_next_wakeup_light);
                    long wakeUpTime = nextAlarm - TimeUnit.MINUTES.toMillis(selectedDelay);
                    nextWakeUpValueText.setText(StringUtils.getDateTimeText(wakeUpTime));
                    nextWakeUpValueText.setVisibility(View.VISIBLE);
                    AlarmService.setNextWakeUp();
                }
            } else {
                nextWakeUpText.setText(R.string.main_wakeup_light_not_set);
                AlarmService.cancelNextWakeUp();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefRes.putBoolean(IS_APP_VISIBLE, true);
        if(isAppOpened) {
            setAlarm();
        }
        isAppOpened = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefRes.putBoolean(IS_APP_VISIBLE, false);
    }
}