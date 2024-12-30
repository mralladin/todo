package org.dieschnittstelle.mobile.android.todo;

import static java.lang.String.format;

import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityOverviewStructuredListitemViewBinding;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithRetrofit;
import org.dieschnittstelle.mobile.android.todo.security.AuthManager;
import org.dieschnittstelle.mobile.android.todo.viewmodel.DateItemApplication;
import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class OverviewActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<DataItem> listviewAdapter;
    public static String ARG_ITEM="item";
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isOnline=false;
    private OverviewViewModel viewmodel;
    private static String LOG_TAG = "OverviewActivity";
    private HashMap<String, CountDownTimer> activeTimers = new HashMap<>();



    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE = 2;
    private IDataItemCRUDOperations crudOperations;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("TestLog0","here?");
        AuthManager authManager = new AuthManager();
        setContentView(R.layout.activity_overview);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
       //monitorConnectionStatus();

        //crudOperations=new LocalDataItemCRUDOperationsWithRoom(this);
        try {
            Log.i("TestLog1","here?");
            Future<IDataItemCRUDOperations> crudOperationsFuture = ((DateItemApplication) getApplication()).getCrudOperations();
            Log.i("TestLog2","here?");
            crudOperations = crudOperationsFuture.get();

            viewmodel = new ViewModelProvider(this).get(OverviewViewModel.class);
            Log.i("TestLog2","crudOperations:" + crudOperations.toString());
            viewmodel.setCrudOperations(crudOperations);


        progressBar = findViewById(R.id.progressbar);

       setOnlineStatus();


        viewmodel.getProcessingState().observe(this, processingState -> {
            Log.i("TestLog2","observe processing state"+processingState);
            if(processingState == OverviewViewModel.ProcessingState.RUNNING_LONG){
                this.progressBar.setVisibility(View.VISIBLE);
            }else if(processingState == OverviewViewModel.ProcessingState.DONE){
                this.progressBar.setVisibility(View.GONE);
                for (String value : activeTimers.keySet()) {
                    if(activeTimers.get(value)!=null){
                        activeTimers.get(value).cancel();
                    }
                }
                activeTimers.clear();
                this.listviewAdapter.notifyDataSetChanged();
             };

        });



        //if(isOnline) {

            if (authManager.getCurrentUser() == null) {
                // Kein Benutzer angemeldet -> zur Login-Seite weiterleiten
                //startLoginActivity();

            } else {
                // Benutzer ist angemeldet -> App normal starten
                Toast.makeText(this, "Willkommen, " + authManager.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                ((TextView) (findViewById(R.id.userNameTextview))).setText(authManager.getCurrentUser().getEmail());
            }
        //}

        if(!viewmodel.isInitialised()) {
            Log.i(LOG_TAG,"load data to be shown in list view...");
            Log.i("TestLog2","load data to be shown in list view...");
            viewmodel.readAllDataItems();
            viewmodel.setInitialised(true);
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



        listviewAdapter= new ArrayAdapter<>(this,R.layout.activity_overview_structured_listitem_view,viewmodel.getDataItems()){



            @NonNull
            @Override
            public View getView(int position, @Nullable View recyclableListItemView, @NonNull ViewGroup parent) {
                Log.i(LOG_TAG, "getView() for position: "+position);
                ActivityOverviewStructuredListitemViewBinding binding;
                View listItemView=null;
                DataItem listItem = getItem(position);



                //If now view can be recycled we need to create a new one
                if(recyclableListItemView == null) {
                    binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.activity_overview_structured_listitem_view,null,false);
                    listItemView = binding.getRoot();
                    listItemView.setTag(binding);
                 }
                else {
                    listItemView = recyclableListItemView;
                    binding = (ActivityOverviewStructuredListitemViewBinding) listItemView.getTag();
                }

                binding.setItem(listItem);


                setImageViewColor((ImageView)listItemView.findViewById(R.id.priorityIcon),listItem.getPrio());
                ProgressBar progressbarOfEachElem= listItemView.findViewById(R.id.progressBarOfEachItem);
                //Progress Text
                TextView progressText = listItemView.findViewById(R.id.progressTextOfEachItem);
                ConstraintLayout listItemContainer = listItemView.findViewById(R.id.listItemContainer);
                Log.i("Debuger","positionViewElem"+position);

                setTimersAndTextForEachListItem(position,listItemContainer,progressText,progressbarOfEachElem,listItemView,listItem);

                // Delete-Button Klick-Listener
                Button deleteButton = listItemView.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(v -> {
                    deleteItem(position);
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



        }
        catch (Exception e){
        }

    }

    private void setOnlineStatus() {
        // Holen des ImageView-Elements
        ImageView connectionStatusImageView = findViewById(R.id.connection_status);
        // Überprüfen der Verbindung zum Backend
        new Thread(() -> {
            boolean isConnected = ((DateItemApplication) getApplication()).checkAccessToBackend();

            if (isConnected) {
                // Wenn verbunden, setze das grüne Icon
                connectionStatusImageView.setImageResource(R.drawable.baseline_cloud_done_24);  // grünes Icon
            } else {
                // Wenn nicht verbunden, setze das rote Icon
                connectionStatusImageView.setImageResource(R.drawable.baseline_cloud_off_24);  // rotes Icon
            }
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
        DataItem listItem = viewmodel.getDataItems().get(position);
        AtomicInteger currentPriority = new AtomicInteger(listItem.getPrio());
        // Dialog erstellen
        new AlertDialog.Builder(this)
                .setTitle("Priorität auswählen")
                .setSingleChoiceItems(priorities, currentPriority.get(), (dialog, which) -> {
                    // Temporäre Auswahl speichern
                    currentPriority.set(which);
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    listItem.setPrio(currentPriority.get());
                    viewmodel.updateDataItem(listItem);
                })
                .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void deleteItem(int position) {
        DataItem listItem = viewmodel.getDataItems().get(position);
        // Dialog zur Bestätigung anzeigen
        new AlertDialog.Builder(this)
                .setTitle("Löschen bestätigen")
                .setMessage("Möchten Sie dieses Element wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    // Timer stoppen, falls vorhanden
                    Log.i("DebuggingTimers","val to Remove"+String.valueOf(position));
                    synchronized(activeTimers) {
                        CountDownTimer timer = activeTimers.remove(String.valueOf(position));
                        if (timer != null) {
                            timer.cancel();
                            Log.i("DebuggingTimers", "Timer für Position " + position + " wurde entfernt.");
                        } else {
                            Log.w("DebuggingTimers", "Kein Timer für Position " + position + " gefunden.");
                        }
                    }


                    viewmodel.getProcessingState().setValue(OverviewViewModel.ProcessingState.RUNNING_LONG);
                    new Thread(() -> {

                        // Element aus der Datenbank entfernen
                        crudOperations.deleteDataItem(listItem);

                        // Element aus der lokalen Liste entfernen
                        runOnUiThread(() -> {
                            viewmodel.getDataItems().remove(position);
                            viewmodel.getProcessingState().postValue(OverviewViewModel.ProcessingState.DONE);
                            Toast.makeText(OverviewActivity.this, listItem.getName() + " gelöscht", Toast.LENGTH_SHORT).show();
                        });
                    }).start();



                })
                .setNegativeButton("Abbrechen", (dialog, which) -> {
                    // Abbrechen, nichts tun
                    dialog.dismiss();
                })
                .show();
    }
    private void setTimersAndTextForEachListItem(
            int position,
            ConstraintLayout listItemContainer,
            TextView progressText,
            ProgressBar progressBar,
            View listItemView,
            DataItem listItem) {

        // Berechne verbleibende Zeit und Gesamtzeit
        long remainingTime = calculateRemainingTime(listItem);
        long totalTime = calculateTotalTime(listItem);
        // Verhindere Division durch 0
        if (remainingTime <= 0) {
            progressBar.setProgress(0);
            progressText.setText(R.string.finish);
            listItemContainer.setEnabled(false);
            /*listItemContainer.setBackgroundColor(
                    listItemView.getContext().getResources().getColor(R.color.todo_text_expired)
            );*/
            return;
        }else{
        // Setze den initialen Fortschritt
        progressBar.setMax(100);
        updateProgress(progressBar, progressText, remainingTime, totalTime);

        // Starte den Timer
        CountDownTimer timer= new CountDownTimer(remainingTime, 1000) { // Aktualisierung jede Sekunde
            @Override
            public void onTick(long millisUntilFinished) {
                updateProgress(progressBar, progressText, millisUntilFinished, totalTime);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                progressText.setText(R.string.finish);
                listItemContainer.setEnabled(false);
                /*listItemContainer.setBackgroundColor(
                        listItemView.getContext().getResources().getColor(R.color.todo_text_expired)
                );*/
            }
        }.start();
            for (String value : activeTimers.keySet()) {
                Log.i("DebuggingTimers","key: "+value);
            }
            if (!activeTimers.containsKey(String.valueOf(position))) {
                activeTimers.put(String.valueOf(position), timer);
            }

        }
    }

    // Hilfsmethode zur Aktualisierung des Fortschritts
    private void updateProgress(ProgressBar progressBar, TextView progressText, long remainingTime, long totalTime) {
        int progress = (int) Math.round((remainingTime * 100.0) / totalTime);
        Log.i("Debuger","progress: "+progress);

        progressBar.setProgress(progress);
        progressText.setText(progress + "%");
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
        setOnlineStatus();
        // Neue Daten hinzufügen oder Aktion durchführen
        Log.i("DebuggingTimers","timer size"+activeTimers.size());
        for (String value : activeTimers.keySet()) {
            Log.i("DebuggingTimers","key: "+value);
        }

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

                viewmodel.updateDataItem(itemFromDetailViewToBeModifiedInList);
                //CALL CRUD
                Log.i("TestLog5","l: "+itemFromDetailViewToBeModifiedInList.getTbdDate());
                Log.i("TestLog5",ARG_ITEM);


            }
            else {
                showMessage("no results");
            }
        }
        else if(requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE){
            if(resultCode == OverviewActivity.RESULT_OK){
                DataItem itemToBeCreated = (DataItem) data.getSerializableExtra(ARG_ITEM);
               // listviewAdapter.add(itemToBeCreated);
                viewmodel.createDataItem(itemToBeCreated);

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
                    setUserName();
                }
            });

    }

    private void setUserName() {
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
        if(item.getStartTime()==null || item.getTbdDate()==null){

            Calendar calendar = Calendar.getInstance();
            calendar.add(0,1);
            item.setStartTime(calendar.getTimeInMillis());
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG,"ondestry");
        viewmodel.setCrudOperations(null);

    }
}
