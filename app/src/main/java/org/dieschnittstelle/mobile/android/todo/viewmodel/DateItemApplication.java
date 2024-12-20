package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.app.Application;

import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.todo.model.RemoteDataItemCRUDOperationsWithRetrofit;

public class DateItemApplication extends Application {

    private IDataItemCRUDOperations crudOperations;

    @Override
    public void onCreate() {
        super.onCreate();
        this.crudOperations = new RemoteDataItemCRUDOperationsWithRetrofit();
    }

    public IDataItemCRUDOperations getCrudOperations(){
        return  this.crudOperations;
    }
}
