package com.ardeapps.simplewakeuplight;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class TimePicker extends LinearLayout {
    TimeEditText hourText;
    TimeEditText minuteText;
    int hours;
    int minutes;

    public TimePicker(Context context) {
        super(context);
        createView(context);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView(context);
    }

    private void createView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.time_picker, this);
        hourText = findViewById(R.id.hourText);
        minuteText = findViewById(R.id.minuteText);
        clearFocus();
    }

    private void setTextListeners() {
        // ON FOCUS CHANGE
        hourText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    formatHourText();
                }
            }
        });
        minuteText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    formatMinuteText();
                }
            }
        });
        // ON TEXT CHANGE
        hourText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String hours = s.toString();
                if (!StringUtils.isEmptyString(hours)) {
                    int value = Integer.parseInt(hours);
                    if (count == 2 || value > 2) {
                        minuteText.setFocusableInTouchMode(true);
                        minuteText.requestFocus();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        // IME
        hourText.setKeyImeChangeListener(new TimeEditText.KeyImeChange() {
            @Override
            public void onKeyIme(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    formatHourText();
                    hourText.clearFocus();
                    AppRes.hideKeyBoard(hourText);
                }
            }
        });
        minuteText.setKeyImeChangeListener(new TimeEditText.KeyImeChange() {
            @Override
            public void onKeyIme(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    formatMinuteText();
                    minuteText.clearFocus();
                    AppRes.hideKeyBoard(minuteText);
                }
            }
        });
        // EDITOR
        hourText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    formatHourText();
                }
                return false;
            }
        });
        minuteText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    formatMinuteText();
                    minuteText.clearFocus();
                    AppRes.hideKeyBoard(minuteText);
                }
                return false;
            }
        });

    }

    private void formatMinuteText() {
        int newMinutes = minutes;
        String minuteString = minuteText.getText().toString();
        if (!StringUtils.isEmptyString(minuteString)) {
            newMinutes = Integer.parseInt(minuteString);
            if (newMinutes > 59) {
                newMinutes = minutes;
            }
        }
        setMinuteText(newMinutes);
    }

    private void formatHourText() {
        int newHours = hours;
        String hourString = hourText.getText().toString();
        if (!StringUtils.isEmptyString(hourString)) {
            newHours = Integer.parseInt(hourString);
            if (newHours > 23) {
                newHours = hours;
            }
        }
        setHourText(newHours);
    }

    private void setMinuteText(int minutes) {
        String minuteString = (minutes < 10 ? "0" : "") + minutes;
        minuteText.setText(minuteString);
    }

    private void setHourText(int hours) {
        hourText.setText(String.valueOf(hours));
    }

    public long getTimeInMillis() {
        try {
            int hours = Integer.parseInt(hourText.getText().toString());
            int minutes = Integer.parseInt(minuteText.getText().toString());
            return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setTimeInMillis(long millis) {
        hours = (int)TimeUnit.MILLISECONDS.toHours(millis);
        minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
        setHourText(hours);
        setMinuteText(minutes);
        setTextListeners();
    }
}