package org.dieschnittstelle.mobile.android.todo.model;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.Collections;
import java.util.List;
@Entity
public class LocalItemCRUDOperationsWithRoom implements IDataItemCRUDOperations{
    @Dao
    public static interface SQLiteDataItemCRUDOperations{
        @Insert
        public long createItem(DataItem item);
        @Update
        public void updateItem(DataItem item);
        @Delete

        public void  deleteItem(DataItem item);
        @Query("SELECT * FROM dataitem")
        public List<DataItem> readAllItems();
        @Query("SELECT * FROM dataitem WHERE id=(:id)")
        public DataItem readItem(long id);

    }
   @Database(entities = DataItem.class, version = 1)
    public abstract static class DataItemDatabase extends RoomDatabase{

        public abstract SQLiteDataItemCRUDOperations getDao();
    }

    private DataItemDatabase db;

    public LocalItemCRUDOperationsWithRoom(Context context ){
        db = Room.databaseBuilder(context.getApplicationContext(),
                DataItemDatabase.class ,"dataitems-db"
                ).build();
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        long newId = db.getDao().createItem(item);
        item.setId(newId);
        return item;
    }

    @Override
    public List<DataItem> readAllDataItems()  {
        return db.getDao().readAllItems();
    }

    @Override
    public DataItem readDataItem(long id) {
        return null;
    }

    @Override
    public Boolean updateDataItem(DataItem item) {
        return true;
    }

    @Override
    public DataItem deleteDataItem(long id) {
        return null;
    }
}
