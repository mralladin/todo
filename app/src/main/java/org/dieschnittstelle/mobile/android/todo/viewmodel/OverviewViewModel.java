package org.dieschnittstelle.mobile.android.todo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.dieschnittstelle.mobile.android.todo.model.DataItem;
import org.dieschnittstelle.mobile.android.todo.model.IDataItemCRUDOperations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends ViewModel {

    private static final String LOG_TAG = "OverviewActivityViewModel";
    private static final Comparator<DataItem> SORT_BY_CHECKED_AND_NAME = Comparator.comparing(DataItem::isChecked).thenComparing(DataItem::getName);

    private static final Comparator<DataItem> SORT_BY_CHECKED_AND_PRIORITY = Comparator.comparing(DataItem::isChecked).reversed().thenComparing(DataItem::getPrio).reversed();
    private static final Comparator<DataItem> SORT_BY_CHECKED_AND_DATE = Comparator.comparing(DataItem::isChecked).reversed().thenComparing(DataItem::getTbdDate).reversed();
    private static final Comparator<DataItem> SORT_BY_CHECKED_AND_PRIORITY_REVERSE = Comparator.comparing(DataItem::isChecked).reversed().thenComparing(DataItem::getPrio);
    private static final Comparator<DataItem> SORT_BY_CHECKED_AND_DATE_REVERSE = Comparator.comparing(DataItem::isChecked).reversed().thenComparing(DataItem::getTbdDate);



    public static final String FILTER_VALUE_PRIORITY="priority";
    public static final String FILTER_VALUE_DATE="date";

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();
    private final List<DataItem> dataItems = new ArrayList<>();
    private IDataItemCRUDOperations crudOperations;
    private Comparator<DataItem> currentSorter = SORT_BY_CHECKED_AND_DATE;
    private boolean initialised;

    public OverviewViewModel() {
        Log.i(LOG_TAG, "contructer called");
    }

    public MutableLiveData<ProcessingState> getProcessingState() {
        return this.processingState;
    }

    public void addExampleData() {
        //Add new DataItems to crud operations with name a - q and prio 0,1,2,3 and
        List<DataItem> dataItems = new ArrayList<>();
        long currentTime = System.currentTimeMillis(); // Aktuelle Zeit in Millisekunden

        dataItems.add(createDataItem("1. Wäsche waschen", "Die Wäsche muss dringend gewaschen werden. Es gibt viele Kleidungsstücke zu sortieren.", currentTime, 60));
        dataItems.add(createDataItem("2. Einkaufen gehen", "Der Kühlschrank ist leer. Bitte Obst, Gemüse und Milchprodukte kaufen.", currentTime, 120));
        dataItems.add(createDataItem("3. Auto waschen", "Das Auto ist sehr schmutzig. Eine gründliche Reinigung ist notwendig.", currentTime, 180));
        dataItems.add(createDataItem("4. Bücher zurückgeben", "Die ausgeliehenen Bücher müssen zur Bibliothek zurückgebracht werden. Es gibt Fristgebühren zu vermeiden.", currentTime, 240));
        dataItems.add(createDataItem("5. Spaziergang machen", "Ein entspannter Spaziergang im Park ist geplant. Die frische Luft tut gut.", currentTime, 300));
        dataItems.add(createDataItem("6. Rechnungen bezahlen", "Alle offenen Rechnungen müssen beglichen werden. Besonders die Stromrechnung ist wichtig.", currentTime, 360));
        dataItems.add(createDataItem("7. Freunde treffen", "Ein Abend mit Freunden ist organisiert. Treffpunkt ist das Lieblingscafé.", currentTime, 420));
        dataItems.add(createDataItem("8. Arzttermin wahrnehmen", "Ein Kontrolltermin beim Hausarzt steht an. Der Termin ist wichtig für die Gesundheit.", currentTime, 480));
        dataItems.add(createDataItem("9. Zimmer aufräumen", "Das Zimmer ist unordentlich. Eine Grundreinigung und Sortierung sind notwendig.", currentTime, 540));
        dataItems.add(createDataItem("10. Präsentation vorbereiten", "Die Präsentation für das Meeting am Montag muss erstellt werden. PowerPoint-Folien und ein Skript sind notwendig.", currentTime, 600));
        dataItems.add(createDataItem("11. Fahrrad reparieren", "Das Fahrrad hat einen Platten. Reparatur und Wartung sind nötig.", currentTime, 660));
        dataItems.add(createDataItem("12. Pflanzen gießen", "Die Zimmerpflanzen benötigen Wasser. Besonders die Kakteen dürfen nicht vergessen werden.", currentTime, 720));
        dataItems.add(createDataItem("13. E-Mails schreiben", "Es gibt einige wichtige E-Mails zu beantworten. Besonders die an den Chef hat Priorität.", currentTime, 780));
        dataItems.add(createDataItem("14. Paket abholen", "Ein bestelltes Paket liegt zur Abholung bereit. Es enthält dringend benötigte Artikel.", currentTime, 840));
        dataItems.add(createDataItem("15. Lernen für die Prüfung", "Die Prüfung steht kurz bevor. Intensives Lernen ist erforderlich.", currentTime, 900));
        dataItems.add(createDataItem("16. Kochen für die Familie", "Ein gesundes Abendessen muss gekocht werden. Es sind viele hungrige Mäuler zu füttern.", currentTime, 960));
        dataItems.add(createDataItem("17. Fotos sortieren", "Die digitalen Fotos müssen aufgeräumt und in Ordnern organisiert werden.", currentTime, 1020));
        dataItems.add(createDataItem("18. Reise planen", "Die nächste Reise muss geplant werden. Ziel, Unterkunft und Aktivitäten sind zu organisieren.", currentTime, 1080));
        dataItems.add(createDataItem("19. Zahnarzttermin buchen", "Ein Termin beim Zahnarzt ist überfällig. Besonders wichtig ist die jährliche Kontrolle.", currentTime, 1140));
        dataItems.add(createDataItem("20. Yoga machen", "Eine Yoga-Sitzung ist geplant, um den Körper zu entspannen. Es tut gut, sich zu dehnen.", currentTime, 1200));
        dataItems.add(createDataItem("21. Zeitschriften durchsehen", "Die alten Zeitschriften sollen durchgesehen und aussortiert werden. Platz schaffen ist das Ziel.", currentTime, 1260));
        dataItems.add(createDataItem("22. Rezept ausprobieren", "Ein neues Rezept wartet darauf, ausprobiert zu werden. Es wird ein kulinarisches Abenteuer.", currentTime, 1320));
        dataItems.add(createDataItem("23. Zimmerpflanzen umtopfen", "Die Pflanzen benötigen neue Töpfe und frische Erde. Es ist Frühjahrsputz für die Pflanzen.", currentTime, 1380));

        // Output der generierten Daten

        processingState.setValue(ProcessingState.RUNNING_LONG);
        for (DataItem item : dataItems) {
            getDataItems().add(item);
            sortItems("");
        }
        new Thread(() -> {
            for (DataItem item : dataItems) {
                this.crudOperations.createDataItem(item);
            }
            processingState.postValue(ProcessingState.DONE);
        }).start();


    }

    private static DataItem createDataItem(String shortName, String description, long currentTime, int minutesOffset) {
        DataItem item = new DataItem(shortName, getRandomPrio(), getCurrentTimePlusMinutes(currentTime, minutesOffset), currentTime);
        item.setDescription(description);
        return item;
    }

    private static long getCurrentTimePlusMinutes(long currentTime, int minutes) {
        return currentTime + (minutes * 60 * 1000); // Minuten in Millisekunden umrechnen
    }

    //get Random int beetween 0 and 3 0 and 3 included
    public static int getRandomPrio() {
        return (int) (Math.random() * 4);
    }

    //Method which returns long value of current date plus added minutes in long
    public long getCurrentTimePlusMinutes(int minutes) {
        Calendar calender = Calendar.getInstance();
        Long currentTime = calender.getTimeInMillis();
        calender.setTimeInMillis(currentTime);
        calender.add(Calendar.MINUTE, minutes);
        return calender.getTimeInMillis();
    }

    public List<DataItem> getDataItems() {
        return dataItems;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public void setCrudOperations(IDataItemCRUDOperations crudOperations) {
        this.crudOperations = crudOperations;
    }

    public void createDataItem(DataItem itemToBeCreated) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            DataItem createdItem = this.crudOperations.createDataItem(itemToBeCreated);
            getDataItems().add(createdItem);
            processingState.postValue(ProcessingState.DONE);
        }).start();
        sortItems("");
    }

    //get Comparater
    private Comparator<DataItem> getCurrentSorter() {
        return currentSorter;
    }

    public void setSorter(Comparator<DataItem> sorter) {
        this.currentSorter = sorter;
    }

    public void sortItems(String method) {
        if (method.equals(FILTER_VALUE_PRIORITY)) {
            if(getCurrentSorter()==SORT_BY_CHECKED_AND_PRIORITY){
                setSorter(SORT_BY_CHECKED_AND_PRIORITY_REVERSE);
            }else{
                setSorter(SORT_BY_CHECKED_AND_PRIORITY);
            }
        } else if (method.equals(FILTER_VALUE_DATE)) {
            if(getCurrentSorter()==SORT_BY_CHECKED_AND_DATE){
                setSorter(SORT_BY_CHECKED_AND_DATE_REVERSE);
            }else{
                setSorter(SORT_BY_CHECKED_AND_DATE);
            }
        }

        processingState.setValue(ProcessingState.RUNNING);
        getDataItems().sort(this.currentSorter);
        processingState.postValue(ProcessingState.DONE);
    }

    public void readAllDataItems() {
        processingState.setValue(ProcessingState.RUNNING_LONG);

        new Thread(() -> {
            this.crudOperations.syncDataItems(this);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (this.crudOperations != null) {
                List<DataItem> items = this.crudOperations.readAllDataItems();
                getDataItems().addAll(items);
                getDataItems().sort(this.currentSorter);
            }
            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void deleteAllLocalTodos(IDataItemCRUDOperations localOperationsparam) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                List<DataItem> localTodos = localOperationsparam.readAllDataItems();
                for (DataItem todo : localTodos) {
                    localOperationsparam.deleteDataItem(todo);
                    getDataItems().remove(todo);
                }
                Log.i("TODO_APP", "Alle lokalen Todos gelöscht.");
            } catch (Exception e) {
                Log.e("TODO_APP", "Fehler beim Löschen lokaler Todos: " + e.getMessage(), e);
            }
            processingState.postValue(ProcessingState.DONE);
        }).start();
    }

    public void deleteAllRemoteTodos(IDataItemCRUDOperations remoteOperationsparam) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        new Thread(() -> {
            try {
                List<DataItem> remoteTodos = remoteOperationsparam.readAllDataItems();
                for (DataItem todo : remoteTodos) {
                    remoteOperationsparam.deleteDataItem(todo);
                }

                Log.i("TODO_APP", "Alle Remote-Todos gelöscht.");
            } catch (Exception e) {
                Log.e("TODO_APP", "Fehler beim Löschen von Remote-Todos: " + e.getMessage(), e);
            }
            processingState.postValue(ProcessingState.DONE);

        }).start();
    }

    public void updateDataItem(DataItem itemFromDetailViewToBeModifiedInList) {
        processingState.setValue(ProcessingState.RUNNING_LONG);
        executorService.execute(() -> {
            boolean updated = this.crudOperations.updateDataItem(itemFromDetailViewToBeModifiedInList);
            Log.e("TestLog5", "updated: " + updated);
            if (updated) {
                //showMessage(getString(R.string.on_result_from_detailview_msg) + itemFromDetailViewToBeModifiedInList.getName());
                int itemPosition = getDataItems().indexOf(itemFromDetailViewToBeModifiedInList);

                DataItem existingItemInList = getDataItems().get(itemPosition);
                existingItemInList.setName(itemFromDetailViewToBeModifiedInList.getName());
                existingItemInList.setDescription(itemFromDetailViewToBeModifiedInList.getDescription());
                existingItemInList.setPrio(itemFromDetailViewToBeModifiedInList.getPrio());
                existingItemInList.setTbdDate(itemFromDetailViewToBeModifiedInList.getTbdDate());
                existingItemInList.setChecked(itemFromDetailViewToBeModifiedInList.isChecked());
                existingItemInList.setContactIds(itemFromDetailViewToBeModifiedInList.getContactIds());

                processingState.postValue(ProcessingState.DONE);
            }
        });
        sortItems("");


    }


    public enum ProcessingState {RUNNING, DONE, RUNNING_LONG}

}
