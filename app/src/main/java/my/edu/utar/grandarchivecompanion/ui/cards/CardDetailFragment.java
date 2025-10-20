package my.edu.utar.grandarchivecompanion.ui.cards;

// ... other imports
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

// CORRECTED IMPORT: Use the binding class that matches your new layout file name
import my.edu.utar.grandarchivecompanion.databinding.FragmentCardDetailBinding;
import my.edu.utar.grandarchivecompanion.R;
import com.squareup.picasso.Picasso;
import io.noties.markwon.Markwon;

public class CardDetailFragment extends Fragment {

    // CORRECTED TYPE: Use the new binding class
    private FragmentCardDetailBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // CORRECTED INFLATION: Use the new binding class to inflate
        binding = FragmentCardDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CardItem card = null;
        if (getArguments() != null) {
            card = getArguments().getParcelable("selectedCard");
        }

        if (card == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        // Your existing logic remains the same
        Markwon markwon = Markwon.create(requireContext());
        markwon.setMarkdown(binding.cardNameDetail, card.getName());
        binding.cardTypeDetail.setText(card.getType());
        markwon.setMarkdown(binding.cardTextDetail, card.getText());
        markwon.setMarkdown(binding.cardRulingsDetail, card.getRulings());
        binding.cardLegalityDetail.setText(card.getLegality());

        if (card.isBanned()){
            binding.cardLegalityDetail.setTextColor(requireContext().getColor(R.color.banned_red));
        }

        String imageUrl = card.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(binding.cardImageDetail);
        } else {
            binding.cardImageDetail.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
