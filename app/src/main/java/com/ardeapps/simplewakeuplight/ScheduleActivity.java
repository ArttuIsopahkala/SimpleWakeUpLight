package com.ardeapps.simplewakeuplight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.ardeapps.simplewakeuplight.PrefRes.ALARM_SAVED;
import static com.ardeapps.simplewakeuplight.PrefRes.IS_APP_VISIBLE;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_MANUAL_MILLIS;
import static com.ardeapps.simplewakeuplight.PrefRes.SELECTED_WEEKDAYS;

public class ScheduleActivity extends Activity {

    Button saveButton;
    Button cancelButton;
    Button mondayButton;
    Button tuesdayButton;
    Button wednesdayButton;
    Button thursdayButton;
    Button fridayButton;
    Button saturdayButton;
    Button sundayButton;
    TimePicker timePicker;

    private ArrayList<String> selectedDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mondayButton = findViewById(R.id.mondayButton);
        tuesdayButton = findViewById(R.id.tuesdayButton);
        wednesdayButton = findViewById(R.id.wednesdayButton);
        thursdayButton = findViewById(R.id.thursdayButton);
        fridayButton = findViewById(R.id.fridayButton);
        saturdayButton = findViewById(R.id.saturdayButton);
        sundayButton = findViewById(R.id.sundayButton);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);
        timePicker = findViewById(R.id.timePicker);

        // Initialize selected time
        long millis = PrefRes.getLong(SELECTED_MANUAL_MILLIS);
        timePicker.setTimeInMillis(millis);

        // Initialize selected days
        selectedDays.addAll(PrefRes.getStringSet(SELECTED_WEEKDAYS));

        setWeekdayButton(mondayButton, "2");
        setWeekdayButton(tuesdayButton, "3");
        setWeekdayButton(wednesdayButton, "4");
        setWeekdayButton(thursdayButton, "5");
        setWeekdayButton(fridayButton, "6");
        setWeekdayButton(saturdayButton, "7");
        setWeekdayButton(sundayButton, "1");

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> selectedDaysSet = new HashSet<>(selectedDays);
                PrefRes.putStringSet(SELECTED_WEEKDAYS, selectedDaysSet);
                long millis = timePicker.getTimeInMillis();
                PrefRes.putLong(SELECTED_MANUAL_MILLIS, millis);
                // Set alarm on
                PrefRes.putBoolean(ALARM_SAVED, true);

                // Alarm is set in MainActivity's onResume
                onBackPressed();
            }
        });
    }

    private void setWeekdayButton(final Button button, final String weekday) {
        setButtonSelected(button, selectedDays.contains(weekday));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = selectedDays.contains(weekday);
                if(isSelected) {
                    selectedDays.remove(weekday);
                } else {
                    selectedDays.add(weekday);
                }

                setButtonSelected(button, !isSelected);
            }
        });
    }

    private void setButtonSelected(Button button, boolean selected) {
        if(selected) {
            button.setTypeface(button.getTypeface(), Typeface.BOLD);
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_button));
            button.setTextColor(ContextCompat.getColor(this, R.color.color_active));
        } else {
            button.setTypeface(button.getTypeface(), Typeface.NORMAL);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(ContextCompat.getColor(this, R.color.color_text_light_secondary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefRes.putBoolean(IS_APP_VISIBLE, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefRes.putBoolean(IS_APP_VISIBLE, false);
    }
}