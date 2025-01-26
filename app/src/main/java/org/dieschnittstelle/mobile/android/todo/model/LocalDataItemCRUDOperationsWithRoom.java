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
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.google.firebase.firestore.FirebaseFirestore;

import org.dieschnittstelle.mobile.android.todo.util.DateConverter;
import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class LocalDataItemCRUDOperationsWithRoom implements IDataItemCRUDOperations {
    public static String LOG_TAG = "DATA_ITEMS";
    private final SQLiteDataItemCRUDOperations localDao;
    private FirebaseFirestore firestore;
    private DataItemDatabase db;
    public LocalDataItemCRUDOperationsWithRoom(Context context) {
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

    @Override
    public void syncDataItems(OverviewViewModel viewModel) {
    }

    @Dao
    public interface SQLiteDataItemCRUDOperations {
        @Insert
        long createItem(DataItem item);

        @Update
        void updateItem(DataItem item);

        @Delete
        void deleteItem(DataItem item);

        @Query("SELECT * FROM dataitem")
        List<DataItem> readAllItems();

        @Query("SELECT * FROM dataitem WHERE id=(:id)")
        DataItem readItem(long id);

    }

    public static final String ARRAY_ELEMENT_SEPARATOR = ";;";

    public static class ArrayListConverters{
        // when writing to local db: [1,2,3] -> "1;;2;;3"
        @TypeConverter
        public static String toString(ArrayList<String> valuesForDB) {
            if(valuesForDB == null) return "";
            return valuesForDB.stream().collect(Collectors.joining(ARRAY_ELEMENT_SEPARATOR));
        }

        //when reading from local db: "1;;2;;3" -> [1,2,3]
        @TypeConverter
        public static ArrayList<String> fromString(String valuesFromDB) {
            if(valuesFromDB == null || valuesFromDB.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(List.of(valuesFromDB.split(ARRAY_ELEMENT_SEPARATOR)).stream()
                    .map(value -> value.trim()).collect(Collectors.toList()));
        }
    }

    @Database(entities = DataItem.class, version = 11)
    @TypeConverters({DateConverter.class}) // Converter hinzufügen
    public abstract static class DataItemDatabase extends RoomDatabase {

        public abstract SQLiteDataItemCRUDOperations getDao();
    }
}
