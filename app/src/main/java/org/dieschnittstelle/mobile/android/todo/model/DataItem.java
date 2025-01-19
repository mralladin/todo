package org.dieschnittstelle.mobile.android.todo.model;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.dieschnittstelle.mobile.android.todo.util.DateConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@Entity
public class DataItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String firebaseId;
    private String name;
    private String description;
    private boolean checked;
    private String useridcreated;
    private String useridassigned;
    private int prio = 0;
    @TypeConverters(DateConverter.class) // Verwende einen Converter für Room
    //Wann das DataItem fertig ist
    private Long tbdDate;
    //Wann das DataItem gestartet wurde
    private Long startTime;
    private Boolean timeOver;
    @Ignore
    public DataItem(String name) {
        this.name = name;
    }

    //For creating example DataITems
    public DataItem(String name, int prio, Long tbdDate, Long startTime) {
        this.name = name;
        this.prio = prio;
        this.tbdDate = tbdDate;
        this.startTime = startTime;
    }

    // when writing to local db: [1,2,3] -> "1;; 2;; 3"
    //when reading from local db: "1;; 2;; 3" -> [1,2,3]
    @TypeConverters(LocalDataItemCRUDOperationsWithRoom.ArrayListConverters.class)
    private ArrayList<String> contactIds;

    public DataItem() {
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getTbdDate() {
        return tbdDate;
    }

    public void setTbdDate(Long tbdDate) {
        this.tbdDate = tbdDate;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public String getUseridcreated() {
        return useridcreated;
    }

    public void setUseridcreated(String useridcreated) {
        this.useridcreated = useridcreated;
    }

    public String getUseridassigned() {
        return useridassigned;
    }

    public void setUseridassigned(String useridassigned) {
        this.useridassigned = useridassigned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem dataItem = (DataItem) o;
        return id == dataItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Boolean getTimeOver() {
        return timeOver;
    }

    public void setTimeOver(Boolean timeOver) {
        this.timeOver = timeOver;
    }

    public ArrayList<String> getContactIds() {
        if(contactIds == null){
            contactIds = new ArrayList<>();
        }
        return contactIds;
    }

    public void setContactIds(ArrayList<String> contactIds) {
        this.contactIds = contactIds;
    }


    @Override
    public String toString() {
        return "DataItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", checked=" + checked +
                ", contactId='" + contactIds + '\'' +
                ", prio=" + prio;

    }
}
