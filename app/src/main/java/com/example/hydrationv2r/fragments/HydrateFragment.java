package com.example.hydrationv2r.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.viewmodels.HydrateViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HydrateFragment extends Fragment {

    private CircularProgressIndicator progressIndicator;
    private TextView hydration_value, textPercent, dateTitle, goalText;

    HydrateViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hydrate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressIndicator = view.findViewById(R.id.progress_circular);
        hydration_value = view.findViewById(R.id.tv_hydration_amount);
        textPercent = view.findViewById(R.id.tv_text_percent);
        dateTitle = view.findViewById(R.id.tv_date_text);
        goalText = view.findViewById(R.id.tv_goal_amount);

        dateTitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        changeChildFragment();

        viewModel = new ViewModelProvider(requireActivity()).get(HydrateViewModel.class);

        viewModel.getTodayTotal().observe(getViewLifecycleOwner(), total -> {
            hydration_value.setText(total + "ml");

            // Get user-defined goal
            SharedPreferences preferences = getActivity().getSharedPreferences("hydration_preferences", MODE_PRIVATE);
            int goal = preferences.getInt("goal", 2500);
            String goalFormatted = new DecimalFormat("0.##").format(goal / 1000f);

            // Calculate hdyration progress percentage
            int progress = (int) ((total/ (float) goal) * 100);
            if (progress > 100) progress = 100;

            // Set UI progress indicators
            progressIndicator.setProgress(progress, true);
            textPercent.setText(progress + "% complete");
            goalText.setText("of " + goalFormatted + " goal");
        });

        viewModel.refreshTodayTotal();
    }

    public void onResume() {
        super.onResume();
        dateTitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        viewModel.refreshTodayTotal();
    }

    public void changeChildFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new LogDrinkFragment())
                .addToBackStack(null)
                .commit();
    }
}