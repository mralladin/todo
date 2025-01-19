package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithFirebase;
import org.dieschnittstelle.mobile.android.todo.model.SyncDataItemCRUDOperations;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DateItemApplication extends Application {

    protected static String LOG_TAG = "DATA_ITEMS";
    public IDataItemCRUDOperations crudOperations;
    public IDataItemCRUDOperations localCrudOperations;
    public IDataItemCRUDOperations remoteCrudOperations;



    public IDataItemCRUDOperations getRemoteCrudOperations() {
        return remoteCrudOperations;
    }

    public IDataItemCRUDOperations getLocalCrudOperations() {
        return localCrudOperations;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        //this.crudOperations = new RemoteDataItemCRUDOperationsWithRetrofit();
    }

    public boolean checkAccessToBackend() {
        Log.i(LOG_TAG, "Checking access to backend...");
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://console.firebase.google.com/").openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);
            conn.getInputStream();
            Log.i(LOG_TAG, "Access to backend successful");
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Access to backend failed: " + e.getMessage(), e);
            Log.e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }


    public Future<IDataItemCRUDOperations> getCrudOperations() {
        CompletableFuture<IDataItemCRUDOperations> future = new CompletableFuture<>();
        new Thread(() -> {
            boolean backendAvailable = checkAccessToBackend();
            localCrudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
            remoteCrudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
            if (backendAvailable) {
                RemoteDataItemCRUDOperationsWithFirebase remoteCrudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
                LocalDataItemCRUDOperationsWithRoom localCrudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
                //this.crudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
                this.crudOperations = new SyncDataItemCRUDOperations(remoteCrudOperations, localCrudOperations);
            } else {
                this.crudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
            }
            //Initialer Sync
            future.complete(this.crudOperations);
        }).start();
        return future;
    }

}
