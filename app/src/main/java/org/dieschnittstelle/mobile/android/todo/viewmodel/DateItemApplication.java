package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.app.Application;
import android.util.Log;

import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithFirebase;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithRetrofit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DateItemApplication extends Application {

    public IDataItemCRUDOperations crudOperations;
    protected static String LOG_TAG = "DATA_ITEMS";

    @Override
    public void onCreate() {
        super.onCreate();
        this.crudOperations = new RemoteDataItemCRUDOperationsWithRetrofit();
    }



    public boolean checkAccessToBackend(){
        Log.i(LOG_TAG, "Checking access to backend...");


            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("http://192.168.188.21:8080/api/todos").openConnection();
                conn.setRequestMethod("GET");
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

    public Future<IDataItemCRUDOperations> getCrudOperations(){
        Log.i("TestLog0","0");
        CompletableFuture<IDataItemCRUDOperations> future = new CompletableFuture<>();
        Log.i("TestLog0","1");
        new Thread(() -> {
            boolean backendAvailable = checkAccessToBackend();
            Log.i("TestLog2","2");
            if(backendAvailable){
                this.crudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
            }else{
                this.crudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
            }
            future.complete(this.crudOperations);
            Log.i("TestLog2","3");
        }).start();
        Log.i("TestLog2","4");
        return  future;
    }
}
