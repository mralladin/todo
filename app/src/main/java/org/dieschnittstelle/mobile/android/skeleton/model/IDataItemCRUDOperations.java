package org.dieschnittstelle.mobile.android.skeleton.model;

import java.util.List;

public interface IDataItemCRUDOperations
{

    public DataItem createDataItem(DataItem item);

    public List<DataItem> readAllDataItems();

    public DataItem readDataItem(long id);

    public DataItem updateDataItem(DataItem item);



}
