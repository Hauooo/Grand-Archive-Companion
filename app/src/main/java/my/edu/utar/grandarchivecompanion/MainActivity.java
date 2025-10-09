package my.edu.utar.grandarchivecompanion;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public boolean isFullScreen = false;

    public boolean isFullScreen() {
        return isFullScreen;
    }
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Load default fragment (Cards)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new CounterFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            if (item.getItemId() == R.id.nav_cards) {
                selected = new CardsFragment();
            } else if (item.getItemId() == R.id.nav_counter) {
                selected = new CounterFragment();
            } else if (item.getItemId() == R.id.nav_rules) {
                selected = new RulesFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }

            return true;
        });

        //Handle back button to exit full screen mode
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFullScreen) {
                    exitFullScreenMode();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
