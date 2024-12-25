package org.dieschnittstelle.mobile.android.todo.model;

import android.content.Context;
import android.util.Log;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.google.firebase.firestore.FirebaseFirestore;

import org.dieschnittstelle.mobile.android.todo.util.DateConverter;

import java.util.List;
@Entity
public class LocalDataItemCRUDOperationsWithRoom implements IDataItemCRUDOperations{
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
   @Database(entities = DataItem.class, version = 6)
   @TypeConverters({DateConverter.class}) // Converter hinzufügen
   public abstract static class DataItemDatabase extends RoomDatabase{

        public abstract SQLiteDataItemCRUDOperations getDao();
    }

    public static String LOG_TAG = "DATA_ITEMS";
    private SQLiteDataItemCRUDOperations localDao;
    private FirebaseFirestore firestore;

    private DataItemDatabase db;
    public LocalDataItemCRUDOperationsWithRoom(Context context ){
        // Room-Setup
        DataItemDatabase db = Room.databaseBuilder(
                context.getApplicationContext(),
                DataItemDatabase.class,
                "dataitems-db")
                .fallbackToDestructiveMigration() // Alte Daten werden gelöscht

                .build();
        this.localDao = db.getDao();

        // Firestore-Setup
        //this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        // Speichern in Room
        long newId = localDao.createItem(item);
        item.setId(newId);

        // Speichern in Firestore
        /*firestore.collection("dataitems")
                .document(String.valueOf(item.getId()))
                .set(item);*/

        return item;
    }

    @Override
    public List<DataItem> readAllDataItems() {
        // Zuerst lokale Daten laden
        List<DataItem> localItems = localDao.readAllItems();
        /*localItems.forEach(dataItem ->
                        Log.i(LOG_TAG,"Get DataItem: "+dataItem.getId()+" DataItem Name:"+dataItem.getName())
                );*/
        return localItems;
    }

    @Override
    public DataItem readDataItem(long id) {
        // Firestore oder Room (hybrides Modell möglich)
        return localDao.readItem(id);
    }

    @Override
    public Boolean updateDataItem(DataItem item) {
        // Update in Room
           localDao.updateItem(item);
           // Update in Firestore
           /*firestore.collection("dataitems")
                   .document(String.valueOf(item.getId()))
                   .set(item);*/
        return true;
    }

    @Override
    public Boolean deleteDataItem(DataItem itemToDel) {
        DataItem item = localDao.readItem(itemToDel.getId());

        // Löschen aus Room
        localDao.deleteItem(item);

        // Löschen aus Firestore
       /* firestore.collection("dataitems")
                .document(String.valueOf(item.getId()))
                .delete();*/

        return true;
    }
}
