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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.util.ContactAdapter;
import org.dieschnittstelle.mobile.android.todo.viewmodel.DetailviewViewModel;
import org.dieschnittstelle.mobile.android.todo.widgets.DatePickerActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailviewActivity extends AppCompatActivity implements ContactAdapter.OnItemClickListener  {


    protected static final String LOG_TAG = DetailviewActivity.class.getName();
    protected static final String ARG_ITEM = "item";
    private static final int REQUEST_CONTACT_PERMISSIONS = 42;
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE = 2;
    private static final int RESULT_TO_DELETE = 9;
    private ActivityDetailviewBinding binding;
    private TextInputEditText dateEditText;
    private FloatingActionButton deleteButtonDetailView;

    private Long tbdTimestamp;
    private Date tbdDate;
    private AutoCompleteTextView priorityDropdown;
    private ContactAdapter adapter;
    private List<String> contactList;

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedData = data.getStringExtra("result_key");
                        Log.e(LOG_TAG, returnedData);
                        dateEditText.setText(returnedData);
                        parseDateString(returnedData);
                    }
                }
            }
    );
    private DetailviewViewModel viewModel;
    private final ActivityResultLauncher<Intent> selectContactLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i(LOG_TAG, "got reuslt from selectre contact:" + result);
                if (result.getResultCode() == RESULT_OK) {
                    readContactDetails(result.getData().getData());
                }
            }
    );



    public DetailviewActivity() {
        Log.i(LOG_TAG, "contructor called");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(DetailviewViewModel.class);

        int requestCode = getIntent().getIntExtra("REQUEST_CODE", -1);


        DataItem item = (DataItem) getIntent().getSerializableExtra(ARG_ITEM);
        if (viewModel.getItem() == null) {
            if (item == null) {
                item = new DataItem();
                Calendar calendar = Calendar.getInstance();
                //setPriorityDropdown initial to Niedrig
                item.setStartTime(calendar.getTimeInMillis());
            }else{
                Log.i(LOG_TAG, "got data item with contact ids: " + item.getContactIds().toString());
                item.getContactIds().forEach(contactIdAsString -> readContactDetailsForInternalID(Long.parseLong(contactIdAsString)));
            }
            this.viewModel.setItem(item);

        }

        // Controller und Item mit Binding verknüpfen
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);
        binding.setViewmodel(this.viewModel);
        binding.setLifecycleOwner(this);
        initializeDropDown(item);




        //register acitivty as observer
        this.viewModel.getValidOnSave().observe(this, validOnSave -> {
            if (validOnSave) {
                saveItem();
                Log.i(LOG_TAG, "save");
            }
        });

        setDateButtonAndDeleteButtonLogic();

        if (requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE) {
            // Handle the specific case
            deleteButtonDetailView.setVisibility(View.GONE);
            Log.i(LOG_TAG, "Started with REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE");
        }

        initRecyclerView();





        Toolbar toolbar = findViewById(R.id.toolbarDetailView);
        setSupportActionBar(toolbar);

    }

    private void initRecyclerView() {
        RecyclerView contactRecyclerView = findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int maxHeight = screenHeight *10/100;
        ViewGroup.LayoutParams layoutParams = contactRecyclerView.getLayoutParams();
        layoutParams.height = maxHeight;
        contactRecyclerView.setLayoutParams(layoutParams);
        contactList = new ArrayList<>();

        adapter = new ContactAdapter(contactList,this);


        contactRecyclerView.setAdapter(adapter);

        viewModel.getContactIds().observe(this, adapter::updateContacts);
    }

    private void setDateButtonAndDeleteButtonLogic() {
        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setText(getFormatesDateStringFromLong(viewModel.getItem().getTbdDate()));
        dateEditText.setOnClickListener(v -> {
            showDatePicker();
        });
        deleteButtonDetailView = findViewById(R.id.deleteButtonDetailView);
        deleteButtonDetailView.setOnClickListener(v -> {
            deleteItem();
        });
    }

    private void initializeDropDown(DataItem item) {
        priorityDropdown = findViewById(R.id.priorityDropdown);
        // Adapter für die Dropdown-Liste
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.priority_values,
                android.R.layout.simple_dropdown_item_1line
        );

        priorityDropdown.setAdapter(adapter);
        priorityDropdown.setOnClickListener(v -> priorityDropdown.showDropDown());
        //Immer Prio ändern bei Click
        priorityDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPriority = parent.getItemAtPosition(position).toString();
            viewModel.getItem().setPrio(position);
            Log.i(LOG_TAG, selectedPriority);
        });

        priorityDropdown.setText(item.getPrio() == 0 ? "Niedrig" : item.getPrio() == 1 ? "Mittel" : item.getPrio() == 2 ? "Hoch" : "Sehr hoch", false);

    }

    public void deleteItem(){
        new AlertDialog.Builder(this)
                .setTitle("Löschen bestätigen")
                .setMessage("Möchten Sie dieses Element wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(OverviewActivity.ARG_ITEM, viewModel.getItem());
                    this.setResult(DetailviewActivity.RESULT_TO_DELETE, returnIntent);
                    this.finish();
                })
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    // Abbrechen, nichts tun
                    dialog.dismiss();
                })
                .show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detailview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(LOG_TAG, "item.getItemId() " + item.getItemId());
        if (item.getItemId() ==  R.id.addContact) {
            addContact();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void sendSMS(String phonenr) {
        Uri receiverUri= Uri.parse("smsto:"+phonenr);
        Intent sendSMSIntent=new Intent(Intent.ACTION_SENDTO,receiverUri);
        sendSMSIntent.putExtra("sms_body","Neues ToDo für dich:"+ this.viewModel.getItem().getName()+" \nBeschreibung: "+this.viewModel.getItem().getDescription()+"");
        startActivity(sendSMSIntent);
    }

    private void sendMail(String mailAddress) {
        Intent sendMailIntent = new Intent(Intent.ACTION_SEND); // ACTION_SEND statt ACTION_SENDTO
        sendMailIntent.setType("message/rfc822"); // E-Mail-spezifischer MIME-Typ
        sendMailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailAddress});
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "Neues ToDo für dich: " + this.viewModel.getItem().getName());
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, "Beschreibung: " + this.viewModel.getItem().getDescription());

        // Nur kompatible E-Mail-Apps anzeigen
        try {
            startActivity(Intent.createChooser(sendMailIntent, "Wähle eine E-Mail-App aus"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(LOG_TAG, "Keine E-Mail-App gefunden.");
            // Zeige eine Benachrichtigung oder Dialog an
        }
    }

    public void saveItem() {
        Intent returnIntent = new Intent();
        parseDateString(dateEditText.getText().toString());
        viewModel.getItem().setTbdDate(tbdTimestamp);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            viewModel.getItem().setUseridcreated(user.getUid());
        }
        returnIntent.putExtra(OverviewActivity.ARG_ITEM, viewModel.getItem());
        this.setResult(DetailviewActivity.RESULT_OK, returnIntent);
        this.finish();
    }

    public void showDatePicker() {
        Log.i(LOG_TAG, "oncreate called");
        Intent intent = new Intent(DetailviewActivity.this, DatePickerActivity.class);
        startForResult.launch(intent);

    }

    private void parseDateString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Log.e(LOG_TAG, dateString);

        try {
            // String in Date konvertieren
            tbdDate = dateFormat.parse(dateString);
            Log.e(LOG_TAG, "I reach this");

            // Date in Millisekunden umwandeln
            tbdTimestamp = tbdDate.getTime();

        } catch (ParseException e) {

            e.printStackTrace();
        }
    }

    private String getFormatesDateStringFromLong(Long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        if (timestamp == null) {
            timestamp = calendar.getTime().getTime();
            Log.i(LOG_TAG, "calendar " + calendar.getTime());
        }
        // Timestamp in Date umwandeln
        Date date = new Date(timestamp);
        Log.i(LOG_TAG, "date " + date);

        // Date in das gewünschte String-Format umwandeln
        return dateFormat.format(date);
    }

    public void addContact() {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSIONS);
        }
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        selectContactLauncher.launch(selectContactIntent);
    }

    public void readContactDetails(Uri contactUri) {
        Log.i(LOG_TAG, "readContactDetails contact uri is: " + contactUri);

        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int displayNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String displayName = cursor.getString(displayNameColumnIndex);
            Log.i(LOG_TAG, "readContactDetails displayName is: " + displayName);
            int internalContactIDColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String internalContactID = cursor.getString(internalContactIDColumnIndex);
            Log.i(LOG_TAG, "readContactDetails internalContactID is: " + internalContactID);
            if(!viewModel.getItem().getContactIds().contains(internalContactID)){
                viewModel.getItem().getContactIds().add(String.valueOf(internalContactID));
            }
            readContactDetailsForInternalID(Long.parseLong(internalContactID));
            cursor.close();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "requestCode is:"+requestCode);
        if (requestCode == REQUEST_CONTACT_PERMISSIONS) {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "permission granted");
            } else {
                Log.i(LOG_TAG, "permission denied");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }

    public ArrayList<String> getEmails(long contactId) {
        ArrayList<String> emails = new ArrayList<>();
        String querySelectionPattern = ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?";

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null
                , querySelectionPattern, new String[]{String.valueOf(contactId)}, null
        );
        while (cursor.moveToNext()) {
            int emailColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            String email = cursor.getString(emailColumnIndex);
            emails.add(email);
        }
        cursor.close();
        return emails;
    }

    public ArrayList<String> getPhoneNrs(long contactId) {
        ArrayList<String> phoneNumbers = new ArrayList<>();

        String querySelectionPattern = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null
                , querySelectionPattern, new String[]{String.valueOf(contactId)}, null
        );
        //Handynnummer
        while (cursor.moveToNext()) {
            int phoneNumColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int phoneNumTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            String phoneNum = cursor.getString(phoneNumColumnIndex);
            int phoneNumType = cursor.getInt(phoneNumTypeColumnIndex);
            Log.i(LOG_TAG, "readContactDetailsForInternalID phoneNum is: " + phoneNum);
            phoneNumbers.add(phoneNum);
            boolean isMobile = phoneNumType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

            Log.i(LOG_TAG, "readContactDetailsForInternalID isMobile: " + isMobile);
        }
        cursor.close();

        return phoneNumbers;
    }

    public void readContactDetailsForInternalID(long contactId) {
        Log.i(LOG_TAG, "readContactDetailsForInternalID internalContactID is: " + contactId);
        Cursor cursor;
        int hasReadContactPermission = checkSelfPermission(android.Manifest.permission.READ_CONTACTS);
        if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSIONS);
            return;
        }
        //Queryselrctor for ContactNAme
        String querySelectionPattern = ContactsContract.Contacts._ID + "=?";
        cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null
                , querySelectionPattern, new String[]{String.valueOf(contactId)}, null
        );
        //string contactName
        while (cursor.moveToNext()) {
            int displayNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String displayName = cursor.getString(displayNameColumnIndex);;
            viewModel.addContactName(displayName);
        }


        querySelectionPattern = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null
                , querySelectionPattern, new String[]{String.valueOf(contactId)}, null
        );
        //Telefonnummer
        while (cursor.moveToNext()) {
            int phoneNumColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int phoneNumTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            String phoneNum = cursor.getString(phoneNumColumnIndex);
            int phoneNumType = cursor.getInt(phoneNumTypeColumnIndex);
            Log.i(LOG_TAG, "readContactDetailsForInternalID phoneNum is: " + phoneNum);

            boolean isMobile = phoneNumType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            Log.i(LOG_TAG, "readContactDetailsForInternalID isMobile: " + isMobile);
        }
        //Email

        querySelectionPattern = ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?";
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null
                , querySelectionPattern, new String[]{String.valueOf(contactId)}, null
        );
        while (cursor.moveToNext()) {
            int emailColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            String email = cursor.getString(emailColumnIndex);
            Log.i(LOG_TAG, "readContactDetailsForInternalID email is: " + email);
        }




        cursor.close();



    }

    @Override
    public void onDeleteClick(int position, String contact) {
        contactList.remove(position); // Element aus der Liste entfernen
        viewModel.getItem().getContactIds().remove(position);
        adapter.notifyItemRemoved(position); // RecyclerView aktualisieren
        Log.i(LOG_TAG, "Deleted contact: " + contact + " at position: " + position);
    }

    //Versende Emails
    @Override
    public void onSendMailClick(int position, String contact) {
        String contactid= viewModel.getItem().getContactIds().get(position);
        ArrayList<String> email = getEmails(Long.parseLong(contactid));
        if(!email.isEmpty()){
            for (String mail : email) {
                sendMail(mail);
            }
        }
        Log.i(LOG_TAG, "ContactId: " +contactid);

    }

    //Versende SMS
    @Override
    public void onSendSMSClick(int position, String contact) {
        String contactid= viewModel.getItem().getContactIds().get(position);
        ArrayList<String> phoneNumbers = getPhoneNrs(Long.parseLong(contactid));
        if(!phoneNumbers.isEmpty()){
            for (String phoneNumber : phoneNumbers) {
                sendSMS(phoneNumber);
            }
        }
    }
}
