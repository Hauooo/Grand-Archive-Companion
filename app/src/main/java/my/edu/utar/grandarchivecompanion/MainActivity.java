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
    //private final Fragment settingsFragment = new SettingsFragment();


    private Fragment activeFragment = cardsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Corrected fragment transaction
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    // 1. Add rulesFragment, tag it "rules", and hide it.
                    .add(R.id.fragment_container, rulesFragment, "rules").hide(rulesFragment)

                    // 2. Add counterFragment, tag it "counter", and hide it.
                    .add(R.id.fragment_container, counterFragment, "counter").hide(counterFragment)
                    //.add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)

                    // 3. Add cardsFragment, tag it "cards". DO NOT hide it, as it's the default.
                    .add(R.id.fragment_container, cardsFragment, "cards")
                    .commit();

            // Sync the BottomNavigationView UI to show the "Cards" icon as selected
            bottomNav.setSelectedItemId(R.id.nav_cards);
        }


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int selectedId = 0;
            int activeId = 0;
            if (item.getItemId() == R.id.nav_cards) {
                selected = cardsFragment;
                selectedId = 1;
            }  // Cards option is temporary removed until it's ready
            if (item.getItemId() == R.id.nav_counter) {
                selected = counterFragment;
                selectedId = 2;
            } else if (item.getItemId() == R.id.nav_rules) {
                selected = rulesFragment;
                selectedId = 3;
//            } else if (item.getItemId() == R.id.nav_settings) {
//                selected = settingsFragment;
//                selectedId = 4;
            }

            if(activeFragment == cardsFragment) {
                activeId = 1;
            } else if(activeFragment == counterFragment) {
                activeId = 2;
            } else if(activeFragment == rulesFragment) {
                activeId = 3;
//            } else if(activeFragment == settingsFragment) {
//                activeId = 4;
            }

            if (selected != null && selected != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                selectedId > activeId ? R.anim.slide_in_right : R.anim.slide_in_left,
                                selectedId > activeId ? R.anim.slide_out_left : R.anim.slide_out_right
                        )
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
            bottomNav.animate()
                .translationY(bottomNav.getHeight())
                .setInterpolator(new AccelerateInterpolator())
                    .setDuration(300).withEndAction(() -> bottomNav.setVisibility(View.GONE)).start();

        }
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreenMode() {
        isFullScreen = false;
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);

            bottomNav.animate()
                .translationY(0)
                    .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();
        }
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }
}