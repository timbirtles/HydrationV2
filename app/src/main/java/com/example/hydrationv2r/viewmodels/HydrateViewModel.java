package com.example.hydrationv2r.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.DrinkModel;

import java.time.Instant;

public class HydrateViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> todayTotal = new MutableLiveData<>(0);
    DatabaseHelper db;

    public HydrateViewModel(Application application) {
        super(application);
        db = DatabaseHelper.getInstance(application);
    }

    /**
     * Records a hydration event to the database and refreshes the daily total
     * @param drinkId The unique ID of the drink type being logged
     */
    public void addDrink(int drinkId) {
        DrinkModel drink = db.getDrinkById(drinkId);
        if (drink != null) {
            long timestamp = Instant.now().toEpochMilli();
            db.logDrink(String.valueOf(timestamp), drinkId, drink.ml);
        }

        refreshTodayTotal();
    }

    public LiveData<Integer> getTodayTotal() {
        return todayTotal;
    }

    public void refreshTodayTotal() {
        int total = db.getTodayHydration();
        todayTotal.setValue(total);
    }
}