package org.dieschnittstelle.mobile.android.todo.widgets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.todo.OverviewActivity;

import java.util.Date;

public class DatePickerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datepicker);

        // der DatePicker
        DatePicker picker = (DatePicker)findViewById(R.id.datepicker);

        // das Textfeld zur textuellen Darstellung des Datums
        final TextView dateAsText = (TextView)findViewById(R.id.dateAsText);
        FloatingActionButton saveDate = findViewById(R.id.saveDateButton);
        dateAsText.setText(new Date().toString());

        picker.init(picker.getYear(),picker.getMonth(),picker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                // initialisiere das Datum, beruecksichtige die Unterschiede bei den Konventionen fuer die Repraesentation von Jahr / Monat / Tag als Integer-Werte
                Date dt = new Date(year-1900,monthOfYear,dayOfMonth);
                // setze das Datum auf dem Textfeld
                dateAsText.setText(dt.toString());

                // ueberpruefe das Datum
                if (year < 2024) {
                    startActivity(new Intent(DatePickerActivity.this, OverviewActivity.class));
                }
                Log.i("CustomLog,",dateAsText.getText().toString());
                // Daten in einen Intent packen
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result_key", "Hier sind die Daten von der zweiten Aktivität");

                // Setze das Ergebnis und beende die Aktivität
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

    }
}