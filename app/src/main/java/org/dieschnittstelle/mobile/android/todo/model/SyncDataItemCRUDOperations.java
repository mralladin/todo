package org.dieschnittstelle.mobile.android.todo.model;

import android.util.Log;

import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.util.List;

public class SyncDataItemCRUDOperations implements IDataItemCRUDOperations {
    private static final String LOG_TAG = "SyncDataItemCRUDOperations";
    private final RemoteDataItemCRUDOperationsWithFirebase remoteCrud;
    private final LocalDataItemCRUDOperationsWithRoom localCrud;

    public SyncDataItemCRUDOperations(RemoteDataItemCRUDOperationsWithFirebase remoteCrudOperations, LocalDataItemCRUDOperationsWithRoom localCrudOperations) {
        remoteCrud = remoteCrudOperations;
        localCrud = localCrudOperations;
    }


    @Override
    public DataItem createDataItem(DataItem item) {
        DataItem createdItem = localCrud.createDataItem(item);
        try {
            createdItem = remoteCrud.createDataItem(createdItem);
            localCrud.updateDataItem(createdItem);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating item", e);
        }
        return createdItem;
    }

    @Override
    public List<DataItem> readAllDataItems() {
        return localCrud.readAllDataItems();
    }

    @Override
    public DataItem readDataItem(long id) {
        return localCrud.readDataItem(id);
    }

    @Override
    public Boolean updateDataItem(DataItem item) {
        if (localCrud.updateDataItem(item)) {
            remoteCrud.updateDataItem(item);
        }
        return true;
    }

    @Override
    public Boolean deleteDataItem(DataItem item) {
        if (item != null && localCrud.deleteDataItem(item)) {
            remoteCrud.deleteDataItem(item);
        }
        return true;
    }

    @Override
    public void syncDataItems(OverviewViewModel viewModel) {
        List<DataItem> remoteItems = remoteCrud.readAllDataItems();
        List<DataItem> localItems = localCrud.readAllDataItems();
        //Es gibt Lokale Todo's
        if (!localCrud.readAllDataItems().isEmpty()) {
            //Lösche jedes Remote Item
            for (DataItem remoteItem : remoteItems) {
                remoteCrud.deleteDataItem(remoteItem);
            }
            //Füge jedes Local Item nach dem Remote hinzu
            for (DataItem localItem : localItems) {
                DataItem remoteItem = remoteCrud.createDataItem(localItem);
                //Firebase ID setzten
                localCrud.readDataItem(localItem.getId()).setFirebaseId(remoteItem.getFirebaseId());
                //Log localItem FirebaseId
                Log.i(LOG_TAG, "LocalItem FirebaseId: " + localItem.getFirebaseId());
            }
            for (DataItem remoteItem : remoteCrud.readAllDataItems()) {
                Log.i(LOG_TAG, "RemoteItem FirebaseId: " + remoteItem.getFirebaseId());
            }
        }
        //Es gibt keine Lokalen Todo's
        else {
            //Alle Todos von Remote auf Local übertragen
            for (DataItem remoteItem : remoteItems) {
                localCrud.createDataItem(remoteItem);
                viewModel.getDataItems().add(remoteItem);
            }
        }
    }
}
