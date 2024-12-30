package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithFirebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends ViewModel   {

    public MutableLiveData<ProcessingState> getProcessingState() {
        return this.processingState;
    }

    public void deleteAllLocalTodos(IDataItemCRUDOperations localOperationsparam) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                List<DataItem> localTodos = localOperationsparam.readAllDataItems();
                for (DataItem todo : localTodos) {
                    deleteDataItemFromSpecificCrud( localOperationsparam,todo);
                }
                Log.i("TODO_APP", "Alle lokalen Todos gelöscht.");
            } catch (Exception e) {
                Log.e("TODO_APP", "Fehler beim Löschen lokaler Todos: " + e.getMessage(), e);
            }
            processingState.postValue(ProcessingState.DONE);

        }).start();
    }

    public void deleteAllRemoteTodos(IDataItemCRUDOperations remoteOperationsparam) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                List<DataItem> remoteTodos = remoteOperationsparam.readAllDataItems();
                for (DataItem todo : remoteTodos) {
                    deleteDataItemFromSpecificCrud( remoteOperationsparam,todo);
                }

                Log.i("TODO_APP", "Alle Remote-Todos gelöscht.");
            } catch (Exception e) {
                Log.e("TODO_APP", "Fehler beim Löschen von Remote-Todos: " + e.getMessage(), e);
            }
            processingState.postValue(ProcessingState.DONE);

        }).start();
    }

    public static enum ProcessingState {RUNNING, DONE,RUNNING_LONG};
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static String LOG_TAG = "OverviewActivityViewModel";

    //46:00

    private IDataItemCRUDOperations crudOperations;

    private MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();

    private List<DataItem> dataItems = new ArrayList<>();
    private boolean initialised;

    public OverviewViewModel(){
        Log.i(LOG_TAG,"contructer called");}

    public List<DataItem> getDataItems(){
        return dataItems;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }
    public void setCrudOperations(IDataItemCRUDOperations crudOperations) {
        this.crudOperations = crudOperations;
    }

    public void deleteDataItemFromSpecificCrud( IDataItemCRUDOperations specificCrudOperations,DataItem itemToBeDeleted) {
            specificCrudOperations.deleteDataItem(itemToBeDeleted);
            getDataItems().remove(itemToBeDeleted);
    }

    public void syncTodos(boolean backendAvailable,IDataItemCRUDOperations localOperations, IDataItemCRUDOperations remoteOperations) {
        new Thread(() -> {
            try {
                if (backendAvailable) {
                    // Check for local todos
                    List<DataItem> localTodos = localOperations.readAllDataItems();
                    if (localTodos != null && !localTodos.isEmpty()) {
                        Log.i(LOG_TAG, "Local todos found, syncing to backend...");
                        // Delete all remote todos
                        List<DataItem> remoteTodos = remoteOperations.readAllDataItems();
                        for (DataItem remoteTodo : remoteTodos) {
                            remoteOperations.deleteDataItem(remoteTodo);
                            getDataItems().remove(remoteTodo);
                        }
                        // Upload local todos to backend
                        for (DataItem localTodo : localTodos) {
                            remoteOperations.createDataItem(localTodo);
                            getDataItems().add(localTodo);
                        }
                        Log.i(LOG_TAG, "Sync to backend complete.");
                    } else {
                        Log.i(LOG_TAG, "No local todos found, syncing from backend...");
                        // Fetch todos from backend and save locally
                        List<DataItem> remoteTodos = remoteOperations.readAllDataItems();
                        for (DataItem remoteTodo : remoteTodos) {
                            localOperations.createDataItem(remoteTodo);
                            getDataItems().add(remoteTodo);
                        }

                        Log.i(LOG_TAG, "Sync from backend complete.");
                    }
                } else {
                    Log.e(LOG_TAG, "Backend not available, skipping sync.");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error during sync: " + e.getMessage(), e);
            }


        }).start();
    }

    public void createDataItem(DataItem itemToBeCreated) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(()->{
            DataItem createdItem = this.crudOperations.createDataItem(itemToBeCreated);
            getDataItems().add(createdItem);
            processingState.postValue(ProcessingState.DONE);
        }).start();;

    }


    public void readAllDataItems() {

        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //Data to be shown
            Log.i("CrudOps","2"+this.crudOperations);
            //BUG#1 RACE Condition fix because of register???
            if(this.crudOperations!=null){
                List<DataItem> items = this.crudOperations.readAllDataItems();
                getDataItems().addAll(items);
            }
            processingState.postValue(ProcessingState.DONE);


        }).start();
    }


    public void updateDataItem(DataItem itemFromDetailViewToBeModifiedInList) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        executorService.execute(() -> {
            boolean updated = this.crudOperations.updateDataItem(itemFromDetailViewToBeModifiedInList);

            if(updated) {
                //showMessage(getString(R.string.on_result_from_detailview_msg) + itemFromDetailViewToBeModifiedInList.getName());
                int itemPosition = getDataItems().indexOf(itemFromDetailViewToBeModifiedInList);

                DataItem existingItemInList = getDataItems().get(itemPosition);
                existingItemInList.setName(itemFromDetailViewToBeModifiedInList.getName());
                existingItemInList.setDescription(itemFromDetailViewToBeModifiedInList.getDescription());
                existingItemInList.setPrio(itemFromDetailViewToBeModifiedInList.getPrio());

                existingItemInList.setChecked(itemFromDetailViewToBeModifiedInList.isChecked());
                processingState.postValue(ProcessingState.DONE);
            }


        });


    }

}
