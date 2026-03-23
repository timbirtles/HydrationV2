package com.example.hydrationv2r.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.adapters.LogDrinkAdapter;
import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.DrinkModel;
import com.example.hydrationv2r.viewmodels.HydrateViewModel;

import java.util.List;

public class LogDrinkFragment extends Fragment {

    private RecyclerView gridRecyclerView;
    private LogDrinkAdapter gridAdapter;
    HydrateViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.log_drink_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridRecyclerView = view.findViewById(R.id.rv_drink_buttons);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        drawAddDrinkButtons();

        viewModel = new ViewModelProvider(requireActivity()).get(HydrateViewModel.class);
    }


    private void drawAddDrinkButtons() {
        if (!isAdded() || getContext() == null) return;

        DatabaseHelper db = DatabaseHelper.getInstance(getContext());

        // Fetch all drink types defined in the drinks table
        List<DrinkModel> drinkList = db.getActiveDrinks();

        if (gridAdapter == null) {
            // Create new LogDrinkAdapter to dynamically populate the recycler view
            gridAdapter = new LogDrinkAdapter(drinkList, new LogDrinkAdapter.OnDrinkClickListener() {
                @Override
                public void onDrinkClick(DrinkModel drink) {
                    viewModel.addDrink(drink.id);
                }

                @Override
                public void onDrinkLongClick(DrinkModel drink) {
                    //TODO: Add long click abilities?
                    // - Add custom amount of drink type?
                    // - Re-order the log drink buttons?

                }
            });
            gridRecyclerView.setAdapter(gridAdapter);
        } else {
            gridAdapter.setDrinks(drinkList);
        }
    }
}
