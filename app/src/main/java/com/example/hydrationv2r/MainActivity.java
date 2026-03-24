package com.example.hydrationv2r;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.hydrationv2r.databinding.ActivityMainBinding;
import com.example.hydrationv2r.fragments.HydrateFragment;
import com.example.hydrationv2r.viewmodels.HydrateViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNav = binding.bottomNavigation;

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HydrateFragment();
            }

            //TODO Add history functionality
            else if (itemId == R.id.nav_history) {}

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        HydrateViewModel viewModel = new ViewModelProvider(this).get(HydrateViewModel.class);
        viewModel.getGoalReachedEvent().observe(this, reached -> {
            if (reached != null && reached) {
                wave(1.0f);
                viewModel.consumeGoalEvent();
            }
        });
    }

    private ObjectAnimator currentAnimator;

    public void wave(float amount) {
        WaveView waveView = findViewById(R.id.wave_view);
        float targetHeight = amount;

        // Cancel existing animation
        if (currentAnimator != null) {
            currentAnimator.removeAllListeners();
            currentAnimator.cancel();
        }

        // Animate from current progress to target
        currentAnimator = ObjectAnimator.ofFloat(waveView, "progress", waveView.getProgress(), targetHeight);
        currentAnimator.setDuration(1000);
        currentAnimator.setInterpolator(new DecelerateInterpolator());

        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (targetHeight > 0) {
                    drain(waveView);
                }
            }
        });

        currentAnimator.start();
    }

    private void drain(WaveView waveView) {
        // Smoothly return to 0
        currentAnimator = ObjectAnimator.ofFloat(waveView, "progress", waveView.getProgress(), 0f);
        currentAnimator.setDuration(2000);
        currentAnimator.setStartDelay(1000); // Wait at the top briefly
        currentAnimator.start();
    }
}
