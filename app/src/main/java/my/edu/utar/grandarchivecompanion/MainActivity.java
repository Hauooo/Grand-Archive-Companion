package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
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
    private WindowInsetsControllerCompat insetsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullScreen) {
            updateSystemBars();
        }
    }

    // --- Fullscreen methods are now correct since they use 'binding.bottomNavigation' ---

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void enterFullScreenMode() {
        isFullScreen = true;
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Find the current fragment and tell it to update its UI
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).setBottomNavVisibility(false);
        }

        updateSystemBars();
    }

    public void exitFullScreenMode() {
        isFullScreen = false;
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // Find the current fragment and tell it to update its UI
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).setBottomNavVisibility(true);
        }

        updateSystemBars();
    }

    private void updateSystemBars() {
        if (isFullScreen) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }
}