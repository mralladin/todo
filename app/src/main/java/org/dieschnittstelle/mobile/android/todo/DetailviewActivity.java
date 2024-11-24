package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.widgets.DatePickerActivity;

public class DetailviewActivity extends AppCompatActivity {

    protected static final String LOG_TAG = DetailviewActivity.class.getName();
    private TextInputEditText dateEditText;
    private FloatingActionButton saveButton;
    private String selectedDate;
    private EditText itemNameEditedText;
    private CheckBox itemCheckbox;
    private TextView description;
    public DetailviewActivity(){
        Log.i(LOG_TAG,"contructor called");
    }
    DataItem item;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //View setzten weist das layout zu für diese activitiy
        setContentView(R.layout.activity_detailview);

        item = (DataItem) getIntent().getSerializableExtra(OverviewActivity.ARG_ITEM);
        if (item==null){
            item=new DataItem();
        }
        Log.i("Debuger","item"+item);
        itemNameEditedText =  findViewById(R.id.itemName);
        itemNameEditedText.setText(item.getName());

        itemCheckbox =  findViewById(R.id.itemchecked);
        itemCheckbox.setChecked(item.isChecked());

        dateEditText = findViewById(R.id.dateEditText);
        saveButton = findViewById(R.id.saveButton);

        description = findViewById(R.id.itemDescription);
        description.setText(item.getDescription());

        saveButton.setOnClickListener(view -> {
            saveItem();
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void saveItem() {
        Intent returnIntent = new Intent();
        item.setName(itemNameEditedText.getText().toString());
        item.setChecked(itemCheckbox.isChecked());
        item.setDescription(description.getText().toString());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            item.setPhonenr(user.getPhoneNumber());
        }
        returnIntent.putExtra(OverviewActivity.ARG_ITEM,item);
        Log.i("TestLog","Hello");
        this.setResult(DetailviewActivity.RESULT_OK,returnIntent);
        this.finish();
    }

    private void showDatePicker() {
        Log.i(LOG_TAG,"oncreate called");
        Intent intent = new Intent(DetailviewActivity.this,
                DatePickerActivity.class);
        startForResult.launch(intent);

    }


    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedData = data.getStringExtra("result_key");
                        dateEditText.setText(returnedData);
                        Log.i("DateLogg",returnedData);
                    }
                }
            }
    );

}
