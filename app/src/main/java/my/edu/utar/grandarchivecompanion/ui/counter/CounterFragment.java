// java
package my.edu.utar.grandarchivecompanion.ui.counter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import android.widget.ImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import my.edu.utar.grandarchivecompanion.MainActivity;
import my.edu.utar.grandarchivecompanion.R;


public class CounterFragment extends Fragment {

    private ImageView player1background, player2background;
    private CounterViewModel viewModel;
    private TextView activeChangeTextA = null;
    private TextView activeChangeTextB = null;
    private int pendingChangeA = 0;
    private int pendingChangeB = 0;
    private LogAdapter logAdapter;
    private MaterialCardView logPanel;
    private ImageButton logPanelCloseButton;
    private boolean isLogPanelVisible = false;



    private String[] champions = {
            "Alice", "Allen", "Arisanna", "Ciel", "Diana", "Diana (Astra)", "Diao Chan", "Guo Jia",
            "Jin", "Kong Ming", "Lorraine", "Lu Bu", "Mordred", "Rai", "Zander", "Nico",
            "Polkhawk", "Vanitas", "Merlin", "Silvie", "Tonoris", "Tristan"
    };

    private int[] championImages = {
            R.drawable.alice, R.drawable.allen, R.drawable.arisanna, R.drawable.ciel,
            R.drawable.diana, R.drawable.diana_astra, R.drawable.diaochan, R.drawable.guojia, R.drawable.jin,
            R.drawable.kongming, R.drawable.lorraine,R.drawable.lubu, R.drawable.mordred, R.drawable.rai,
            R.drawable.zander, R.drawable.nico, R.drawable.polkhawk, R.drawable.vanitas,
            R.drawable.merlin, R.drawable.silvie, R.drawable.tonoris, R.drawable.shadowdancer
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        viewModel = new ViewModelProvider(this).get(CounterViewModel.class);

        //--Initialize the Log Adapter ---
        logAdapter = new LogAdapter();



        TextView counterValueA = view.findViewById(R.id.counter_value_a);
        TextView counterValueB = view.findViewById(R.id.counter_value_b);
        player1background = view.findViewById(R.id.player_a_background);
        player2background = view.findViewById(R.id.player_b_background);

        FloatingActionButton fabMain = view.findViewById(R.id.fab_main);
        FloatingActionButton fabLog = view.findViewById(R.id.fab_log);
        FloatingActionButton fabChangeChampion = view.findViewById(R.id.fab_change_champion);
        FloatingActionButton fabRollDice = view.findViewById(R.id.fab_roll_dice);
        FloatingActionButton fabFullScreen = view.findViewById(R.id.fab_full_screen);

        final boolean[] isFabOpen = {false};

        // --- OBSERVE the LiveData from ViewModel ---
        viewModel.getLifeA().observe(getViewLifecycleOwner(), value -> counterValueA.setText(String.valueOf(value)));
        viewModel.getLifeB().observe(getViewLifecycleOwner(), value -> counterValueB.setText(String.valueOf(value)));

        //Observe logs for RecyclerView (if needed in future)
        viewModel.getLogEntries().observe(getViewLifecycleOwner(), logs -> {
            //Update RecyclerView adapter with new logs
        });

        // --- Update the Observer for log entries ---
        viewModel.getLogEntries().observe(getViewLifecycleOwner(), logs -> {
            logAdapter.updateLogs(logs); // Update the adapter with new logs
        });

        // Setup for Player 1
        View player1Indicator = view.findViewById(R.id.damage_indicator_layer_a);
        setupPlayerTouchListener(player1background, player1Indicator, true);

// Setup for Player 2
        View player2Indicator = view.findViewById(R.id.damage_indicator_layer_b);
        setupPlayerTouchListener(player2background, player2Indicator, false);

        //--- Find the new panel and its close button ---
        logPanel = view.findViewById(R.id.log_panel);
        logPanelCloseButton = view.findViewById(R.id.log_panel_close_button);

        // FAB handlers
        fabMain.setOnClickListener(v -> {
            if (!isFabOpen[0]) {
                FloatingActionButton[] fabs = {fabLog, fabChangeChampion, fabRollDice, fabFullScreen};
                for (int i = 0; i < fabs.length; i++) {
                    showFabInCircle(fabs[i], i, fabs.length, 80f); // radius 120dp
                }
                fabMain.animate().rotation(135f).setDuration(300).start();
                isFabOpen[0] = true;
                fabMain.setImageResource(R.drawable.ic_refresh);
            } else {
                hideFab(fabLog);
                hideFab(fabChangeChampion);
                hideFab(fabRollDice);
                hideFab(fabFullScreen);
                fabMain.animate().rotation(0f).setDuration(300).start();
                isFabOpen[0] = false;
                fabMain.setImageResource(R.drawable.list_icon);
            }

            vibrate();
        });


        fabLog.setOnClickListener(v -> {
            showLogPanel();
            vibrate();
        });

        logPanelCloseButton.setOnClickListener(v -> {
            hideLogPanel();
            vibrate();
        });

        logAdapter = new LogAdapter(); // Make sure logAdapter is a member variable
        RecyclerView logRecyclerView = view.findViewById(R.id.log_recycler_view);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        logRecyclerView.setAdapter(logAdapter);

        // This observer will now automatically update the recycler view inside the panel
        viewModel.getLogEntries().observe(getViewLifecycleOwner(), logs -> {
            if (logAdapter != null) {
                logAdapter.updateLogs(logs);
            }
        });

        fabMain.setOnLongClickListener(fabMainLongView -> {
            viewModel.resetLife(); // Just tell the ViewModel to reset
            vibrate();
            Toast.makeText(getContext(), "Game Reset!", Toast.LENGTH_SHORT).show();
            return true;
        });

        fabChangeChampion.setOnClickListener(fabChangeView -> {
            showChampionPicker(1);
            showChampionPicker(2);
            vibrate();
        });

        // Optional: dice roll FAB remains commented if not used

        fabRollDice.setOnClickListener(v -> {
            showDiceRollerPopup(v); // 'v' is the FAB button itself
            vibrate();
        });

        fabFullScreen.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();

                // Toggle full-screen mode
                if (activity.isFullScreen()) {
                    activity.exitFullScreenMode();
                } else {
                    activity.enterFullScreenMode();
                }
            }
            vibrate();
        });


        return view;
    }

    private void showChange(FrameLayout playerLayer, int amount, boolean isPlayerA) {
        TextView changeText;
        int newValue;

        if (isPlayerA) {
            pendingChangeA += amount;

            if (activeChangeTextA == null) {
                changeText = new TextView(playerLayer.getContext());
                activeChangeTextA = changeText;
                playerLayer.addView(changeText);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL
                );
                params.topMargin = 200;
                changeText.setLayoutParams(params);

                changeText.setTextSize(32f);
                changeText.setShadowLayer(8f, 4f, 4f, Color.BLACK);
            } else {
                changeText = activeChangeTextA;
                changeText.animate().cancel();
                changeText.setAlpha(1f);
                changeText.setTranslationY(0f);
            }

            newValue = pendingChangeA;

        } else {
            pendingChangeB += amount;

            if (activeChangeTextB == null) {
                changeText = new TextView(playerLayer.getContext());
                activeChangeTextB = changeText;
                playerLayer.addView(changeText);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL
                );
                params.topMargin = 200;
                changeText.setLayoutParams(params);

                changeText.setTextSize(32f);
                changeText.setShadowLayer(8f, 4f, 4f, Color.BLACK);
            } else {
                changeText = activeChangeTextB;
                changeText.animate().cancel();
                changeText.setAlpha(1f);
                changeText.setTranslationY(0f);
            }

            newValue = pendingChangeB;
        }

        if (newValue > 0) {
            changeText.setText("+" + newValue);
            changeText.setTextColor(Color.WHITE);
        } else {
            changeText.setText(String.valueOf(newValue));
            changeText.setTextColor(Color.WHITE);
        }

        changeText.animate()
                .alpha(0f)
                .translationY(-200f)
                .setDuration(1000)
                .withEndAction(() -> {
                    playerLayer.removeView(changeText);
                    if (isPlayerA) {
                        activeChangeTextA = null;
                        pendingChangeA = 0;
                    } else {
                        activeChangeTextB = null;
                        pendingChangeB = 0;
                    }
                })
                .start();
    }

    private void showChampionPicker(int player) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        // 1. Inflate the new carousel layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_champion_carousel, null);
        dialog.setContentView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.champion_carousel_recycler_view);

        // 2. Set the LayoutManager to be HORIZONTAL
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 3. Attach the magic SnapHelper to the RecyclerView. This makes it snap to the center.
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        // In showChampionPicker, after snapHelper.attachToRecyclerView(recyclerView);
        addCarouselZoomEffect(recyclerView);

        // 4. Set your updated adapter
        ChampionAdapter adapter = new ChampionAdapter(champions, championImages, position -> {
            if (player == 1) {
                player1background.setImageResource(championImages[position]);
            } else {
                player2background.setImageResource(championImages[position]);
            }
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        // For Player 2, we still rotate the entire dialog view.
        if (player == 2) {
            dialogView.setRotation(180);
        }

        dialog.show();
    }

    private void showFabInCircle(FloatingActionButton fab, int index, int total, float radiusDp) {
        float radiusPx = radiusDp * getResources().getDisplayMetrics().density;

        // Calculate angle in radians (360° divided equally)
        double angle = Math.toRadians((360.0 / total) * index);

        float offsetX = (float) (Math.cos(angle) * radiusPx);
        float offsetY = (float) (Math.sin(angle) * radiusPx);

        fab.setVisibility(View.VISIBLE);
        fab.setAlpha(0f);
        fab.setTranslationX(0f);
        fab.setTranslationY(0f);

        fab.animate()
                .translationX(offsetX)
                .translationY(offsetY)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void hideFab(FloatingActionButton fab) {
        fab.animate()
                .translationX(0f)
                .translationY(0f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> fab.setVisibility(View.GONE))
                .start();
    }


    private void hideFabHorizontally(FloatingActionButton fab) {
        fab.animate()
                .translationX(0f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> fab.setVisibility(View.GONE))
                .start();
    }

    private void vibrate(){
        Vibrator vibrator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = requireContext().getSystemService(VibratorManager.class);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // deprecated in API 26
                vibrator.vibrate(50);
            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Safely check if the activity exists and if we are actually in full-screen mode
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.isFullScreen()) {
                mainActivity.exitFullScreenMode();
            }
        }
    }

    // Add these two new methods anywhere inside your CounterFragment class

    private void showLogPanel() {
        if (isLogPanelVisible) return;

        // Make it visible and animate it in from the right
        logPanel.setVisibility(View.VISIBLE);
        logPanel.animate()
                .translationX(0) // Move to its original position
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
        isLogPanelVisible = true;
    }

    private void hideLogPanel() {
        if (!isLogPanelVisible) return;

        // Animate it out to the right
        logPanel.animate()
                .translationX(logPanel.getWidth() + 50) // Move it off-screen
                .setDuration(250)
                .withEndAction(() -> logPanel.setVisibility(View.INVISIBLE)) // Hide it after animation
                .start();
        isLogPanelVisible = false;
    }

    private void setupPlayerTouchListener(final View backgroundView, final View indicatorView, final boolean isPlayer1) {
        backgroundView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // We only care about the initial touch down event
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    int requestedChange;

                    // Determine the requested change based on which player it is.
                    // Player 1 (bottom): Left is -1, Right is +1
                    // Player 2 (top, mirrored): Left is +1, Right is -1
                    if (isPlayer1) {
                        requestedChange = (x < view.getWidth() / 2) ? -1 : 1;
                    } else {
                        requestedChange = (x < view.getWidth() / 2) ? -1 : 1;
                    }

                    // Ask the ViewModel to apply the change and tell us what actually happened.
                    final int actualChange = viewModel.changeLife(isPlayer1, requestedChange);

                    // Only provide feedback if the life total was actually modified.
                    if (actualChange != 0) {
                        // Show the floating "+1" or "-1" text
                        // THIS IS THE CORRECTED LINE:
                        showChange((FrameLayout) indicatorView, actualChange, isPlayer1);

                        // Play the correct animation based on the actual change
                        if (actualChange > 0) {
                            ChampionAnimationHelper.playDamage(backgroundView);
                        } else {
                            ChampionAnimationHelper.playHeal(backgroundView);
                        }

                        // Provide haptic feedback
                        vibrate();
                    }

                    // We've handled the event, so return true.
                    return true;
                }
                // Ignore other event types (move, up, etc.)
                return false;
            }
        });
    }

    private void addCarouselZoomEffect(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                float midPoint = recyclerView.getWidth() / 2f;
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childMidPoint = (child.getLeft() + child.getRight()) / 2f;
                    float distanceFromCenter = Math.abs(midPoint - childMidPoint);

                    // Scale the item based on its distance from the center.
                    // The closer to the center, the larger it is (closer to 1.0f).
                    // The further away, the smaller it is (closer to 0.8f).
                    float scale = 1f - (distanceFromCenter / midPoint) * 0.2f; // 0.2f is the amount to shrink
                    child.setScaleX(Math.max(0.8f, scale));
                    child.setScaleY(Math.max(0.8f, scale));
                }
            }
        });
    }

    // Add these three new methods to CounterFragment.java

    // In CounterFragment.java

    private void showDiceRollerPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_dice_roller, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(20);

        // --- MODIFIED: Find all the views for BOTH players ---
        // Player A (Bottom)
        ImageView diceImageA1 = popupView.findViewById(R.id.dice_image_view_a1);
        ImageView diceImageA2 = popupView.findViewById(R.id.dice_image_view_a2);
        TextView resultTextA = popupView.findViewById(R.id.dice_result_text_a);
        Button roll2D6ButtonA = popupView.findViewById(R.id.roll_2d6_button_a);


        // Player B (Top)
        ImageView diceImageB1 = popupView.findViewById(R.id.dice_image_view_b1);
        ImageView diceImageB2 = popupView.findViewById(R.id.dice_image_view_b2);
        TextView resultTextB = popupView.findViewById(R.id.dice_result_text_b);
        Button roll2D6ButtonB = popupView.findViewById(R.id.roll_2d6_button_b);


        // --- MODIFIED: Both sets of buttons call the same logic ---
        // Pass all the necessary views to the roll methods.
        View.OnClickListener roll2d6ListenerA = v -> rollTwoDice(diceImageA1, diceImageA2, resultTextA);
        roll2D6ButtonA.setOnClickListener(roll2d6ListenerA);


        View.OnClickListener roll2d6ListenerB = v -> rollTwoDice(diceImageB1, diceImageB2, resultTextB);
        roll2D6ButtonB.setOnClickListener(roll2d6ListenerB);





        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    // MODIFIED: Method now takes views for both players
    private void rollTwoDice(ImageView die1, ImageView die2, TextView resultTextView) {
        // Make sure both dice are visible before animating
        die1.setVisibility(View.VISIBLE);
        die2.setVisibility(View.VISIBLE);

        // Animate the dice for visual effect
        die1.animate().rotationBy(360f).setDuration(500).start();

        // The end action is attached to only one animation to ensure the logic runs just once.
        die2.animate()
                .rotationBy(-360f)
                .setDuration(500)
                .withEndAction(() -> {
                    // Generate random numbers for two dice
                    java.util.Random random = new java.util.Random();
                    int result1 = random.nextInt(6) + 1; // Generates a number between 1 and 6
                    int result2 = random.nextInt(6) + 1; // Generates a number between 1 and 6
                    int sum = result1 + result2;

                    updateDiceUI(die1, die2, resultTextView, result1, result2, sum);
                    vibrate(); // Assuming vibrate() is a method in your class
                }).start();
    }

    /**
     * Updates the ImageViews and TextView with the new dice roll results.
     */
    private void updateDiceUI(ImageView die1, ImageView die2, TextView resultTextView, int result1, int result2, int sum) {
        int[] dieFaces = {
                R.drawable.ic_dice_1, R.drawable.ic_dice_2, R.drawable.ic_dice_3,
                R.drawable.ic_dice_4, R.drawable.ic_dice_5, R.drawable.ic_dice_6
        };

        String resultString = String.format("Rolled %d & %d (Total: %d)", result1, result2, sum);

        die1.setImageResource(dieFaces[result1 - 1]);
        die2.setImageResource(dieFaces[result2 - 1]);
        resultTextView.setText(resultString);
    }
}