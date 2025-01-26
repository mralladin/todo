package org.dieschnittstelle.mobile.android.todo.widgets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dieschnittstelle.mobile.android.skeleton.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerActivity extends Activity {
    private static final String LOG_TAG = DatePickerActivity.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datepicker);

        // der DatePicker
        DatePicker picker = findViewById(R.id.datepicker);
        TimePicker timepicker = findViewById(R.id.timepicker);
        timepicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        Log.i(LOG_TAG, "calendar in date picker " + calendar.getTime());
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.add(Calendar.MINUTE, 4);
        int currentMinute = calendar.get(Calendar.MINUTE);
        Log.i(LOG_TAG, "hour" + currentHour);
        timepicker.setHour(currentHour);
        timepicker.setMinute(currentMinute);


        // das Textfeld zur textuellen Darstellung des Datums
        final TextView dateAsText = findViewById(R.id.dateAsText);
        FloatingActionButton saveDateButton = findViewById(R.id.saveDateButton);
        dateAsText.setText(new Date().toString());


        saveDateButton.setOnClickListener(v -> {
            // Datum und Uhrzeit aus den Pickern lesen
            int year = picker.getYear();
            int month = picker.getMonth(); // Monate starten bei 0
            int day = picker.getDayOfMonth();
            int hour = timepicker.getHour();
            int minute = timepicker.getMinute();

            calendar.set(year, month, day, hour, minute);

            // Formatieren des Datums
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());

            // Zurückgeben Datum
            Intent resultIntent = new Intent();
            resultIntent.putExtra("result_key", formattedDate);

            setResult(RESULT_OK, resultIntent);
            finish();

        });


    }
}