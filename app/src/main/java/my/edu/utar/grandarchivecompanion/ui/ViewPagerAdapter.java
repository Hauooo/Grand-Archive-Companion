package my.edu.utar.grandarchivecompanion.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
                return new RulesFragment();
            default:
                return new CardsFragment(); // Default fallback
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Cards, Counter, Rules
    }
}