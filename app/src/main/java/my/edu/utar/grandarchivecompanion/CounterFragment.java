package my.edu.utar.grandarchivecompanion;

import android.graphics.Color;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;



import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import my.edu.utar.grandarchivecompanion.ChampionAnimationHelper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;


import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class CounterFragment extends Fragment {

    private ImageView player1background, player2background;
    private Button changeBackgroundButton1, changeBackgroundButton2;



    private TextView activeChangeTextA = null;
    private TextView activeChangeTextB = null;

    ArrayList<String> damageLogs = new ArrayList<>();
    private void addLog(String entry){
        damageLogs.add(entry);
    }

    private int pendingChangeA = 0;
    private int pendingChangeB = 0;

    private String[] champions = {
            "Alice", "Allen", "Arisanna", "Ciel", "Diana", "Diana (Astra)", "Diao Chan", "Guo Jia",
            "Jin", "Kong Ming", "Lorraine", "Lu Bu", "Mordred", "Rai", "Zander", "Nico",
            "Polkhawk", "Vanitas", "Merlin", "Silvie", "Tonoris", "Tristan, Shadow Dancer"
    };

    private int[] championImages = {
            R.drawable.alice, R.drawable.allen, R.drawable.arisanna, R.drawable.ciel,
            R.drawable.diana, R.drawable.diana_astra, R.drawable.diaochan, R.drawable.guojia, R.drawable.jin,
            R.drawable.kongming, R.drawable.lorraine,R.drawable.lubu, R.drawable.mordred, R.drawable.rai,
            R.drawable.zander, R.drawable.nico, R.drawable.polkhawk, R.drawable.vanitas,
            R.drawable.merlin, R.drawable.silvie, R.drawable.tonoris, R.drawable.shadowdancer
    };


    private int counterA = 0;
    private int counterB = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);


        ((MainActivity) getActivity()).enterFullScreenMode();




        TextView counterValueA = view.findViewById(R.id.counter_value_a);
        TextView counterValueB = view.findViewById(R.id.counter_value_b);
        player1background = view.findViewById(R.id.player_a_background);
        player2background = view.findViewById(R.id.player_b_background);
        changeBackgroundButton1 = view.findViewById(R.id.changeBackgroundButton1);
        changeBackgroundButton2 = view.findViewById(R.id.changeBackgroundButton2);

        // Initialize gesture detector


        // Initial values
        counterValueA.setText(String.valueOf(counterA));
        counterValueB.setText(String.valueOf(counterB));

        player1background.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                FrameLayout playerALayer = view.findViewById(R.id.damage_indicator_layer_a);

                if (x < v.getWidth() / 2) {
                    if (counterA > 0) {
                        counterA--;
                        counterValueA.setText(String.valueOf(counterA));
                        showChange(playerALayer, -1, true);
                        ChampionAnimationHelper.playHeal(player1background);
                        vibrate();
                    }
                } else {
                    counterA++;
                    counterValueA.setText(String.valueOf(counterA));
                    showChange(playerALayer, +1, true);
                    ChampionAnimationHelper.playDamage(player1background);
                    vibrate();
                }
                return true;
            }
            return false;
        });


        player2background.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                FrameLayout playerBLayer = view.findViewById(R.id.damage_indicator_layer_b);

                if (x < v.getWidth() / 2) {
                    if (counterB > 0) {
                        counterB--;
                        counterValueB.setText(String.valueOf(counterB));
                        showChange(playerBLayer, -1, false);
                        ChampionAnimationHelper.playHeal(player2background);
                        vibrate();
                    }
                } else {
                    counterB++;
                    counterValueB.setText(String.valueOf(counterB));
                    showChange(playerBLayer, +1, false);
                    ChampionAnimationHelper.playDamage(player2background);
                    vibrate();
                }
                return true;
            }
            return false;
        });




        // Let players choose champion by tapping their background
        changeBackgroundButton1.setOnClickListener(v -> showChampionPicker(1));
        changeBackgroundButton2.setOnClickListener(v -> showChampionPicker(2));

        changeBackgroundButton2.setOnLongClickListener(v -> {
            counterA = 0;
            counterB = 0;
            counterValueA.setText(String.valueOf(counterA));
            counterValueB.setText(String.valueOf(counterB));
            vibrate();
            return true;
        });

        changeBackgroundButton1.setOnLongClickListener(v -> {
            counterA = 0;
            counterB = 0;
            counterValueA.setText(String.valueOf(counterA));
            counterValueB.setText(String.valueOf(counterB));
            vibrate();
            return true;
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
                changeText.animate().cancel(); // cancel previous animation
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

        // Update text + color
        if (newValue > 0) {
            changeText.setText("+" + newValue);
            changeText.setTextColor(Color.WHITE);

        } else {
            changeText.setText(String.valueOf(newValue));
            changeText.setTextColor(Color.WHITE);
        }

        // Restart animation from scratch
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_champion_picker, null);
        dialog.setContentView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.champion_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (player == 2){
            dialogView.setRotation(180);
        }
        recyclerView.setAdapter(new ChampionAdapter(champions, championImages, position -> {
            if (player == 1) {
                player1background.setImageResource(championImages[position]);
            } else {
                player2background.setImageResource(championImages[position]);
            }
            dialog.dismiss();
        }));
        dialog.show();
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
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                vibrator.vibrate(50);
            }
        }
    }

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





    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).exitFullScreenMode();

    }
}


