package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.viewmodel.DetailviewViewModel;
import org.dieschnittstelle.mobile.android.todo.widgets.DatePickerActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DetailviewActivity extends AppCompatActivity {


    private ActivityDetailviewBinding binding;

    protected static final String LOG_TAG = DetailviewActivity.class.getName();
    protected static final String ARG_ITEM = "item";
    private TextInputEditText dateEditText;
    private String selectedDate;
    private Long tbdTimestamp;
    private int prioValue=0;
    private Date tbdDate;
    private Spinner prioritySpinner;
    private DetailviewViewModel viewModel;
    public DetailviewActivity(){
        Log.i(LOG_TAG,"contructor called");
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(DetailviewViewModel.class);

        if(viewModel.getItem() == null){
            DataItem item = (DataItem) getIntent().getSerializableExtra(ARG_ITEM);
            if (item==null){
                item=new DataItem();
                Calendar calendar = Calendar.getInstance();
                item.setStartTime(calendar.getTimeInMillis());
            }
            this.viewModel.setItem(item);

        }

        // Controller und Item mit Binding verknüpfen
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);
        binding.setViewmodel(this.viewModel);
        binding.setLifecycleOwner(this);

        //register acitivty as observer

        this.viewModel.getValidOnSave().observe(this,validOnSave->{
            if(validOnSave){
                saveItem();
                Log.i("save","save");

            }
        });

        prioritySpinner = findViewById(R.id.prioritySpinner);
        prioritySpinner.setSelection(viewModel.getItem().getPrio());

        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setText(getFormatesDateStringFromLong(viewModel.getItem().getTbdDate()));

        dateEditText.setOnClickListener(v -> {
            showDatePicker();
        });

    }

    public void saveItem() {
        Intent returnIntent = new Intent();
        parseDateString(dateEditText.getText().toString());
        viewModel.getItem().setTbdDate(tbdTimestamp);
        Log.e("TestLog","prioritySpinner: "+prioritySpinner.getSelectedItemPosition());

        viewModel.getItem().setPrio(prioritySpinner.getSelectedItemPosition());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            viewModel.getItem().setUseridcreated(user.getUid());
        }
        returnIntent.putExtra(OverviewActivity.ARG_ITEM,viewModel.getItem());
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
        Calendar calendar = Calendar.getInstance();
        if(timestamp==null){
            timestamp=calendar.getTime().getTime();
            Log.i("Debuger","calendar "+calendar.getTime());
        }
        // Timestamp in Date umwandeln
        Date date = new Date(timestamp);
        Log.i("Debuger","date "+date);

        // Date in das gewünschte String-Format umwandeln
        return dateFormat.format(date);
    }

    public void addContact(){
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);


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
