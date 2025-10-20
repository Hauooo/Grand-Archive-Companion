package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import my.edu.utar.grandarchivecompanion.databinding.ActivityMainBinding;
import my.edu.utar.grandarchivecompanion.ui.HomeFragment;
import my.edu.utar.grandarchivecompanion.ui.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private boolean isFullScreen = false;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Find the NavController from the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Handle back button presses correctly
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullScreen()) {
                    exitFullScreenMode();
                } else if (!navController.navigateUp()) {
                    // If NavController can't go back, exit the app
                    finish();
                }
            }
        });
    }
    // --- Fullscreen methods are now correct since they use 'binding.bottomNavigation' ---

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void enterFullScreenMode() {
        isFullScreen = true;

        // Find the current fragment and tell it to update its UI
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).setBottomNavVisibility(false);
        }

        // Hide the system bars (status bar, etc.)
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreenMode() {
        isFullScreen = false;

        // Find the current fragment and tell it to update its UI
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).setBottomNavVisibility(true);
        }

        // Show the system bars
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }
}