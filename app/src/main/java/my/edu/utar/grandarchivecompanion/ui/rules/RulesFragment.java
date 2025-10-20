package my.edu.utar.grandarchivecompanion.ui.rules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import my.edu.utar.grandarchivecompanion.R;

public class RulesFragment extends Fragment {

    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The URL for the comprehensive rules
        final String rulesUrl = "https://rules.gatcg.com/";

        // Find the WebView by its ID from the layout
        webView = view.findViewById(R.id.rules_webview);

        // --- Configure the WebView ---

        // 1. Enable JavaScript (many modern websites require it)
        webView.getSettings().setJavaScriptEnabled(true);

        // 2. Set a WebViewClient. This is CRUCIAL.
        // It forces links to open inside your WebView instead of an external browser.
        webView.setWebViewClient(new WebViewClient());

        // Load the URL into the WebView
        webView.loadUrl(rulesUrl);

        // --- Handle Back Button for Web Navigation ---
        // This makes the back button go to the previous web page instead of the previous fragment.
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    // If the WebView can navigate back in its history, do that
                    webView.goBack();
                } else {
                    // Otherwise, disable this callback and let the default back press action happen
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }
}