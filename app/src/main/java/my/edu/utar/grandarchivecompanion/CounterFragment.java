package my.edu.utar.grandarchivecompanion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class CounterFragment extends Fragment {

    private ImageView player1background, player2background;
    private Button changeBackgroundButton1, changeBackgroundButton2;

    private String[] champions = {
            "Alice", "Allen", "Arisanna", "Ciel", "Diana", "Diao Chan", "Guo Jia",
            "Jin", "Kong Ming", "Lorraine", "Mordred", "Rai", "Zander", "Nico",
            "Polkhawk", "Vanitas", "Merlin", "Silvie", "Tonoris", "Tristan"
    };

    private int[] championImages = {
            R.drawable.alice, R.drawable.allen, R.drawable.arisanna, R.drawable.ciel,
            R.drawable.diana, R.drawable.diaochan, R.drawable.guojia, R.drawable.jin,
            R.drawable.kongming, R.drawable.lorraine, R.drawable.mordred, R.drawable.rai,
            R.drawable.zander, R.drawable.nico, R.drawable.polkhawk, R.drawable.vanitas,
            R.drawable.merlin, R.drawable.silvie, R.drawable.tonoris, R.drawable.tristan
    };

    private int counterA = 0;
    private int counterB = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        TextView counterValueA = view.findViewById(R.id.counter_value_a);
        TextView counterValueB = view.findViewById(R.id.counter_value_b);
        player1background = view.findViewById(R.id.player_a_background);
        player2background = view.findViewById(R.id.player_b_background);
        changeBackgroundButton1 = view.findViewById(R.id.changeBackgroundButton1);
        changeBackgroundButton2 = view.findViewById(R.id.changeBackgroundButton2);



        // Initial values
        counterValueA.setText(String.valueOf(counterA));
        counterValueB.setText(String.valueOf(counterB));

        counterValueA.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                if (x < v.getWidth() / 2) {
                    if(counterA > 0) counterA--;
                } else {
                    counterA++;
                }
                counterValueA.setText(String.valueOf(counterA));
                return true; // consume only when touching counter
            }
            return false; // let others (like button) handle their clicks
        });

        counterValueB.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                if (x < v.getWidth() / 2) {
                    if(counterB > 0) counterB--;
                } else {
                    counterB++;
                }
                counterValueB.setText(String.valueOf(counterB));
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
            return true;
        });

        changeBackgroundButton1.setOnLongClickListener(v -> {
            counterA = 0;
            counterB = 0;
            counterValueA.setText(String.valueOf(counterA));
            counterValueB.setText(String.valueOf(counterB));
            return true;
        });


        return view;
    }

    private void showChampionPicker(int player) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Champion")
                .setItems(champions, (dialog, which) -> {
                    if (player == 1) {
                        player1background.setImageResource(championImages[which]);
                    } else {
                        player2background.setImageResource(championImages[which]);
                    }
                })
                .show();
    }

    public void onResume() {
        super.onResume();

        if (getActivity() != null){
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            if (navBar != null) {
                navBar.setVisibility(View.GONE);
            }
        }
    }

    public void onPause() {
        super.onPause();

        if (getActivity() != null){
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            if (navBar != null) {
                navBar.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }
}


