package my.edu.utar.grandarchivecompanion.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// --- REFINEMENT 1: Add animation imports ---
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ViewPagerAdapter viewPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use getChildFragmentManager() for adapters inside a fragment.
        // It's crucial for nested fragment management (ViewPager2 uses child fragments).
        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setUserInputEnabled(false); // Disable swipe gestures
        binding.viewPager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

        // Link BottomNavigationView clicks to ViewPager2
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_cards) {
                binding.viewPager.setCurrentItem(0, true);
            } else if (itemId == R.id.navigation_counter) {
                binding.viewPager.setCurrentItem(1, true);
            } else if (itemId == R.id.navigation_rules) {
                binding.viewPager.setCurrentItem(2, true);
            }
            return true;
        });

        // Link ViewPager2 swipes to BottomNavigationView
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomNavigation.setSelectedItemId(R.id.navigation_cards);
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.navigation_counter);
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.navigation_rules);
                        break;
                }
            }
        });
    }

    public void setBottomNavVisibility(boolean visible) {
        // --- BUG FIX 2: Add null check for the binding ---
        if (binding == null) {
            return; // Don't do anything if the view is already destroyed
        }

        if (visible) {
            // Show the BottomNavigationView
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            binding.bottomNavigation.animate()
                    .translationY(0)
                    // Use the imported class
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(300)
                    .start();
        } else {
            // Hide the BottomNavigationView
            binding.bottomNavigation.animate()
                    .translationY(binding.bottomNavigation.getHeight())
                    // Use the imported class
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(300)
                    .withEndAction(() -> binding.bottomNavigation.setVisibility(View.GONE))
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Correctly nullify the binding to prevent memory leaks
    }
}
