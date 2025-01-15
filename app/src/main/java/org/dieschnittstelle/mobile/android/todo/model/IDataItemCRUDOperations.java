package org.dieschnittstelle.mobile.android.todo.model;

import org.dieschnittstelle.mobile.android.todo.viewmodel.OverviewViewModel;

import java.util.List;

public interface IDataItemCRUDOperations
{

    public DataItem createDataItem(DataItem item);

    public List<DataItem> readAllDataItems();

    public DataItem readDataItem(long id);

    public Boolean updateDataItem(DataItem item);

    public Boolean deleteDataItem(DataItem item);

    public Boolean syncDataItems(OverviewViewModel model);
}
