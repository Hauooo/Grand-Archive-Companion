package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private ProgressBar progressBar;
    private View errorContainer;
    private CardsViewModel viewModel;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        errorContainer = view.findViewById(R.id.error_container);
        Button retryButton = view.findViewById(R.id.button_retry);
        searchView = view.findViewById(R.id.search_view);

        adapter = new CardAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        retryButton.setOnClickListener(v -> viewModel.fetchCards());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CardsViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.setSearchQuery("");
                }
                return true;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                        viewModel.loadMoreCards();
                    }
                }
            }
        });
    }

    private void setupObservers() {
        // Observer for the INITIAL card list (fired only once on first load or refresh)
        viewModel.getCards().observe(getViewLifecycleOwner(), cardItems -> {
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setCards(cardItems); // Uses DiffUtil for a full refresh
        });

        // FIX: NEW observer for SUBSEQUENT pages of cards
        viewModel.getNewCardsPage().observe(getViewLifecycleOwner(), newCardPage -> {
            // This observer only fires when a new page is loaded.
            adapter.addCards(newCardPage); // Appends the new cards to the list
        });

        // Observer for the main loading spinner
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                recyclerView.setVisibility(View.GONE);
                errorContainer.setVisibility(View.GONE);
            }
        });

        // Observer for the pagination loading footer
        viewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            if (isLoadingMore) {
                adapter.showLoadingFooter();
            } else {
                adapter.hideLoadingFooter();
            }
        });

        // Observer for the error state
        viewModel.isError().observe(getViewLifecycleOwner(), isError -> {
            errorContainer.setVisibility(isError ? View.VISIBLE : View.GONE);
            if (isError) {
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}