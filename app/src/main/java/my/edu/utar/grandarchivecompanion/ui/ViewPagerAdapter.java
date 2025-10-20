package my.edu.utar.grandarchivecompanion.ui; // Or your actual ui package

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import my.edu.utar.grandarchivecompanion.ui.cards.CardsFragment;
import my.edu.utar.grandarchivecompanion.ui.counter.CounterFragment;
import my.edu.utar.grandarchivecompanion.ui.rules.RulesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance for the given position
        switch (position) {
            case 0:
                return new CardsFragment();
            case 1:
                return new CounterFragment();
            case 2:
                return new RulesFragment(); // This will contain your WebView
            default:
                return new CardsFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {
        return 3; // The number of tabs you have
    }
}
