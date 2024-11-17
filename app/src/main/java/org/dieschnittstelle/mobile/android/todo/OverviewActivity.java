package org.dieschnittstelle.mobile.android.todo;

import static java.lang.String.format;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalItemCRUDOperationsWithRoom;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    private ListView listView;
    private List<DataItem> listData=new ArrayList<>();
    private ArrayAdapter<DataItem> listviewAdapter;
    public static String ARG_ITEM="item";
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE = 2;
    private IDataItemCRUDOperations crudOperations;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
//        listData.addAll(
//                Arrays.asList("Lorem","Ipsum","Todorala")
//                        .stream()
//                        .map(name -> new DataItem(name)).collect(Collectors.toList())
//        );
        listView=findViewById(R.id.listview);

        this.crudOperations=new LocalItemCRUDOperationsWithRoom(this);

        listviewAdapter= new ArrayAdapter<>(this,R.layout.activity_overview_simple_list_item_view,listData){

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                ViewGroup listItemView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_overview_simple_list_item_view,null);

                DataItem listItem = getItem(position);
                ((TextView)listItemView.findViewById(R.id.itemNameInOvervoew)).setText(listItem.getName());

                ((CheckBox)listItemView.findViewById(R.id.itemChecked)).setChecked(listItem.isChecked());
                ((CheckBox)listItemView.findViewById(R.id.itemChecked)).setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                listItem.setChecked(isChecked);
                            }
                        }
                );

                return listItemView;


            }
        };

        listView.setAdapter(listviewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataItem selectedItem= listviewAdapter.getItem(position);
                showDetailviewForItem(selectedItem);
            }
        });
//        Arrays.asList("Lorem","Ipsum","Todorala")
//                .stream()
//                .map(name -> new DataItem(name))
//                .forEach(item -> {
//            TextView currentItemView = (TextView) getLayoutInflater().inflate(R.layout.activity_overview_simple_list_item_view, null);
//            currentItemView.setText(item.getName());
//            listView.addView(currentItemView);
//            currentItemView.setOnClickListener(view -> showDetailviewForItem(item));
//        });

        FloatingActionButton addItems = findViewById(R.id.addButton);
        addItems.setOnClickListener(view -> {
            Intent callDetailViewForCreateIntent=new Intent(this,DetailviewActivity.class);
            startActivityForResult(callDetailViewForCreateIntent,REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE);


        });

        progressBar = findViewById(R.id.progressbar);
        this.progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {

            //Data to be shown
            List<DataItem> items = null;

            try {
                items = this.crudOperations.readAllDataItems();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            this.listData.addAll(items);

            runOnUiThread(()->{
                this.progressBar.setVisibility(View.GONE);

                listviewAdapter.notifyDataSetChanged();
                    });


        }).start();


    }

        protected void showDetailviewForItem(DataItem item){
            Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
            callDetailviewIntent.putExtra(ARG_ITEM,item);
            startActivityForResult(callDetailviewIntent, REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT){
            if(resultCode== OverviewActivity.RESULT_OK){
                Log.i("TestLog","Results ok from detail view");
                Log.i("TestLog",ARG_ITEM);

                DataItem itemFromDetailViewToBeModifiedInList =(DataItem) data.getSerializableExtra(ARG_ITEM);
                //CALL CRUD
                Log.i("TestLog5","l: "+itemFromDetailViewToBeModifiedInList);
                Log.i("TestLog5",ARG_ITEM);

                boolean updated = this.crudOperations.updateDataItem(itemFromDetailViewToBeModifiedInList);

                if(updated) {
                    //showMessage(getString(R.string.on_result_from_detailview_msg) + itemFromDetailViewToBeModifiedInList.getName());
                    int itemPosition = listData.indexOf(itemFromDetailViewToBeModifiedInList);

                    DataItem existingItemInList = listData.get(itemPosition);
                    existingItemInList.setName(itemFromDetailViewToBeModifiedInList.getName());
                    existingItemInList.setDescription(itemFromDetailViewToBeModifiedInList.getDescription());

                    existingItemInList.setChecked(itemFromDetailViewToBeModifiedInList.isChecked());
                    listviewAdapter.notifyDataSetChanged();
                }
            }
            else {
                showMessage("no results");
            }
        }
        else if(requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE){
            if(resultCode == OverviewActivity.RESULT_OK){
                DataItem itemToBeCreated = (DataItem) data.getSerializableExtra(ARG_ITEM);
               // listviewAdapter.add(itemToBeCreated);
                new Thread(()->{
                    DataItem createdItem = this.crudOperations.createDataItem(itemToBeCreated);

                    listData.add(createdItem);
                    runOnUiThread(()->{
                        listviewAdapter.notifyDataSetChanged();

                    });

                }).start();;

            }
        }


        else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    private void showMessage(String message){
         Toast.makeText(OverviewActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
