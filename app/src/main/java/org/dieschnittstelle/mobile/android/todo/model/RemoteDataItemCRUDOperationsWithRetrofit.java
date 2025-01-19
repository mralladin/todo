package org.dieschnittstelle.mobile.android.todo.model;

import android.util.Log;

import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RemoteDataItemCRUDOperationsWithRetrofit implements IDataItemCRUDOperations {

    private final TodoRESTWebAPI todoRESTWebAPI;

    public RemoteDataItemCRUDOperationsWithRetrofit() {
        Retrofit apibase = new Retrofit.Builder().baseUrl("http://192.168.188.21:8080")
                .addConverterFactory(GsonConverterFactory.create()).build();

        todoRESTWebAPI = apibase.create(TodoRESTWebAPI.class);
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        DataItem createdItem = null;
        try {
            Response<DataItem> response = todoRESTWebAPI.createItem(item).execute();
            if (response.isSuccessful()) {
                System.out.println(response.body()); // Debug-Ausgabe
                createdItem = todoRESTWebAPI.createItem(item).execute().body();
            } else {
                Log.e("Response", "Log:" + response.errorBody().string());
            }
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
            todoRESTWebAPI.updateItem(item.getId(), item).execute().body();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean deleteDataItem(DataItem item) {
        try {
            Log.i("TestLog1", "dknallt da?");
            Boolean d = todoRESTWebAPI.deleteItem(item.getId()).execute().body();
            Log.i("TestLog1", "d:" + d);
            return d;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void syncDataItems(OverviewViewModel viewModel) {
    }

    public interface TodoRESTWebAPI {

        //erstelle POST request auf http://localhost:8080/api/todos
        @POST("/api/todos")
        Call<DataItem> createItem(@Body DataItem item);

        @GET("/api/todos")
        Call<List<DataItem>> readAllItems();

        @GET("/api/todos/{todoId}")
        Call<DataItem> readItem(@Path("todoId") long itemId);

        @PUT("/api/todos/{todoId}")
        Call<Boolean> updateItem(@Path("todoId") long itemId, @Body DataItem item);

        @DELETE("/api/todos/{todoId}")
        Call<Boolean> deleteItem(@Path("todoId") long itemId);

    }
}
