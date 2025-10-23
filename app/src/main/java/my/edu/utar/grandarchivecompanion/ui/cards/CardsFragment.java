package my.edu.utar.grandarchivecompanion.ui.cards;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import my.edu.utar.grandarchivecompanion.ui.cards.LoadingFootAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.FragmentCardsBinding;

public class CardsFragment extends Fragment {

    private FragmentCardsBinding binding;
    private CardAdapter adapter;
    private CardsViewModel viewModel;
    private ImageView loadingGif;
    private LoadingFootAdapter footerAdapter;
    private ConcatAdapter concatAdapter;

    // --- Suggestion 2: Encapsulate set data into a model class ---
    private static class SetInfo {
        final String name;
        final String prefix;

        SetInfo(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        @NonNull
        @Override
        public String toString() {
            // This is what the spinner will display
            return name;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Scope the ViewModel to the parent fragment (HomeFragment).
        // This ViewModel will be shared by all fragments in the ViewPager.
        viewModel = new ViewModelProvider(requireParentFragment()).get(CardsViewModel.class);
        loadingGif = view.findViewById(R.id.loadingGif);
        // Setup the adapter with a refined click listener
        setupAdapter();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Now it's safe to setup listeners and observers
        setupSetSpinner();
        setupListeners();
        setupObservers();
    }

    private void setupAdapter() {
        // In CardsFragment.java, inside onViewCreated() or a setup method...
        adapter = new CardAdapter(card -> {
            try {
                // Create an Intent to start the new Activity
                Intent intent = new Intent(requireContext(), CardDetailActivity.class);

                // Put the selected card object as an "extra"
                intent.putExtra("selectedCard", card);

                // Start the activity
                startActivity(intent);

            } catch (Exception e) {
                // Handle error
            }
        });

        footerAdapter = new LoadingFootAdapter();
        concatAdapter = new ConcatAdapter(adapter, footerAdapter);
        binding.recyclerView.setAdapter(concatAdapter);

    }

    private void setupSetSpinner() {
        // --- Suggestion 2: Use the SetInfo class ---
        final List<SetInfo> sets = new ArrayList<>();
        sets.add(new SetInfo("All Sets", ""));
        sets.add(new SetInfo("Demo 2022", "DEMO22"));
        sets.add(new SetInfo("Promo 2022", "P22"));
        sets.add(new SetInfo("Dawn of Ashes (1st Ed.)", "DOA 1st"));
        sets.add(new SetInfo("Dawn of Ashes Prelude", "DOAp"));
        sets.add(new SetInfo("Kickstarter Promotional", "KSP"));
        sets.add(new SetInfo("GeSeCha", "GSC"));
        sets.add(new SetInfo("Event Packs", "EP"));
        sets.add(new SetInfo("Proxia's Vault", "PRXY"));
        sets.add(new SetInfo("Dawn of Ashes (Alter)", "DOA Alter"));
        sets.add(new SetInfo("DOA Starter Deck", "DOA SD"));
        sets.add(new SetInfo("Supporter Pack 1", "SP1"));
        sets.add(new SetInfo("Promotional 2023", "P23 Promo"));
        sets.add(new SetInfo("Demo 2023", "DEMO23"));
        sets.add(new SetInfo("Fractured Crown", "FTC"));
        sets.add(new SetInfo("Fractured Crown: Armaments", "FTCA"));
        sets.add(new SetInfo("Promotional 2024", "P24"));
        sets.add(new SetInfo("Alchemical Revolution", "ALC"));
        sets.add(new SetInfo("Alchemical Revolution (1st Ed.)", "ALC 1st"));
        sets.add(new SetInfo("ALC Starter Deck", "ALC SD"));
        sets.add(new SetInfo("SquareLive Collaboration", "SLC"));
        sets.add(new SetInfo("Mercurial Heart", "MRC"));
        sets.add(new SetInfo("Mercurial Heart (1st Ed.)", "MRC 1st"));
        sets.add(new SetInfo("Re:Collection - Shadowdancer", "ReC-SHD"));
        sets.add(new SetInfo("Re:Collection - Slime Sovereign", "ReC-SLM"));
        sets.add(new SetInfo("Supporter Pack 2", "SP2"));
        sets.add(new SetInfo("Mortal Ambition", "AMB"));
        sets.add(new SetInfo("Mortal Ambition (1st Ed.)", "AMB 1st"));
        sets.add(new SetInfo("Mortal Ambition Starter Deck", "AMB SD"));
        sets.add(new SetInfo("Mortal Ambition Draft Pack", "AMB DP"));
        sets.add(new SetInfo("Alchemical Revolution (Alter)", "ALC Alter"));
        sets.add(new SetInfo("Promotional 2025", "P25"));
        sets.add(new SetInfo("Promo 2024", "P24"));
        sets.add(new SetInfo("Abyssal Heaven", "HVN"));
        sets.add(new SetInfo("Abyssal Heaven (1st Ed.)", "HVN 1st"));
        sets.add(new SetInfo("Re:Collection - Heaven's Favoured", "ReC-HVF"));
        sets.add(new SetInfo("Re:Collection - Idyll Corsage", "ReC-IDY"));
        sets.add(new SetInfo("Mercurial Heart (Alter)", "MRC Alter"));
        sets.add(new SetInfo("Supporter Pack 3", "SP3"));
        sets.add(new SetInfo("Distorted Reflections", "DTR"));
        sets.add(new SetInfo("Distorted Reflections (1st Ed.)", "DTR 1st"));
        sets.add(new SetInfo("DTR Starter Deck", "DTR SD"));
        // ... Add all other sets in the same way


        ArrayAdapter<SetInfo> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sets);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.setSpinner.setAdapter(spinnerAdapter);

        binding.setSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SetInfo selectedSet = (SetInfo) parent.getItemAtPosition(position);
                viewModel.setSetPrefix(selectedSet.prefix);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing */ }
        });
    }

    private void setupListeners() {
        binding.buttonRetry.setOnClickListener(v -> viewModel.fetchCards());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                Boolean isLoadingMore = viewModel.isLoadingMore().getValue();
                if (isLoadingMore != null && isLoadingMore) {
                    return; // Already loading
                }

                // Load more when near the end of the list
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    viewModel.loadMoreCards();
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getCards().observe(getViewLifecycleOwner(), cardItems -> {
            if (cardItems != null) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(cardItems);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                loadingGif.setVisibility(View.VISIBLE);
                // Load the gif resource (add `res/drawable/loading.gif`)
                Glide.with(requireContext())
                        .asGif()
                        .load(R.drawable.loading)
                        .into(loadingGif);
            } else {
                loadingGif.setVisibility(View.GONE);
            }
        });

        viewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            if (Boolean.TRUE.equals(isLoadingMore)) {
                if (!concatAdapter.getAdapters().contains(footerAdapter)) {
                    concatAdapter.addAdapter(footerAdapter);
                }
            } else {
                if (concatAdapter.getAdapters().contains(footerAdapter)) {
                    concatAdapter.removeAdapter(footerAdapter);
                }
            }
        });

        viewModel.isError().observe(getViewLifecycleOwner(), isError -> {
            binding.errorContainer.setVisibility(isError ? View.VISIBLE : View.GONE);
            if (isError) {
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // This is correct for preventing memory leaks
    }
}
