package org.dieschnittstelle.mobile.android.todo;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import org.dieschnittstelle.mobile.android.skeleton.R;
import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityOverviewStructuredListitemViewBinding;
import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.security.AuthManager;
import org.dieschnittstelle.mobile.android.todo.viewmodel.DateItemApplication;
import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class OverviewActivity extends AppCompatActivity {

    public static String ARG_ITEM = "item";
    private static final String LOG_TAG = "OverviewActivity";
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT = 1;
    protected final int REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE = 2;
    protected final int RESULT_TO_DELETE = 9;

    private ListView listView;
    private ArrayAdapter<DataItem> listviewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final boolean isOnline = false;
    private OverviewViewModel viewmodel;
    //War buggy und wird aktuell nicht mehr genutzt
    private final HashMap<String, CountDownTimer> activeTimers = new HashMap<>();
    private AuthManager authManager;
    private ImageButton logoutButton;
    private IDataItemCRUDOperations crudOperations;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AuthManager();
        setContentView(R.layout.activity_overview);
        //RefreshLayout sollte ursprünglich zur Synchronisierung genutzt werden
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        //crudOperations=new LocalDataItemCRUDOperationsWithRoom(this);
        try {
            Future<IDataItemCRUDOperations> crudOperationsFuture = ((DateItemApplication) getApplication()).getCrudOperations();
            crudOperations = crudOperationsFuture.get();
            viewmodel = new ViewModelProvider(this).get(OverviewViewModel.class);
            progressBar = findViewById(R.id.progressbar);
            viewmodel.getProcessingState().observe(this, processingState -> {
                if (processingState == OverviewViewModel.ProcessingState.RUNNING_LONG) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (processingState == OverviewViewModel.ProcessingState.DONE) {
                    progressBar.setVisibility(View.GONE);
                    if (listviewAdapter != null)
                        listviewAdapter.notifyDataSetChanged();
                }


            });
            viewmodel.setCrudOperations(crudOperations);
            
            setOnlineStatus();

            if (!viewmodel.isInitialised()) {
                Log.i(LOG_TAG, "load data to be shown in list view...");
                viewmodel.readAllDataItems();
                viewmodel.setInitialised(true);
            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Aktion ausführen, z. B. neue Daten laden

                refreshData();

                // Nach Abschluss der Aktion den Ladespinner ausblenden
                swipeRefreshLayout.setRefreshing(false);
            });



            logoutButton.setOnClickListener(a -> {
                authManager.logout();
                startLoginActivity();
            });

            listView = findViewById(R.id.listview);


            listviewAdapter = new ArrayAdapter<>(this, R.layout.activity_overview_structured_listitem_view, viewmodel.getDataItems()) {

                @NonNull
                @Override
                public View getView(int position, @Nullable View recyclableListItemView, @NonNull ViewGroup parent) {

                    ActivityOverviewStructuredListitemViewBinding binding;
                    View listItemView = null;
                    DataItem listItem = getItem(position);


                    //If now view can be recycled we need to create a new one
                    if (recyclableListItemView == null) {
                        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_overview_structured_listitem_view, null, false);
                        listItemView = binding.getRoot();
                        listItemView.setTag(binding);
                    } else {
                        listItemView = recyclableListItemView;
                        binding = (ActivityOverviewStructuredListitemViewBinding) listItemView.getTag();
                    }

                    binding.setItem(listItem);


                    setImageViewColor(listItemView.findViewById(R.id.priorityIcon), listItem.getPrio());
                    ProgressBar progressbarOfEachElem = listItemView.findViewById(R.id.progressBarOfEachItem);
                    CheckBox checkBoxOfEachElem = listItemView.findViewById(R.id.itemChecked);
                    //Progress Text
                    TextView progressText = listItemView.findViewById(R.id.progressTextOfEachItem);
                    ConstraintLayout listItemContainer = listItemView.findViewById(R.id.listItemContainer);
                    setTimersAndTextForEachListItem(position, listItemContainer, progressText, progressbarOfEachElem, checkBoxOfEachElem, listItemView, listItem);

                    // Delete-Button Klick-Listener
                    Button deleteButton = listItemView.findViewById(R.id.deleteButton);
                    deleteButton.setOnClickListener(v -> {
                        deleteItem(position);
                    });

                    checkBoxOfEachElem.setOnClickListener(v -> {
                        viewmodel.updateDataItem(listItem);
                        viewmodel.sortItems("");
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
                    DataItem selectedItem = listviewAdapter.getItem(position);
                    if (selectedItem.getTimeOver() == null || !selectedItem.getTimeOver()) {
                        showDetailviewForItem(selectedItem);
                    }

                }
            });

            FloatingActionButton addItems = findViewById(R.id.addButton);
            addItems.setOnClickListener(view -> {
                Intent callDetailViewForCreateIntent = new Intent(this, DetailviewActivity.class);
                callDetailViewForCreateIntent.putExtra("REQUEST_CODE", REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE);

                startActivityForResult(callDetailViewForCreateIntent, REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE);


            });

            viewmodel.sortItems("initialSort");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Online Status prüfen kann auch durch synchronise upgedated werden
    private void setOnlineStatus() {
        // Holen des ImageView-Elements
        ImageView connectionStatusImageView = findViewById(R.id.connection_status);
        // Überprüfen der Verbindung zum Backend
        new Thread(() -> {
            logoutButton = findViewById(R.id.toolbar_button);
            boolean isConnected = ((DateItemApplication) getApplication()).checkAccessToBackend();

            if (isConnected) {
                runOnUiThread(() -> {
                    logoutButton.setEnabled(true);
                });
                // Wenn verbunden, setze das grüne Icon
                connectionStatusImageView.setImageResource(R.drawable.baseline_cloud_done_24);  // grünes Icon
                //User weiterleiten wenn online und nicht registriert
                if (authManager.getCurrentUser() == null) {
                    // Kein Benutzer angemeldet -> zur Login-Seite weiterleiten
                    startLoginActivity();

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Willkommen, " + authManager.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        ((TextView) (findViewById(R.id.userNameTextview))).setText(authManager.getCurrentUser().getEmail());
                    });
                }

            } else {
                // Wenn nicht verbunden, setze das rote Icon
                connectionStatusImageView.setImageResource(R.drawable.baseline_cloud_off_24);  // rotes Icon
                runOnUiThread(() -> {
                    logoutButton.setEnabled(false);
                    logoutButton.setVisibility(View.GONE);
                    Toast.makeText(this, "Willkommen, " + "Sie haben keine Verbindung", Toast.LENGTH_SHORT).show();
                    ((TextView) (findViewById(R.id.userNameTextview))).setText("Keine Verbindung");
                });
            }
        }).start();

    }
    //Popup Menü um die Prio zu ändern (Anforderung)  oder Items zu löschen
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
    
    //Alert Dialo um Prio auszuwählen und zu änden
    private void changePriority(int position) {
        // Optionen für die Priorität
        String[] priorities = {"Niedrig", "Mittel", "Hoch", "Sehr hoch"};
        int[] selectedPriority = {0}; // Standardauswahl (Niedrigrn
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

    //Deaktiviert Elemente welche abgelaufen sind
    private void setTimersAndTextForEachListItem(
            int position,
            ConstraintLayout listItemContainer,
            TextView progressText,
            ProgressBar progressBar,
            CheckBox checkBox,
            View listItemView,
            DataItem listItem) {
        // Berechne verbleibende Zeit und Gesamtzeit
        long remainingTime = calculateRemainingTime(listItem);
        long totalTime = calculateTotalTime(listItem);
        // Verhindere Division durch 0
        if (remainingTime <= 0) {
            //show listItem.getTbdDate() to DDMMYYYY HH:MM

            Log.i(LOG_TAG, listItem.getName() + " " + listItem.getTbdDate());
            disableProgressBar(listItem, checkBox);
        } else {
            enableProgressBar(listItem, checkBox);
            // Setze den initialen Fortschritt
            //updateProgress(progressBar, progressText, remainingTime, totalTime);


        }
    }

    //disable Progressbar,Text,Button,CheckBox
    private void disableProgressBar(DataItem listItem, CheckBox checkBox) {
        listItem.setTimeOver(true);
        checkBox.setEnabled(false);
    }

    //enable Progressbar,Text,Button,CheckBox
    private void enableProgressBar(DataItem listItem, CheckBox checkBox) {
        listItem.setTimeOver(false);
        checkBox.setEnabled(true);
    }


    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void showDetailviewForItem(DataItem item) {
        Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
        callDetailviewIntent.putExtra(ARG_ITEM, item);
        startActivityForResult(callDetailviewIntent, REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT);
    }

    private void refreshData() {
        setOnlineStatus();
        if (listviewAdapter != null)
            listviewAdapter.notifyDataSetChanged();
    }

    private void setImageViewColor(ImageView priorityIcon, int priority) {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Verknüpft die Menü-XML-Datei
        inflater.inflate(R.menu.overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_local:
                viewmodel.deleteAllLocalTodos(((DateItemApplication) getApplication()).getLocalCrudOperations());
                return true;
            case R.id.action_delete_remote:
                viewmodel.deleteAllRemoteTodos(((DateItemApplication) getApplication()).getRemoteCrudOperations());
                return true;
            case R.id.addExampleData:
                viewmodel.addExampleData();
                return true;
            case R.id.action_sync:
                viewmodel.getProcessingState().setValue(OverviewViewModel.ProcessingState.RUNNING_LONG);
                new Thread(() -> {
                    crudOperations.syncDataItems(viewmodel,false);
                    viewmodel.getProcessingState().postValue(OverviewViewModel.ProcessingState.DONE);
                }
                ).start();
                return true;
            case R.id.sortItems:
                this.viewmodel.sortItems(viewmodel.FILTER_VALUE_DATE);
                return true;
            case R.id.sortItemsToPrio:
                this.viewmodel.sortItems(viewmodel.FILTER_VALUE_PRIORITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(LOG_TAG, "Results To Delete from detail view"+requestCode+resultCode);

        if (resultCode == RESULT_TO_DELETE) {
            DataItem itemToBeDeleted = (DataItem) data.getSerializableExtra(ARG_ITEM);
            Log.i(LOG_TAG, "Results To Delete from detail view"+requestCode);
            viewmodel.getProcessingState().setValue(OverviewViewModel.ProcessingState.RUNNING_LONG);
            new Thread(() -> {
                // Element aus der Datenbank entfernen
                crudOperations.deleteDataItem(itemToBeDeleted);
                // Element aus der lokalen Liste entfernen
                runOnUiThread(() -> {
                    //delete item from listviewAdapter and notify adapter
                    int position = viewmodel.getDataItems().indexOf(itemToBeDeleted);
                    viewmodel.getDataItems().remove(position);
                    viewmodel.getProcessingState().postValue(OverviewViewModel.ProcessingState.DONE);
                    Toast.makeText(OverviewActivity.this, itemToBeDeleted.getName() + " gelöscht", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }



        if (requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_EDIT) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                Log.i(LOG_TAG, "Results ok from detail view");
                Log.i(LOG_TAG, ARG_ITEM);

                DataItem itemFromDetailViewToBeModifiedInList = (DataItem) data.getSerializableExtra(ARG_ITEM);

                viewmodel.updateDataItem(itemFromDetailViewToBeModifiedInList);


            } else {
                showMessage("no results");
            }
        } else if (requestCode == REQUEST_CODE_FOR_CALL_DETAIL_VIEW_FOR_CREATE) {
            if (resultCode == OverviewActivity.RESULT_OK) {
                DataItem itemToBeCreated = (DataItem) data.getSerializableExtra(ARG_ITEM);
                // listviewAdapter.add(itemToBeCreated);
                viewmodel.createDataItem(itemToBeCreated);

            }
        }  else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    private long calculateRemainingTime(DataItem item) {
        long currentTime = System.currentTimeMillis();
        long endTime = 0;
        if (item.getTbdDate() != null) {
            endTime = item.getTbdDate();
        }
        long remainingTime = Math.max(0, endTime - currentTime);
        return remainingTime;
    }


    private long calculateTotalTime(DataItem item) {
        if (item.getStartTime() == null || item.getTbdDate() == null) {

            Calendar calendar = Calendar.getInstance();
            calendar.add(0, 1);
            item.setStartTime(calendar.getTimeInMillis());
        }
        Date startTime = new Date(item.getStartTime());
        Date tbdDate = new Date();

        if (item.getTbdDate() != null) {
            tbdDate = new Date(item.getTbdDate());
        }

        if (startTime != null && tbdDate != null) {
            return tbdDate.getTime() - startTime.getTime();
        }
        return 0;
    }

    private void showMessage(String message) {
        Toast.makeText(OverviewActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "ondestry");
        viewmodel.setCrudOperations(null);

    }
}
