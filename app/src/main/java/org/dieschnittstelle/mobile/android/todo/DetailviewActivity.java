package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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


    private static final int REQUEST_CONTACT_PERMISSIONS = 42;
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

        Toolbar toolbar = findViewById(R.id.toolbarDetailView);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detailview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(LOG_TAG,"item.getItemId() "+ item.getItemId());

        if(item.getItemId() == R.id.addContact){
            addContact();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }


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

        selectContactLauncher.launch(selectContactIntent);

    }

    private ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i(LOG_TAG,"got reuslt from selectre contact:"+result);
                if(result.getResultCode() == RESULT_OK){
                    readContactDetails(result.getData().getData());
                }
            }


    );

    public void readContactDetails(Uri contactUri){
        Log.i(LOG_TAG,"readContactDetails contact uri is: "+contactUri);

        Cursor cursor = getContentResolver().query(contactUri,null,null,null,null);
        if(cursor.moveToFirst()){
            int displayNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String displayName = cursor.getString(displayNameColumnIndex);
            Log.i(LOG_TAG,"readContactDetails displayName is: "+displayName);
            int internalContactIDColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String internalContactID = cursor.getString(internalContactIDColumnIndex);
            Log.i(LOG_TAG,"readContactDetails internalContactID is: "+internalContactID);
            readContactDetailsForInternalID(Long.parseLong(internalContactID));
            cursor.close();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CONTACT_PERMISSIONS){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i(LOG_TAG,"permission granted");
                readContactDetails(null);
            }else{
                Log.i(LOG_TAG,"permission denied");
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }

    public void readContactDetailsForInternalID(long contactId){
        Log.i(LOG_TAG,"readContactDetailsForInternalID internalContactID is: "+contactId);

        int hasReadContactPermission = checkSelfPermission(android.Manifest.permission.READ_CONTACTS);
        if(hasReadContactPermission != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},REQUEST_CONTACT_PERMISSIONS);
            return;
        }


        String querySelectionPattern = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null
        ,querySelectionPattern, new String[]{String.valueOf(contactId)},null
        );
        while (cursor.moveToNext()){
            int phoneNumColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int phoneNumTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            String phoneNum = cursor.getString(phoneNumColumnIndex);
            int phoneNumType = cursor.getInt(phoneNumTypeColumnIndex);
            Log.i(LOG_TAG,"readContactDetailsForInternalID phoneNum is: "+phoneNum);

            boolean isMobile = phoneNumType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            Log.i(LOG_TAG,"readContactDetailsForInternalID isMobile: "+isMobile);
        }
        cursor.close();
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
