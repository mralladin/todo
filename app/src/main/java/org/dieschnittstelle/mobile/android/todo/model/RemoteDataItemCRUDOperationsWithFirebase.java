package org.dieschnittstelle.mobile.android.todo.model;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoteDataItemCRUDOperationsWithFirebase implements IDataItemCRUDOperations {

    private final CollectionReference todosCollection;

    public RemoteDataItemCRUDOperationsWithFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        todosCollection = db.collection("todos");
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        CompletableFuture<DataItem> future = new CompletableFuture<>();
        todosCollection.add(item)
                .addOnSuccessListener(documentReference -> {
                    item.setFirebaseId(documentReference.getId());
                    future.complete(item);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future.join();
    }

    @Override
    public List<DataItem> readAllDataItems() {
        CompletableFuture<List<DataItem>> future = new CompletableFuture<>();
        todosCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DataItem> items = new ArrayList<>();
                    queryDocumentSnapshots.forEach(documentSnapshot -> {
                        DataItem item = documentSnapshot.toObject(DataItem.class);
                        item.setFirebaseId(documentSnapshot.getId());
                        items.add(item);
                    });
                    future.complete(items);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future.join();
    }

    @Override
    public DataItem readDataItem(long id) {
        CompletableFuture<DataItem> future = new CompletableFuture<>();
        todosCollection.document(String.valueOf(id)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DataItem item = documentSnapshot.toObject(DataItem.class);
                        item.setFirebaseId(documentSnapshot.getId());
                        future.complete(item);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future.join();
    }

    @Override
    public Boolean updateDataItem(DataItem item) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        todosCollection.document(item.getFirebaseId())
                .set(item)
                .addOnSuccessListener(unused -> future.complete(true))
                .addOnFailureListener(e -> future.complete(false));
        return future.join();
    }

    @Override
    public Boolean deleteDataItem(DataItem item) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        todosCollection.document(item.getFirebaseId()).delete()
                .addOnSuccessListener(unused -> future.complete(true))
                .addOnFailureListener(e -> future.complete(false));
        return future.join();
    }
}