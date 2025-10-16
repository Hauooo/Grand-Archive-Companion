package my.edu.utar.grandarchivecompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import my.edu.utar.grandarchivecompanion.databinding.FragmentCardsBinding; // 1. Import View Binding

public class CardsFragment extends Fragment {

    // 2. Declare a single binding variable
    private FragmentCardsBinding binding;
    private CardAdapter adapter;
    private CardsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 3. Inflate the layout using View Binding
        binding = FragmentCardsBinding.inflate(inflater, container, false);

        // 4. Initialize the adapter with the required click listener
        adapter = new CardAdapter(card -> {
            Intent intent = new Intent(requireContext(), CardDetailActivity.class);
            // Pass the entire Parcelable object
            intent.putExtra(CardDetailActivity.EXTRA_CARD_ITEM, card);
            startActivity(intent);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        // Access views through the binding object
        binding.buttonRetry.setOnClickListener(v -> viewModel.fetchCards());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CardsViewModel.class);

        setupSetSpinner(); // 1. Call the new setup method
        setupObservers();
        setupListeners();
    }

    // 5. Add onDestroyView to prevent memory leaks with View Binding in fragments
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupSetSpinner() {
        // In a real app, you would fetch this list from an API.
        // For now, we'll hardcode the sets and their prefixes.
        final List<String> setNames = new ArrayList<>(Arrays.asList(
                "All Sets", "Demo 2022", "Promo 2022", "Dawn of Ashes (1st Ed.)",
                "Dawn of Ashes (Prelude)", "Kickstarter Promo", "GeSeCha", "Event Packs",
                "Proxia's Vault", "Dawn of Ashes (Alter)", "DOA Starter Deck", "Supporter Pack 1",
                "Promo 2023", "Demo 2023", "Fractured Crown", "Fractured Crown: Armaments",
                "Promo 2024", "Alchemical Revolution", "Alchemical Revolution (1st Ed.)",
                "ALC Starter Deck", "SquareLive Collaboration", "Mercurial Heart", "Mercurial Heart (1st Ed.)",
                "Re:Collection - Shadowdancer", "Re:Collection - Slime Sovereign", "Supporter Pack 2",
                "Mortal Ambition", "AMB (1st Ed.)", "AMB Starter Deck",
                "AMB Draft Pack", "Alchemical Revolution (Alter)", "Promo 2025",
                "Abyssal Heaven", "Abyssal Heaven (1st Ed.)", "Re:Collection - Heaven's Favoured",
                "Re:Collection - Idyll Corsage", "Mercurial Heart (Alter)", "Supporter Pack 3",
                "Distorted Reflections", "DTR Starter Deck", "Distorted Reflections (1st Ed.)"
        ));

        // Create the corresponding list of API prefixes. This MUST be in the same order.
        final List<String> setPrefixes = new ArrayList<>(Arrays.asList(
                "", "DEMO22", "P22", "DOA 1st", "DOAp", "KSP", "GSC", "EVP",
                "PRXY", "DOA Alter", "DOASD", "SP1", "P23", "DEMO23", "FTC", "FTCA",
                "P24", "ALC", "ALC 1st", "ALCSD", "SLC", "MRC", "MRC 1st",
                "ReC-SHD", "ReC-SLM", "SP2", "AMB", "AMB 1st", "AMBSD", "AMBDP",
                "ALC Alter", "P25", "HVN", "HVN 1st", "ReC-HVF", "ReC-IDY",
                "MRC Alter", "SP3", "DTR", "DTRSD", "DTR 1st"
        ));

        // Create an adapter for the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, setNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.setSpinner.setAdapter(spinnerAdapter);

        // Set the listener for when an item is selected
        binding.setSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the prefix corresponding to the selected set name
                String selectedPrefix = setPrefixes.get(position);
                // Tell the ViewModel about the change
                viewModel.setSetPrefix(selectedPrefix);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupListeners() {
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

        // --- REVISED SCROLL LISTENER ---
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();

                // Check if layoutManager is not null to prevent crashes
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Get the current loading state from the ViewModel
                Boolean isLoadingMore = viewModel.isLoadingMore().getValue();

                // The check for null is crucial because LiveData can be null initially
                if (isLoadingMore != null && isLoadingMore) {
                    return; // Do nothing if already loading more
                }

                // Load more if we are near the end of the list
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    viewModel.loadMoreCards();
                }
            }
        });
    }

    // In CardsFragment.java

// ... inside the setupObservers() method

    private void setupObservers() {
        // This is now the ONLY observer needed for the list.
        // It will be triggered for page 1 AND all subsequent pages.
        viewModel.getCards().observe(getViewLifecycleOwner(), cardItems -> {
            if (cardItems != null) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                // Submit the full, updated list. ListAdapter handles the animations.
                adapter.submitList(cardItems);
            }
        });

        // --- DELETE the observer for getNewCardsPage ---
        // viewModel.getNewCardsPage().observe(...) // DELETE THIS WHOLE BLOCK

        // The rest of the observers for isLoading, isLoadingMore, and isError are correct
        // and can remain the same. Just ensure they use the 'binding' object.

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.recyclerView.setVisibility(View.GONE);
                binding.errorContainer.setVisibility(View.GONE);
            }
        });

        // This can still be useful to show/hide a loading footer if you add one.
        viewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            // Here you would add logic to show/hide a footer in your adapter
            // For now, we can leave it empty or log a message.
        });

        viewModel.isError().observe(getViewLifecycleOwner(), isError -> {
            binding.errorContainer.setVisibility(isError ? View.VISIBLE : View.GONE);
            if (isError) {
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
    }
}