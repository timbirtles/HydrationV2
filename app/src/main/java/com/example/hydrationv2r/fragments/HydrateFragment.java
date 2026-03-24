package com.example.hydrationv2r.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

    private CircularProgressIndicator cpi_progressIndicator;
    private TextView tv_hydrationValue, tv_textPercent, tv_dateTitle, tv_goalText;

    HydrateViewModel viewModel;

    private int lastHydrateTotal = 0;
    private boolean isStarted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hydrate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cpi_progressIndicator = view.findViewById(R.id.progress_circular);
        tv_hydrationValue = view.findViewById(R.id.tv_hydration_amount);
        tv_textPercent = view.findViewById(R.id.tv_text_percent);
        tv_dateTitle = view.findViewById(R.id.tv_date_text);
        tv_goalText = view.findViewById(R.id.tv_goal_amount);

        tv_dateTitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        // Load log drink fragment by default
        changeChildFragment();

        viewModel = new ViewModelProvider(requireActivity()).get(HydrateViewModel.class);

        viewModel.getTodayTotal().observe(getViewLifecycleOwner(), total -> {
            // If this is the first time data is got since fragment creation
            // a 300ms delay gives the UI time to appear on screen before animating
            if (!isStarted) {
                tv_hydrationValue.postDelayed(() -> {
                    animateToLiters(0, total, tv_hydrationValue);
                    lastHydrateTotal = total;
                    isStarted = true;
                }, 100);
            } else {
                animateToLiters(lastHydrateTotal, total, tv_hydrationValue);
                lastHydrateTotal = total;
            }

            // Get user-defined goal
            SharedPreferences preferences = getActivity().getSharedPreferences("hydration_preferences", MODE_PRIVATE);
            int goal = preferences.getInt("goal", 2500);
            String goalFormatted = new DecimalFormat("0.##").format(goal / 1000f);

            // Calculate hdyration progress percentage
            int progress = (int) ((total/ (float) goal) * 100);
            if (progress > 100) progress = 100;

            // Set UI progress indicators
            cpi_progressIndicator.setProgress(progress, true);
            tv_textPercent.setText(progress + "% complete");
            tv_goalText.setText("of " + goalFormatted + " goal");

            viewModel.checkGoal(total, goal);
            // Pulse text and change colour if goal reached
            if (progress == 100) {
                pulseText(tv_hydrationValue);
                tv_hydrationValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            }
        });


        viewModel.refreshTodayTotal();
    }

    public void onResume() {
        super.onResume();
        tv_dateTitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        viewModel.refreshTodayTotal();
    }

    public void changeChildFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new LogDrinkFragment())
                .addToBackStack(null)
                .commit();
    }

    private void animateToLiters(int initial, int end, final TextView textView) {
        ValueAnimator animator = ValueAnimator.ofInt(initial, end);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            float liters = animatedValue / 1000f;
            textView.setText(String.format("%.2fL", liters));
        });
        animator.start();
    }

    private void pulseText(TextView tv) {
        ObjectAnimator.ofFloat(tv, "scaleX", 1f, 1.15f, 1f).setDuration(500).start();
        ObjectAnimator.ofFloat(tv, "scaleY", 1f, 1.15f, 1f).setDuration(500).start();
    }
}