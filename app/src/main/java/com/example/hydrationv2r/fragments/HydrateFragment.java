package com.example.hydrationv2r.fragments;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_SHORT;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import java.util.Calendar;

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
            else if (itemId == R.id.add_history) {
                showHistoryInjectionDialog();
            }
            else if (itemId == R.id.reset_today) {
                DatabaseHelper.getInstance(getContext()).resetTodayHydration();
                viewModel.refreshTodayTotal();
                Toast.makeText(getContext(), "Reset today's intake.", LENGTH_SHORT).show();
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


    /**
     * Helper testing function that shows a chain of pickers to inject custom historical
     * data records directly into the SQLite database table.
     */
    public void showHistoryInjectionDialog() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(requireContext(), (dateView, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // Calculate the timestamp in absolute milliseconds
                long injectedEpochMilli = calendar.getTimeInMillis();

                // Build a layout to input IDs and Sizes manually
                android.widget.LinearLayout dialogLayout = new android.widget.LinearLayout(requireContext());
                dialogLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                dialogLayout.setPadding(50, 40, 50, 10);

                final EditText etDrinkId = new EditText(requireContext());
                etDrinkId.setHint("Drink ID Template (e.g. 1=Tea, 2=Water, 3=Coffee)");
                etDrinkId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                dialogLayout.addView(etDrinkId);

                final EditText etAmountMl = new EditText(requireContext());
                etAmountMl.setHint("Amount size to log (mL)");
                etAmountMl.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                dialogLayout.addView(etAmountMl);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Simulate Mock Log Entry")
                        .setMessage("Specify mock structural components to register at: " +
                                java.time.Instant.ofEpochMilli(injectedEpochMilli)
                                        .atZone(java.time.ZoneId.systemDefault()).toString())
                        .setView(dialogLayout)
                        .setPositiveButton("Inject Row", (dialog, which) -> {
                            String idStr = etDrinkId.getText().toString().trim();
                            String amountStr = etAmountMl.getText().toString().trim();

                            if (!idStr.isEmpty() && !amountStr.isEmpty()) {
                                int drinkId = Integer.parseInt(idStr);
                                int amountMl = Integer.parseInt(amountStr);

                                // Write transaction log downstream directly to database
                                DatabaseHelper.getInstance(getContext()).logDrink(injectedEpochMilli, drinkId, amountMl);

                                // Update the livedata views inside the session screen layout
                                viewModel.refreshTodayTotal();

                                Toast.makeText(getContext(), "Injected mock log success!", LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Fields cannot be left blank", LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}