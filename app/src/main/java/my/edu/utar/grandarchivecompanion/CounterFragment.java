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
    private boolean areSoundsUnlocked = false;
    private static final String PREFS_NAME = "GrandArchivePrefs";
    private static final String SOUNDS_UNLOCKED_KEY = "sounds_unlocked";
    private boolean hasPlayerALost = false;
    private boolean hasPlayerBLost = false;

    //sound effects
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
        logAdapter = new LogAdapter(); // Initialize adapter

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(3)
                .build();

        damageSoundId = soundPool.load(getContext(), R.raw.damage_sound, 1);
        healSoundId = soundPool.load(getContext(), R.raw.heal_sound, 1);
        loseSoundId = soundPool.load(getContext(), R.raw.lose_sound, 1);

        TextView counterValueA = view.findViewById(R.id.counter_value_a);
        TextView counterValueB = view.findViewById(R.id.counter_value_b);
        player1background = view.findViewById(R.id.player_a_background);
        player2background = view.findViewById(R.id.player_b_background);
        logPanel = view.findViewById(R.id.log_panel);
        logPanelCloseButton = view.findViewById(R.id.log_panel_close_button);

        RecyclerView logRecyclerView = view.findViewById(R.id.log_recycler_view);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        logRecyclerView.setAdapter(logAdapter);

        viewModel.getLogEntries().observe(getViewLifecycleOwner(), logs -> {
            if (logAdapter != null) {
                logAdapter.updateLogs(logs);
            }
        });

        viewModel.getLifeA().observe(getViewLifecycleOwner(), life -> {
            counterValueA.setText(String.valueOf(life));
            if (life == 4492) {
                unlockSounds();
            }
            if (life >= 25 && !hasPlayerALost) {
                playLoseSound();
                hasPlayerALost = true;
            }
        });

        viewModel.getLifeB().observe(getViewLifecycleOwner(), life -> {
            counterValueB.setText(String.valueOf(life));
            if (life == 4492) {
                unlockSounds();
            }
            if (life >= 25 && !hasPlayerBLost) {
                playLoseSound();
                hasPlayerBLost = true;
            }
        });

        View player1Indicator = view.findViewById(R.id.damage_indicator_layer_a);
        setupPlayerTouchListener(player1background, player1Indicator, true);

        View player2Indicator = view.findViewById(R.id.damage_indicator_layer_b);
        setupPlayerTouchListener(player2background, player2Indicator, false);

        FloatingActionButton fabMain = view.findViewById(R.id.fab_main);
        FloatingActionButton fabLog = view.findViewById(R.id.fab_log);
        FloatingActionButton fabChangeChampion = view.findViewById(R.id.fab_change_champion);
        FloatingActionButton fabRollDice = view.findViewById(R.id.fab_roll_dice);
        FloatingActionButton fabFullScreen = view.findViewById(R.id.fab_full_screen);

        final boolean[] isFabOpen = {false};
        fabMain.setOnClickListener(v -> {
            if (!isFabOpen[0]) {
                FloatingActionButton[] fabs = {fabLog, fabChangeChampion, fabRollDice, fabFullScreen};
                for (int i = 0; i < fabs.length; i++) {
                    showFabInCircle(fabs[i], i, fabs.length, 80f);
                }
                fabMain.animate().rotation(135f).setDuration(300).start();
                fabMain.setImageResource(R.drawable.ic_refresh);
            } else {
                hideFab(fabLog);
                hideFab(fabChangeChampion);
                hideFab(fabRollDice);
                hideFab(fabFullScreen);
                fabMain.animate().rotation(0f).setDuration(300).start();
                fabMain.setImageResource(R.drawable.list_icon);
            }
            isFabOpen[0] = !isFabOpen[0];
            vibrate();
        });

        fabLog.setOnClickListener(v -> {
            showLogPanel();
            vibrate();
        });

        fabMain.setOnLongClickListener(v -> {
            viewModel.resetLife();
            hasPlayerALost = false;
            hasPlayerBLost = false;
            vibrate();
            Toast.makeText(getContext(), "Game Reset!", Toast.LENGTH_SHORT).show();
            return true;
        });

        fabChangeChampion.setOnClickListener(v -> {
            showChampionPicker(1);
            showChampionPicker(2);
            vibrate();
        });

        fabRollDice.setOnClickListener(v -> {
            showDiceRollerPopup(v);
            vibrate();
        });

        fabFullScreen.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity.isFullScreen()) {
                    activity.exitFullScreenMode();
                } else {
                    activity.enterFullScreenMode();
                }
                vibrate();
            }
        });

        fabLog.setOnClickListener(v -> showLogPanel());
        logPanelCloseButton.setOnClickListener(v -> hideLogPanel());
        fabMain.setOnLongClickListener(v -> {
            viewModel.resetLife();
            hasPlayerALost = false;
            hasPlayerBLost = false;
            vibrate();
            Toast.makeText(getContext(), "Game Reset!", Toast.LENGTH_SHORT).show();
            return true;
        });
        fabChangeChampion.setOnClickListener(v -> {
            showChampionPicker(1);
            showChampionPicker(2);
        });
        fabRollDice.setOnClickListener(v -> showDiceRollerPopup(v));
        fabFullScreen.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity.isFullScreen()) {
                    activity.exitFullScreenMode();
                } else {
                    activity.enterFullScreenMode();
                }
            }
        });

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        areSoundsUnlocked = prefs.getBoolean(SOUNDS_UNLOCKED_KEY, false);
        if (areSoundsUnlocked) {
            Toast.makeText(getContext(), "Secret Sounds are Active!", Toast.LENGTH_SHORT).show();
        }

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

    private void vibrate() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = manager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void playDamageSound(){
        if (!areSoundsUnlocked) return;
        soundPool.play(damageSoundId, 1, 1, 0, 0, 1);
    }

    private void playHealSound(){
        if (!areSoundsUnlocked) return;
        soundPool.play(healSoundId, 1, 1, 0, 0, 1);
    }

    private void playLoseSound(){
        if (!areSoundsUnlocked) return;
        soundPool.play(loseSoundId, 1, 1, 0, 0, 1f);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showChampionPicker(int player) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_champion_carousel, null);
        dialog.setContentView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.champion_carousel_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        addCarouselZoomEffect(recyclerView);

        ChampionAdapter adapter = new ChampionAdapter(champions, championImages, position -> {
            if (player == 1) {
                player1background.setImageResource(championImages[position]);
            } else {
                player2background.setImageResource(championImages[position]);
            }
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        if (player == 2) {
            dialogView.setRotation(180);
        }

        dialog.show();
    }

    private void showFabInCircle(FloatingActionButton fab, int index, int total, float radiusDp) {
        float radiusPx = radiusDp * getResources().getDisplayMetrics().density;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.isFullScreen()) {
                mainActivity.exitFullScreenMode();
            }
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void showLogPanel() {
        if (isLogPanelVisible) return;

        logPanel.setVisibility(View.VISIBLE);
        logPanel.animate()
                .translationX(0)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
        isLogPanelVisible = true;
    }

    private void hideLogPanel() {
        if (!isLogPanelVisible) return;

        logPanel.animate()
                .translationX(logPanel.getWidth() + 50)
                .setDuration(250)
                .withEndAction(() -> logPanel.setVisibility(View.INVISIBLE))
                .start();
        isLogPanelVisible = false;
    }

    private void setupPlayerTouchListener(final View backgroundView, final View indicatorView, final boolean isPlayerA) {
        backgroundView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                int requestedChange;

                if (isPlayerA) {
                    requestedChange = (x < view.getWidth() / 2) ? -1 : 1;
                } else {
                    requestedChange = (x < view.getWidth() / 2) ? 1 : -1;
                }

                final int actualChange = viewModel.changeLife(isPlayerA, requestedChange);

                if (actualChange != 0) {
                    showChange((FrameLayout) indicatorView, actualChange, isPlayerA);
                    if (actualChange > 0) {
                        ChampionAnimationHelper.playDamage(backgroundView);
                        playDamageSound();
                    } else {
                        ChampionAnimationHelper.playHeal(backgroundView);
                        playHealSound();
                    }
                    vibrate();
                }
                return true;
            }
            return false;
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

                    float scale = 1f - (distanceFromCenter / midPoint) * 0.2f;
                    child.setScaleX(Math.max(0.8f, scale));
                    child.setScaleY(Math.max(0.8f, scale));
                }
            }
        });
    }

    private void unlockSounds() {
        if (areSoundsUnlocked) return;

        areSoundsUnlocked = true;

        Toast.makeText(getContext(), "SECRET SOUNDS UNLOCKED!", Toast.LENGTH_LONG).show();
        playHealSound();
        vibrate();

        android.content.SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SOUNDS_UNLOCKED_KEY, true);
        editor.apply();
    }

    private void showDiceRollerPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_dice_roller, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(20);

        ImageView diceImageA1 = popupView.findViewById(R.id.dice_image_view_a1);
        ImageView diceImageA2 = popupView.findViewById(R.id.dice_image_view_a2);
        TextView resultTextA = popupView.findViewById(R.id.dice_result_text_a);
        Button roll2D6ButtonA = popupView.findViewById(R.id.roll_2d6_button_a);

        ImageView diceImageB1 = popupView.findViewById(R.id.dice_image_view_b1);
        ImageView diceImageB2 = popupView.findViewById(R.id.dice_image_view_b2);
        TextView resultTextB = popupView.findViewById(R.id.dice_result_text_b);
        Button roll2D6ButtonB = popupView.findViewById(R.id.roll_2d6_button_b);

        View.OnClickListener roll2d6ListenerA = v -> rollTwoDice(diceImageA1, diceImageA2, resultTextA);
        roll2D6ButtonA.setOnClickListener(roll2d6ListenerA);

        View.OnClickListener roll2d6ListenerB = v -> rollTwoDice(diceImageB1, diceImageB2, resultTextB);
        roll2D6ButtonB.setOnClickListener(roll2d6ListenerB);

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    private void rollTwoDice(ImageView die1, ImageView die2, TextView resultTextView) {
        die1.setVisibility(View.VISIBLE);
        die2.setVisibility(View.VISIBLE);

        die1.animate().rotationBy(360f).setDuration(500).start();

        die2.animate()
                .rotationBy(-360f)
                .setDuration(500)
                .withEndAction(() -> {
                    java.util.Random random = new java.util.Random();
                    int result1 = random.nextInt(6) + 1;
                    int result2 = random.nextInt(6) + 1;
                    int sum = result1 + result2;

                    updateDiceUI(die1, die2, resultTextView, result1, result2, sum);
                    vibrate();
                }).start();
    }

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
