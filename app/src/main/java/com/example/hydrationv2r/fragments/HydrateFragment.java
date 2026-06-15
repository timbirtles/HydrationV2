package com.example.hydrationv2r.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.viewmodels.HydrateViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
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
        setupToggleView();

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

        MaterialButton menuButton = view.findViewById(R.id.button_show_menu);

        // Set up click listener to display the popup
        menuButton.setOnClickListener(v -> showSettingsMenu(v));
    }

    private void showSettingsMenu(View anchorView) {
        // Create PopupMenu
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);

        // Inflate layout XML
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());

        // Handle item selections
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.set_drinks) {
                // Open drinks configuration fragment
                Fragment manageDrinksFragment = new UpdateDrinkInfoFragment();

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, manageDrinksFragment)
                        .addToBackStack(null) // Allows the user to use the system back button to return home
                        .commit();
                return true;
            }

            // TODO: Handle other menu item IDs
            return false;
        });

        // Display the popup over the UI
        popup.show();
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

    public void setupToggleView() {
        MaterialButtonToggleGroup toggleGroup = getView().findViewById(R.id.toggleGroup);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.button_log_drinks) {
                    changeChildFragment(0);
                } else if (checkedId == R.id.button_history) {
                    changeChildFragment(1);
                }
            }
        });
        // Check default option
        toggleGroup.check(R.id.button_log_drinks);
    }

    // Change between LogDrinkFragment and TodayHistoryFragment
    public void changeChildFragment(int choice) {

        Fragment chosen = choice == 0 ? new LogDrinkFragment() : new TodayHistoryFragment();

        boolean slidingRight = choice != 0;
        int enter = slidingRight ? R.anim.slide_in_right : R.anim.slide_in_left;
        int exit = slidingRight ? R.anim.slide_out_left : R.anim.slide_out_right;

        int popEnter = slidingRight ? R.anim.slide_in_left : R.anim.slide_in_right;
        int popExit = slidingRight ? R.anim.slide_out_right : R.anim.slide_out_left;

        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(R.id.fragment_container_view, chosen)
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