package org.dieschnittstelle.mobile.android.todo.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.dieschnittstelle.mobile.android.todo.util.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
public class DataItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String description;
    private boolean checked;
    private String useridcreated;
    private String useridassigned;
    private int prio;
    @TypeConverters(DateConverter.class) // Verwende einen Converter für Room
    //Wann das DataItem fertig ist
    private Long tbdDate;
    //Wann das DataItem gestartet wurde
    private Long startTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Ignore
    public DataItem(String name) {
        this.name = name;
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

    public DataItem() {
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

    @NonNull
    @Override
    public String toString() {
        return getName();
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
}
