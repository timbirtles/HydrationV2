package com.example.hydrationv2r.viewmodels;

import android.app.Application;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.DrinkModel;

import java.time.Instant;

public class HydrateViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> todayTotal = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> goalReachedEvent = new MutableLiveData<>();
    DatabaseHelper db;
    private int lastSeenTotal = -1;

    public HydrateViewModel(Application application) {
        super(application);
        db = DatabaseHelper.getInstance(application);
    }

    /**
     * Records a hydration event to the database and refreshes the daily total
     * @param drinkId The unique ID of the drink type being logged
     */
    public int addDrink(int drinkId) {
        try {
            DrinkModel drink = db.getDrinkById(drinkId);
            int count = db.getTodayDrinkCounts().getOrDefault(drinkId, 0);
            if (drink != null && count < 99) {
                long timestamp = Instant.now().toEpochMilli();
                db.logDrink(timestamp, drinkId, drink.ml);
                refreshTodayTotal();
                return 0;
            }
            return 2;
        } catch (Exception e) {
            return 1;
        }
    }

    public LiveData<Integer> getTodayTotal() {
        return todayTotal;
    }

    public MutableLiveData<Boolean> getGoalReachedEvent() {
        return goalReachedEvent;
    }

    public void consumeGoalEvent() {
        goalReachedEvent.setValue(false);
    }

    public void refreshTodayTotal() {
        int total = db.getTodayHydration();
        todayTotal.setValue(total);
    }

    public void checkGoal(int currentTotal, int goal) {

        // Set to current total on load
        if (lastSeenTotal == -1) {
            lastSeenTotal = currentTotal;
            return;
        }

        if (lastSeenTotal < goal && currentTotal >= goal) {
            goalReachedEvent.setValue(true);
        }
        lastSeenTotal = currentTotal;
    }
}