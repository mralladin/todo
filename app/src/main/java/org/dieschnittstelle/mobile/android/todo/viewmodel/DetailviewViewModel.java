package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.util.Log;
import android.view.inputmethod.EditorInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;

import java.util.ArrayList;
import java.util.List;

public class DetailviewViewModel extends ViewModel {

    private MutableLiveData<Boolean> itemValidOnSaveOrDelete = new MutableLiveData<>(false);


    private static String LOG_TAG = DetailviewViewModel.class.getName();

    private MutableLiveData<String> errorStatus = new MutableLiveData<>(null);

    private DataItem item;

    public DetailviewViewModel(){
        Log.i(LOG_TAG,"contructor called");
    }

    public DataItem getItem() {
        return item;
    }
    //1845:

    public void saveItem(){
        Log.i(LOG_TAG,"save item");
        itemValidOnSaveOrDelete.setValue(true);

    }

    public void addContactName(String contactId) {
        if (contactId != null && !contactId.isEmpty()) {
            // Aktuelle Liste abrufen
            List<String> currentList = contactIds.getValue();
            if (currentList != null && !currentList.contains(contactId)) {
                currentList.add(contactId); // Kontakt hinzufügen
                contactIds.setValue(currentList); // Liste aktualisieren
                Log.i(LOG_TAG, "Contact Name added: " + contactId);
            } else {
                Log.i(LOG_TAG, "Contact Name already exists or currentList is null");
            }
        }
    }

    private final MutableLiveData<List<String>> contactIds = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<String>> getContactIds() {
        return contactIds;
    }

    public void setItem(DataItem item) {
        this.item=item;
    }

    public MutableLiveData<Boolean> getValidOnSave() {
        return this.itemValidOnSaveOrDelete;
    }

    public boolean checkFieldInputValid(int keyId){
        Log.i(LOG_TAG,"checkFieldInputValid");
    if(keyId == EditorInfo.IME_ACTION_NEXT || keyId == EditorInfo.IME_ACTION_DONE){
        String itemName = item.getName();
        if(itemName.length() < 4){
            this.errorStatus.setValue("Name too short!");;
            return true;
        }else{
            this.errorStatus.setValue("");;
        }
    }

        return false;
    }

    public boolean onNameFieldInputChanged(){
        this.errorStatus.setValue(null);
        return false;
    }

    public MutableLiveData<String> getErrorStatus() {
        return errorStatus;
    }
}
