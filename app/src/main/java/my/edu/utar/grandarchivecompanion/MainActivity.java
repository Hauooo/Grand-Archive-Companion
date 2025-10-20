package my.edu.utar.grandarchivecompanion;

// ... imports
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController; // Import corrected
import androidx.navigation.fragment.NavHostFragment; // Import corrected
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

// DELETE these imports. The NavController will handle your fragments.
// import my.edu.utar.grandarchivecompanion.ui.counter.CounterFragment;
// import my.edu.utar.grandarchivecompanion.ui.cards.CardsFragment;
// import my.edu.utar.grandarchivecompanion.ui.rules.RulesFragment;
// import androidx.fragment.app.Fragment;

// Import this
import com.google.android.material.bottomnavigation.BottomNavigationView;

import my.edu.utar.grandarchivecompanion.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private boolean isFullScreen = false;
    private ActivityMainBinding binding;
    private NavController navController; // This is needed for the back button

    // Getter remains the same
    public boolean isFullScreen() {
        return isFullScreen;
    }

    // DELETE all the manual fragment variables:
    // private BottomNavigationView bottomNav;
    // private final Fragment cardsFragment = new CardsFragment();
    // ...etc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- FIX 1: Correct View Binding Setup ---
        // You only need to set the content view ONCE using the binding.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // DELETE the other setContentView(R.layout.activity_main)

        // --- FIX 2: Use Navigation Component Correctly ---

        // DELETE all the manual FragmentManager code.
        // getSupportFragmentManager().beginTransaction()...

        // Find the NavController
        // IMPORTANT: This assumes your NavHostFragment in activity_main.xml
        // has the ID "nav_host_fragment" as we fixed before.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        this.navController = navHostFragment.getNavController();

        // Setup the AppBar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_cards, R.id.navigation_counter, R.id.navigation_rules)
                .build();

        // Setup the Bottom Navigation
        // Use the 'binding' object, not findViewById
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);


        // --- FIX 3: Correct Back Button Logic ---
        // Put the navController logic back in!
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullScreen()) {
                    exitFullScreenMode();
                } else {
                    // This is CRITICAL for navigation to work.
                    // It allows the NavController to handle back presses (e.g., from a detail screen).
                    if (!navController.navigateUp()) {
                        // If NavController can't go back, then exit the app.
                        setEnabled(false);
                        MainActivity.super.onBackPressed();
                    }
                }
            }
        });
    }

    // --- FIX 4: Use View Binding in Fullscreen Methods ---
    public void enterFullScreenMode() {
        isFullScreen = true;
        // Use 'binding.bottomNavigation' instead of 'bottomNav'
        binding.bottomNavigation.animate()
                .translationY(binding.bottomNavigation.getHeight())
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(300)
                .withEndAction(() -> binding.bottomNavigation.setVisibility(View.GONE))
                .start();

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreenMode() {
        isFullScreen = false;
        // Use 'binding.bottomNavigation' instead of 'bottomNav'
        binding.bottomNavigation.setVisibility(View.VISIBLE);
        binding.bottomNavigation.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }
}