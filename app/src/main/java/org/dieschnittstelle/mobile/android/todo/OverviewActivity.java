package org.dieschnittstelle.mobile.android.todo;

import static java.lang.String.format;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.security.AuthManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OverviewActivity extends AppCompatActivity {

    private ListView listView;
    private List<DataItem> listData=new ArrayList<>();
    private ArrayAdapter<DataItem> listviewAdapter;
    public static String ARG_ITEM="item";
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isOnline=false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE = 2;
    private IDataItemCRUDOperations crudOperations;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthManager authManager = new AuthManager();
        setContentView(R.layout.activity_overview);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        monitorConnectionStatus();


        if(isOnline){
        if (authManager.getCurrentUser() == null) {
            // Kein Benutzer angemeldet -> zur Login-Seite weiterleiten
            startLoginActivity();

        } else {
            // Benutzer ist angemeldet -> App normal starten
            Toast.makeText(this, "Willkommen, " + authManager.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            ((TextView)(findViewById(R.id.userNameTextview))).setText(authManager.getCurrentUser().getEmail());
        }
        }





        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Swipe-Refresh-Listener hinzufügen
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Aktion ausführen, z. B. neue Daten laden
            refreshData();

            // Nach Abschluss der Aktion den Ladespinner ausblenden
            swipeRefreshLayout.setRefreshing(false);
        });

// Optional: Überprüfen, ob getSupportActionBar() null ist
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ToDo's");
        }
        if (getSupportActionBar() == null) {
            Log.e("ToolbarError", "SupportActionBar konnte nicht initialisiert werden");
        }

        ImageButton logoutButton= findViewById(R.id.toolbar_button);
        logoutButton.setOnClickListener(a ->{
            authManager.logout();
            startLoginActivity();
        });

        listView=findViewById(R.id.listview);

        this.crudOperations=new LocalItemCRUDOperationsWithRoom(this);

        listviewAdapter= new ArrayAdapter<>(this,R.layout.activity_overview_simple_list_item_view,listData){

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                ViewGroup listItemView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_overview_simple_list_item_view,null);
                Log.i("ViewLog", "Aufruf der View Elemente");

                DataItem listItem = getItem(position);
                ((TextView)listItemView.findViewById(R.id.itemNameInOverview)).setText(listItem.getName());

                setImageViewColor((ImageView)listItemView.findViewById(R.id.priorityIcon),listItem.getPrio());
                ProgressBar progressbarOfEachElem= listItemView.findViewById(R.id.progressBarOfEachItem);
                TextView progressText = listItemView.findViewById(R.id.progressTextOfEachItem);
                ConstraintLayout listItemContainer = listItemView.findViewById(R.id.listItemContainer);
                setTimersAndTextForEachListItem(listItemContainer,progressText,progressbarOfEachElem,listItemView,listItem);
                ((CheckBox)listItemView.findViewById(R.id.itemChecked)).setChecked(listItem.isChecked());
                ((CheckBox)listItemView.findViewById(R.id.itemChecked)).setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                listItem.setChecked(isChecked);
                            }
                        }
                );



                //DELETE
                // Delete-Button Klick-Listener
                Button deleteButton = listItemView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(v -> {
                    new Thread(() -> {
                        // Element aus der Datenbank entfernen
                        crudOperations.deleteDataItem(listItem.getId());

                        // Element aus der lokalen Liste entfernen
                        runOnUiThread(() -> {
                            listData.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(OverviewActivity.this, listItem.getName() + " gelöscht", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });


                return listItemView;


            }
        };

        listView.setAdapter(listviewAdapter);
        listView.setOnItemLongClickListener((adapterView, view, position, id) -> {
            // Zeige das Kontextmenü
            showPopupMenu(view, position);
            return true; // Rückgabe true, um den langen Klick zu konsumieren
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataItem selectedItem= listviewAdapter.getItem(position);
                showDetailviewForItem(selectedItem);
            }
        });

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
    private void showPopupMenu(View anchorView, int position) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.context_menu, popupMenu.getMenu());

        // Aktionen für Menüeinträge
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_change_prio:
                    changePriority(position);
                    return true;
                case R.id.action_delete:
                    deleteItem(position);
                    return true;
                case R.id.action_share:
                   // shareItem(position);
                    return true;
                default:
                    return false;
            }
        });

        // Menü anzeigen
        popupMenu.show();
    }

    private void changePriority(int position) {
        // Optionen für die Priorität
        String[] priorities = {"Niedrig", "Mittel", "Hoch", "Sehr hoch"};
        int[] selectedPriority = {0}; // Standardauswahl (Niedrig)
        DataItem listItem = listData.get(position);
        AtomicInteger currentPriority = new AtomicInteger(listItem.getPrio());

        // Dialog erstellen
        new AlertDialog.Builder(this)
                .setTitle("Priorität auswählen")
                .setSingleChoiceItems(priorities, currentPriority.get(), (dialog, which) -> {
                    // Temporäre Auswahl speichern
                    currentPriority.set(which);
                })
                .setPositiveButton("OK", (dialog, which) -> {


                    // Priorität dem Element zuweisen
                    String newPriority = priorities[selectedPriority[0]];

                    listItem.setPrio(currentPriority.get());

                    listviewAdapter.notifyDataSetChanged(); // Adapter aktualisieren
                    new Thread(() -> {
                        // Element aus der Datenbank entfernen
                        crudOperations.updateDataItem(listItem);

                        // Element aus der lokalen Liste entfernen
                        runOnUiThread(() -> {
                            listviewAdapter.notifyDataSetChanged(); // Adapter aktualisieren

                            Toast.makeText(this, "Priorität auf " + newPriority + " gesetzt", Toast.LENGTH_SHORT).show();
                        });
                    }).start();

                })
                .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void deleteItem(int position) {
        // Dialog zur Bestätigung anzeigen
        new AlertDialog.Builder(this)
                .setTitle("Löschen bestätigen")
                .setMessage("Möchten Sie dieses Element wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    // Element aus der Liste entfernen
                    listviewAdapter.notifyDataSetChanged();
                    new Thread(() -> {
                        // Element aus der Datenbank entfernen
                        DataItem listItem = listData.get(position);

                        crudOperations.deleteDataItem(listItem.getId());

                        // Element aus der lokalen Liste entfernen
                        runOnUiThread(() -> {
                            listData.remove(position);
                            listviewAdapter.notifyDataSetChanged();
                            Toast.makeText(OverviewActivity.this, listItem.getName() + " gelöscht", Toast.LENGTH_SHORT).show();
                        });
                    }).start();

                    Toast.makeText(this, "Element gelöscht", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    // Abbrechen, nichts tun
                    dialog.dismiss();
                })
                .show();
    }
    private void setTimersAndTextForEachListItem(ConstraintLayout listItemContainer,TextView progressText,ProgressBar progressBar, View listItemView, DataItem listItem) {
        // Setze den Timer für dieses Element
        long remainingTime = calculateRemainingTime(listItem); // Berechne die verbleibende Zeit
        int maxProgress = 100; // Setze die maximale ProgressBar-Werte
        long totalTime=calculateTotalTime(listItem);
        progressBar.setMax(maxProgress);

        new CountDownTimer(remainingTime, 10) {
            private boolean firstTick = true;

            @Override
            public void onTick(long millisUntilFinished) {
                //int progress = (int) ((millisUntilFinished * maxProgress) / remainingTime);
                int progress = (int) Math.ceil((millisUntilFinished * 100.0) / totalTime);
                Log.i("TimerLog","totalTime: "+totalTime);
                Log.i("TimerLog","milis until finished: "+millisUntilFinished * 100.0);
                if (firstTick) {
                    // Erzwinge 100% beim Start
                    progressBar.setProgress(100);
                    progressText.setText("100%");
                    firstTick = false;
                    return;
                }
                progressBar.setProgress(progress);
                progressText.setText(progress+"%");
                //progressBar.setProgress(50);


            }

            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                progressText.setText(R.string.finish);
                //listItemContainer.setTextColor(ContextCompat.getColor(listItemView.getContext(), R.color.todo_text_expired));
                listItemContainer.setEnabled(false);
                listItemContainer.setBackgroundColor(
                        listItemView.getContext().getResources().getColor(R.color.todo_text_expired)
                );
            }
        }.start();
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void showDetailviewForItem(DataItem item){
            Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
            callDetailviewIntent.putExtra(ARG_ITEM,item);
            startActivityForResult(callDetailviewIntent, REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT);
        }

    private void refreshData() {
        // Neue Daten hinzufügen oder Aktion durchführen

        listviewAdapter.notifyDataSetChanged();
    }

    private void setImageViewColor(ImageView priorityIcon,int priority){

        switch (priority) {
            case 0: // Niedrige Priorität
                priorityIcon.setColorFilter(ContextCompat.getColor(this, R.color.gray));
                break;
            case 1: // Mittlere Priorität
                priorityIcon.setColorFilter(ContextCompat.getColor(this, R.color.blue));
                break;
            case 2: // Hohe Priorität
                priorityIcon.setColorFilter(ContextCompat.getColor(this, R.color.orange));
                break;
            case 3: // Höchste Priorität
                priorityIcon.setColorFilter(ContextCompat.getColor(this, R.color.red));
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT){
            if(resultCode== OverviewActivity.RESULT_OK){
                Log.i("TestLog","Results ok from detail view");
                Log.i("TestLog",ARG_ITEM);

                DataItem itemFromDetailViewToBeModifiedInList =(DataItem) data.getSerializableExtra(ARG_ITEM);
                //CALL CRUD
                Log.i("TestLog5","l: "+itemFromDetailViewToBeModifiedInList.getTbdDate());
                Log.i("TestLog5",ARG_ITEM);

                boolean updated = this.crudOperations.updateDataItem(itemFromDetailViewToBeModifiedInList);

                if(updated) {
                    //showMessage(getString(R.string.on_result_from_detailview_msg) + itemFromDetailViewToBeModifiedInList.getName());
                    int itemPosition = listData.indexOf(itemFromDetailViewToBeModifiedInList);

                    DataItem existingItemInList = listData.get(itemPosition);
                    existingItemInList.setName(itemFromDetailViewToBeModifiedInList.getName());
                    existingItemInList.setDescription(itemFromDetailViewToBeModifiedInList.getDescription());
                    existingItemInList.setPrio(itemFromDetailViewToBeModifiedInList.getPrio());

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

    private long calculateRemainingTime(DataItem item) {
        long currentTime = System.currentTimeMillis();
        Log.i("timerlog", "Time: "+item.getTbdDate());
        long endTime=0;
        if(item.getTbdDate()!=null){
            endTime = item.getTbdDate();
        }
        long remainingTime=Math.max(0, endTime - currentTime);
        Log.i("timerlog", "Remaining Time: "+remainingTime);
        return remainingTime;
    }
    private void monitorConnectionStatus() {
        db.collection("dataitems").document("dataitems")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    updateCloudStatus(false); // Kein Zugriff -> Nicht verbunden
                } else {
                    isOnline=true;
                    updateCloudStatus(true); // Verbunden
                }
            });

    }
    private void updateCloudStatus(boolean isConnected) {
        ImageView cloudStatus = findViewById(R.id.connection_status);
        Log.i("cloudStatusLog","connected? "+isConnected);
        if (isConnected) {
            cloudStatus.setColorFilter(Color.GREEN);
        } else {
            cloudStatus.setColorFilter(Color.RED);
        }
    }


    private long calculateTotalTime(DataItem item) {
        Log.i("TimerLog","Starttime: "+item.getStartTime());
        Log.i("TimerLog","TBDtime: "+item.getTbdDate());
        Date startTime = new Date(item.getStartTime());
        Date tbdDate = new Date();

        if(item.getTbdDate()!=null){
            tbdDate = new Date(item.getTbdDate());
        }

        if (startTime != null && tbdDate != null) {
            return tbdDate.getTime() - startTime.getTime(); // Dauer in Millisekunden
        }
        return 0;
    }

    private void showMessage(String message){
         Toast.makeText(OverviewActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
