package my.edu.utar.grandarchivecompanion;

// ... imports
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // --- CHANGE 1: Improve Encapsulation ---
    private boolean isFullScreen = false;

    // Getter remains the same
    public boolean isFullScreen() {
        return isFullScreen;
    }

    private BottomNavigationView bottomNav;

    // Fragment instances
    private final Fragment cardsFragment = new CardsFragment();
    private final Fragment counterFragment = new CounterFragment();
    private final Fragment rulesFragment = new RulesFragment();


    private Fragment activeFragment = counterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Corrected fragment transaction
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, rulesFragment, "rules").hide(rulesFragment)
                    // FIX: Hide the cardsFragment itself, not the counterFragment
                    .add(R.id.fragment_container, cardsFragment, "cards").hide(cardsFragment)
                    // This line correctly adds and shows the counterFragment by default
                    .add(R.id.fragment_container, counterFragment, "counter")
                    .commit();

            // This line is correct and syncs the UI
            bottomNav.setSelectedItemId(R.id.nav_counter);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_cards) {
                selected = cardsFragment;
            }  // Cards option is temporary removed until it's ready
            if (item.getItemId() == R.id.nav_counter) {
                selected = counterFragment;
            } else if (item.getItemId() == R.id.nav_rules) {
                selected = rulesFragment;
            }

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(selected)
                        .commit();
                activeFragment = selected;
            }
            return true;
        });

        // Handle back button to exit full screen mode (this part remains the same)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullScreen()) {
                    exitFullScreenMode();
                } else {
                    setEnabled(false);
                    MainActivity.super.onBackPressed();
                }
            }
        });
    }

    public void enterFullScreenMode() {
        isFullScreen = true;
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreenMode() {
        isFullScreen = false;
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }
}