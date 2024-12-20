package org.dieschnittstelle.mobile.android.todo.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RemoteDataItemCRUDOperationsWithRetrofit implements IDataItemCRUDOperations{

    public static interface TodoRESTWebAPI{

        //erstelle POST request auf http://localhost:8080/api/todos
        @POST("/api/todos")
        public Call<DataItem> createItem(@Body DataItem item);
        @GET("/api/todos")
        public Call<List<DataItem>> readAllItems();
        @GET("/api/todos/{todoId}")
        public Call<DataItem> readItem(@Path("todoId") long itemId);
        @PUT("/api/todos/{todoId}")
        public Call<Boolean> updateItem(@Path("todoId") long itemId,@Body DataItem item);
        @DELETE("/api/todos/{todoId}")
        public Call<DataItem> deleteItem(@Path("todoId") long itemId);

    }

    private  TodoRESTWebAPI todoRESTWebAPI;

    public RemoteDataItemCRUDOperationsWithRetrofit(){
        Retrofit apibase = new Retrofit.Builder().baseUrl("http://192.168.188.21:8080")
                .addConverterFactory(GsonConverterFactory.create()).build();

        todoRESTWebAPI = apibase.create(TodoRESTWebAPI.class);
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        DataItem createdItem= null;
        try {
            createdItem = todoRESTWebAPI.createItem(item).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createdItem;
    }

    @Override
    public List<DataItem> readAllDataItems() {
        try {
            return todoRESTWebAPI.readAllItems().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataItem readDataItem(long id) {
        try {
            return todoRESTWebAPI.readItem(id).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean updateDataItem(DataItem item) {
        try {
            todoRESTWebAPI.updateItem(item.getId(),item).execute().body();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataItem deleteDataItem(long id) {
        try {
            return   todoRESTWebAPI.deleteItem(id).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
