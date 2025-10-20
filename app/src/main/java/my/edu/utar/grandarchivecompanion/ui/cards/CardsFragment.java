package my.edu.utar.grandarchivecompanion.ui.cards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.FragmentCardsBinding;

public class CardsFragment extends Fragment {

    private FragmentCardsBinding binding;
    private CardAdapter adapter;
    private CardsViewModel viewModel;

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

        // --- BUG FIX & REFINEMENT 1: Initialize ViewModel first ---
        viewModel = new ViewModelProvider(this).get(CardsViewModel.class);

        // Setup the adapter with a refined click listener
        setupAdapter();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        // Now it's safe to setup listeners and observers
        setupSetSpinner();
        setupListeners();
        setupObservers();
    }

    private void setupAdapter() {
        adapter = new CardAdapter(card -> {
            // --- REFINEMENT 3: Improve Navigation Logic ---
            try {
                // Create a bundle to pass the selected card object
                Bundle bundle = new Bundle();
                bundle.putParcelable("selectedCard", card);

                // Find NavController and navigate with the bundle
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_cards_to_detail, bundle);

            } catch (Exception e) {
                // It's better to log the error or show a message
                Toast.makeText(getContext(), "Could not open card details.", Toast.LENGTH_SHORT).show();
            }
        });
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
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.errorContainer.setVisibility(View.GONE);
            }
        });

        viewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            // Logic to show a loading footer can be added here
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
