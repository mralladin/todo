package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.widgets.DatePickerActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailviewActivity extends AppCompatActivity {


    private ActivityDetailviewBinding binding;

    protected static final String LOG_TAG = DetailviewActivity.class.getName();
    private TextInputEditText dateEditText;
    private String selectedDate;
    private Long tbdTimestamp;
    private int prioValue=0;
    private Date tbdDate;
    private Spinner prioritySpinner;
    public DetailviewActivity(){
        Log.i(LOG_TAG,"contructor called");
    }
    DataItem item;

    public DataItem getItem() {
        return item;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        item = (DataItem) getIntent().getSerializableExtra(OverviewActivity.ARG_ITEM);
        if (item==null){
            item=new DataItem();
            //Bei innitialier erstellung muss das Start Datum gesetzt werden
            item.setStartTime(new Date().getTime());
        }

        // Controller und Item mit Binding verknüpfen
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);
        binding.setController(this);


        Log.i("Debuger","item"+item);

        prioritySpinner = findViewById(R.id.prioritySpinner);
        prioritySpinner.setSelection(item.getPrio());

        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setText(getFormatesDateStringFromLong(item.getTbdDate()));

    }

    public void saveItem() {
        Intent returnIntent = new Intent();
        parseDateString(dateEditText.getText().toString());
        item.setTbdDate(tbdTimestamp);
        item.setPrio(prioritySpinner.getSelectedItemPosition());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            item.setUseridcreated(user.getUid());
        }

        returnIntent.putExtra(OverviewActivity.ARG_ITEM,item);
        Log.e("TestLog","Hello: "+R.id.dateAsText);
        this.setResult(DetailviewActivity.RESULT_OK,returnIntent);
        this.finish();
    }

    public void showDatePicker() {
        Log.i(LOG_TAG,"oncreate called");
        Intent intent = new Intent(DetailviewActivity.this,
                DatePickerActivity.class);
        startForResult.launch(intent);

    }

    private void parseDateString(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Log.e("datestrin in parse ",dateString);

        try {
            // String in Date konvertieren
            tbdDate = dateFormat.parse(dateString);
            Log.e("TestLog5","I reach this");

            // Date in Millisekunden umwandeln
            tbdTimestamp = tbdDate.getTime();

        } catch (ParseException e) {

            e.printStackTrace();
        }
    }

    private String getFormatesDateStringFromLong(Long timestamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if(timestamp==null){
            timestamp=new Date().getTime();
        }
        // Timestamp in Date umwandeln
        Date date = new Date(timestamp);

        // Date in das gewünschte String-Format umwandeln
        return dateFormat.format(date);
    }


    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedData = data.getStringExtra("result_key");
                        Log.e("TestLog",returnedData.toString());
                        dateEditText.setText(returnedData);
                        parseDateString(returnedData);
                    }
                }
            }
    );

}
