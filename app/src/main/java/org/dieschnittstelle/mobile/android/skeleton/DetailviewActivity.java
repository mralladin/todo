package org.dieschnittstelle.mobile.android.skeleton;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailviewActivity extends AppCompatActivity {

    protected static final String LOG_TAG = DetailviewActivity.class.getName();
    private TextInputEditText dateEditText;
    private String selectedDate;
    public DetailviewActivity(){
        Log.i(LOG_TAG,"contructor called");
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG,"oncreate called");
        setContentView(R.layout.activity_detailview);
        String item = getIntent().getStringExtra("item");
        EditText itemNameEditedText =  findViewById(R.id.itemName);
        itemNameEditedText.setText(item.toString());

        dateEditText = findViewById(R.id.dateEditText);

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Date");
        // Set initial selection if available
        if (selectedDate != null) {
            try {
                Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate);
                builder.setSelection(date.getTime());
            } catch (ParseException e) {
                // Handle parsing error
            }
        } else {
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        }
        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.show(getSupportFragmentManager(), "datePicker");

        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                Date selectedDateObj = new Date(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = dateFormat.format(selectedDateObj); // Store the selected date
                dateEditText.setText(selectedDate);
            }
        });
    }

}
