package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends ViewModel   {

    public MutableLiveData<ProcessingState> getProcessingState() {
        return this.processingState;
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
