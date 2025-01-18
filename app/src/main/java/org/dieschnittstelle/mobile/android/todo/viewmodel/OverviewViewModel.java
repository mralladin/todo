package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithFirebase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends ViewModel   {

    public MutableLiveData<ProcessingState> getProcessingState() {
        return this.processingState;
    }

    public void addExampleData() {
        //Add new DataItems to crud operations with name a,b,c,d,e,f,g,h,i,j,k,l and prio 0,1,2,3 and
        Calendar calender = Calendar.getInstance();
        Long currentTime = calender.getTimeInMillis();
        DataItem a = new DataItem("a",getRandomPrio(),getCurrentTimePlusMinutes(60),currentTime);
        DataItem b = new DataItem("b",getRandomPrio(),getCurrentTimePlusMinutes(120),currentTime);
        DataItem c = new DataItem("c",getRandomPrio(),getCurrentTimePlusMinutes(180),currentTime);
        DataItem d = new DataItem("d",getRandomPrio(),getCurrentTimePlusMinutes(240),currentTime);
        DataItem e = new DataItem("e",getRandomPrio(),getCurrentTimePlusMinutes(300),currentTime);
        DataItem f = new DataItem("f",getRandomPrio(),getCurrentTimePlusMinutes(360),currentTime);
        DataItem g = new DataItem("g",getRandomPrio(),getCurrentTimePlusMinutes(420),currentTime);
        DataItem h = new DataItem("h",getRandomPrio(),getCurrentTimePlusMinutes(480),currentTime);
        new Thread(()->{
        this.crudOperations.createDataItem(a);
        this.crudOperations.createDataItem(b);
        this.crudOperations.createDataItem(c);
        this.crudOperations.createDataItem(d);
        this.crudOperations.createDataItem(e);
        this.crudOperations.createDataItem(f);
        this.crudOperations.createDataItem(g);
        this.crudOperations.createDataItem(h);
        }).start();
        getDataItems().addAll( List.of(a,b,c,d,e,f,g,h));
    }

    //get Random int beetween 0 and 3 0 and 3 included
    public int getRandomPrio(){
        return (int) (Math.random() * 4);
    }

    //Method which returns long value of current date plus added minutes in long
    public long getCurrentTimePlusMinutes(int minutes){
        Calendar calender = Calendar.getInstance();
        Long currentTime = calender.getTimeInMillis();
        calender.setTimeInMillis(currentTime);
        calender.add(Calendar.MINUTE, minutes);
        return calender.getTimeInMillis();
    }


    public static enum ProcessingState {RUNNING, DONE,RUNNING_LONG};
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static String LOG_TAG = "OverviewActivityViewModel";

    private IDataItemCRUDOperations crudOperations;

    private MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();

    private static Comparator<DataItem> SORT_BY_CHECKED_AND_NAME = Comparator.comparing(DataItem::isChecked).thenComparing(DataItem::getName);

    private static Comparator<DataItem> SORT_BY_CHECKED_AND_PRIORITY = Comparator.comparing(DataItem::isChecked).reversed().thenComparing(DataItem::getPrio).reversed();

    private Comparator<DataItem> currentSorter = SORT_BY_CHECKED_AND_NAME;

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




    public void createDataItem(DataItem itemToBeCreated) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(()->{
            DataItem createdItem = this.crudOperations.createDataItem(itemToBeCreated);
            getDataItems().add(createdItem);
            processingState.postValue(ProcessingState.DONE);
        }).start();;
        sortItems("");
    }


    public void setSorter(Comparator<DataItem> sorter){
        this.currentSorter = sorter;
    }

    public void sortItems(String method){
        if(method.equals("priority")){
            setSorter(SORT_BY_CHECKED_AND_PRIORITY);
        }else if(method.equals("name")){
            setSorter(SORT_BY_CHECKED_AND_NAME);
        }

        processingState.setValue(ProcessingState.RUNNING);
        getDataItems().sort(this.currentSorter);
        processingState.postValue(ProcessingState.DONE);
    }

    public void readAllDataItems() {
        processingState.setValue(ProcessingState.RUNNING_LONG);

        new Thread(() -> {
            this.crudOperations.syncDataItems(this);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(this.crudOperations!=null){
                List<DataItem> items = this.crudOperations.readAllDataItems();
                getDataItems().addAll(items);
            }
            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void deleteAllLocalTodos(IDataItemCRUDOperations localOperationsparam) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                List<DataItem> localTodos = localOperationsparam.readAllDataItems();
                for (DataItem todo : localTodos) {
                    localOperationsparam.deleteDataItem(todo);
                    getDataItems().remove(todo);
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
                    remoteOperationsparam.deleteDataItem(todo);
                }

                Log.i("TODO_APP", "Alle Remote-Todos gelöscht.");
            } catch (Exception e) {
                Log.e("TODO_APP", "Fehler beim Löschen von Remote-Todos: " + e.getMessage(), e);
            }
            processingState.postValue(ProcessingState.DONE);

        }).start();
    }


    public void updateDataItem(DataItem itemFromDetailViewToBeModifiedInList) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        executorService.execute(() -> {
            boolean updated = this.crudOperations.updateDataItem(itemFromDetailViewToBeModifiedInList);
            Log.e("TestLog5", "updated: " + updated);
            if(updated) {
                //showMessage(getString(R.string.on_result_from_detailview_msg) + itemFromDetailViewToBeModifiedInList.getName());
                int itemPosition = getDataItems().indexOf(itemFromDetailViewToBeModifiedInList);

                DataItem existingItemInList = getDataItems().get(itemPosition);
                existingItemInList.setName(itemFromDetailViewToBeModifiedInList.getName());
                existingItemInList.setDescription(itemFromDetailViewToBeModifiedInList.getDescription());
                existingItemInList.setPrio(itemFromDetailViewToBeModifiedInList.getPrio());
                existingItemInList.setTbdDate(itemFromDetailViewToBeModifiedInList.getTbdDate());
                existingItemInList.setChecked(itemFromDetailViewToBeModifiedInList.isChecked());
                processingState.postValue(ProcessingState.DONE);
            }
        });
        sortItems("");


    }

}
