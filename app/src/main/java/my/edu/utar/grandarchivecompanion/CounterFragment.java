// java
package my.edu.utar.grandarchivecompanion;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    //sound effects
    // MediaPlayer damageSound, healSound;
    private SoundPool soundPool;
    private int damageSoundId, healSoundId, loseSoundId;

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

        ((MainActivity) getActivity()).enterFullScreenMode();

        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        } else {
            soundPool = new SoundPool(2, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        damageSoundId = soundPool.load(getContext(), R.raw.damage_sound, 1);
        healSoundId = soundPool.load(getContext(), R.raw.heal_sound, 1);
        loseSoundId = soundPool.load(getContext(), R.raw.lose_sound, 1);

        TextView counterValueA = view.findViewById(R.id.counter_value_a);
        TextView counterValueB = view.findViewById(R.id.counter_value_b);
        player1background = view.findViewById(R.id.player_a_background);
        player2background = view.findViewById(R.id.player_b_background);

        FloatingActionButton fabMain = view.findViewById(R.id.fab_main);
        FloatingActionButton fabLog = view.findViewById(R.id.fab_log);
        FloatingActionButton fabChangeChampion = view.findViewById(R.id.fab_change_champion);
        FloatingActionButton fabRollDice = view.findViewById(R.id.fab_roll_dice);

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

        player1background.setOnTouchListener((pViewA, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                int change = (x < pViewA.getWidth() / 2) ? -1 : 1;

                viewModel.changeLife(true, change); // Tell the ViewModel what happened

                // Keep the UI feedback in the Fragment
                showChange(view.findViewById(R.id.damage_indicator_layer_a), change, true);
                if (change > 0) ChampionAnimationHelper.playDamage(player1background);
                else ChampionAnimationHelper.playHeal(player1background);
                vibrate();
                return true;
            }
            return false;
        });

        player2background.setOnTouchListener((pViewB, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                int change = (x < pViewB.getWidth() / 2) ? -1 : 1;

                viewModel.changeLife(false, change); // Tell the ViewModel what happened

                // Keep the UI feedback in the Fragment
                showChange(view.findViewById(R.id.damage_indicator_layer_b), change, false);
                if (change > 0) ChampionAnimationHelper.playDamage(player2background);
                else ChampionAnimationHelper.playHeal(player2background);
                vibrate();
                return true;
            }
            return false;
        });

        //--- Find the new panel and its close button ---
        logPanel = view.findViewById(R.id.log_panel);
        logPanelCloseButton = view.findViewById(R.id.log_panel_close_button);

        // FAB handlers
        fabMain.setOnClickListener(v -> {
            if (!isFabOpen[0]) {
                FloatingActionButton[] fabs = {fabLog, fabChangeChampion, fabRollDice};
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
            /*
            BottomSheetDialog diceDialog = new BottomSheetDialog(requireContext());
            View diceView = getLayoutInflater().inflate(R.layout.dialog_dice_roller, null);
            diceDialog.setContentView(diceView);

            Button rollD6Button = diceView.findViewById(R.id.roll_d6_button);
            TextView diceResultText = diceView.findViewById(R.id.dice_result_text);

            rollD6Button.setOnClickListener(v1 -> {
                int result = (int) (Math.random() * 6) + 1;
                diceResultText.setText("D6 Result: " + result);
                vibrate();
            });

            diceDialog.show();

             */

            Toast.makeText(getContext(), "Dice Roller feature coming soon!", Toast.LENGTH_SHORT).show();
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

    private void vibrate(){
        Vibrator vibrator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = requireContext().getSystemService(VibratorManager.class);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, 255));
            } else {
                //deprecated in API 26
                vibrator.vibrate(50);
            }
        }
    }

    private void playDamageSound(){
        soundPool.play(damageSoundId, 1, 1, 0, 0, 1);
    }

    private void playHealSound(){
        soundPool.play(healSoundId, 1, 1, 0, 0, 1);
    }

    private void playLoseSound(){
        soundPool.play(loseSoundId, 1, 1, 0, 0, 1f);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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



    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null){
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            requireActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (navBar != null) {
                navBar.setVisibility(View.GONE);
            }
            View decorView = requireActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).exitFullScreenMode();

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
}


