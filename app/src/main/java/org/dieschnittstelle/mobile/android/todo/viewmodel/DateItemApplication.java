package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.LocalDataItemCRUDOperationsWithRoom;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithFirebase;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithRetrofit;
import org.dieschnittstelle.mobile.android.todo.model.SyncDataItemCRUDOperations;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DateItemApplication extends Application {

    public IDataItemCRUDOperations crudOperations;
    protected static String LOG_TAG = "DATA_ITEMS";

    public IDataItemCRUDOperations getRemoteCrudOperations() {
        return remoteCrudOperations;
    }

    public IDataItemCRUDOperations getLocalCrudOperations() {
        return localCrudOperations;
    }

    public IDataItemCRUDOperations localCrudOperations;
    public IDataItemCRUDOperations remoteCrudOperations;
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        //this.crudOperations = new RemoteDataItemCRUDOperationsWithRetrofit();
    }

    public boolean checkAccessToBackend(){
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



    public Future<IDataItemCRUDOperations> getCrudOperations(){
        CompletableFuture<IDataItemCRUDOperations> future = new CompletableFuture<>();
        new Thread(() -> {
            boolean backendAvailable = checkAccessToBackend();
            localCrudOperations =  new LocalDataItemCRUDOperationsWithRoom(this);
            remoteCrudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
            if(backendAvailable){
                RemoteDataItemCRUDOperationsWithFirebase remoteCrudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
                LocalDataItemCRUDOperationsWithRoom localCrudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
                //this.crudOperations = new RemoteDataItemCRUDOperationsWithFirebase();
                this.crudOperations = new SyncDataItemCRUDOperations(remoteCrudOperations,localCrudOperations);
            } else {
                this.crudOperations = new LocalDataItemCRUDOperationsWithRoom(this);
            }
            //Initialer Sync
            future.complete(this.crudOperations);
        }).start();
        return  future;
    }

}
