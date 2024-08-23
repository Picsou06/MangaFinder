// DownloaderFragment.java

package fr.picsou.mangafinder.downloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import fr.picsou.mangafinder.R;
import fr.picsou.mangafinder.SettingsActivity;

public class DownloaderFragment extends Fragment implements BookDownloaderAdapter.OnBookClickListener {
    private ListAnimeAPI listAnimeAPI;
    private BookDownloaderAdapter mAdapter;
    private BookDownloaderAdapter searchAdapter;
    private EditText searchEditText;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 25;
    private List<String> language = new ArrayList<>(List.of("en", "fr"));
    private boolean isFrenchSelected = true;
    private boolean isEnglishSelected = true;
    private ImageButton frenchButton, englishButton;
    private TextView noMangaMessage;
    private Button changeApiButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    int port;

    public DownloaderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = view.findViewById(R.id.recycler_view_books);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<BookClass> mBookList = new ArrayList<>();
        List<BookClass> searchBookList = new ArrayList<>();
        mAdapter = new BookDownloaderAdapter(getContext(), mBookList);
        searchAdapter = new BookDownloaderAdapter(getContext(), searchBookList);
        mAdapter.setOnBookClickListener(this);
        searchAdapter.setOnBookClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload data when user performs swipe-to-refresh
            loadInitialData();
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String serverUrl = sharedPreferences.getString("server_url", "default_url");
        String portString = sharedPreferences.getString("server_port", "3000");
        if (!portString.isEmpty()) {
            port = Integer.parseInt(portString);
        } else {
            port = 3000;
        }
        listAnimeAPI = new ListAnimeAPI(getContext(), mAdapter, searchAdapter, serverUrl, port);
        AsyncTask.execute(() -> {
            long count = BookLocalDatabase.getDatabase(getContext()).bookDao().CountValue();
            listAnimeAPI.updateDatabase(count);
            getActivity().runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() == 0) {
                    showNoMangaMessage();
                } else {
                    hideNoMangaMessage();
                }
                loadMoreData();
            });
        });

        // Setup the EditText and TextWatcher
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    switchToSearchAdapter();
                    listAnimeAPI.searchBooksFromDatabase(searchText, language);
                } else {
                    switchToMainAdapter();
                    loadInitialData();
                }
            }
        });

        // Setup the flag buttons
        frenchButton = view.findViewById(R.id.french_button);
        englishButton = view.findViewById(R.id.english_button);

        frenchButton.setOnClickListener(v -> {
            isFrenchSelected = !isFrenchSelected;
            frenchButton.setImageResource(isFrenchSelected ? R.drawable.french_on : R.drawable.french_off);
            updateLanguageList();
        });

        englishButton.setOnClickListener(v -> {
            isEnglishSelected = !isEnglishSelected;
            englishButton.setImageResource(isEnglishSelected ? R.drawable.english_on : R.drawable.english_off);
            updateLanguageList();
        });

        // Setup the settings button
        ImageButton settingsButton = view.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        // Setup the no manga message and change API button
        noMangaMessage = view.findViewById(R.id.no_manga_message);
        changeApiButton = view.findViewById(R.id.change_api_button);
        changeApiButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (totalItemCount <= (lastVisibleItem + PAGE_SIZE)) {
                    loadMoreData();
                }
            }
        });

        updatelocalData();
    }

    private void loadInitialData() {
        AsyncTask.execute(() -> {
            long totalBooks = BookLocalDatabase.getDatabase(getContext()).bookDao().CountValue();
            listAnimeAPI.updateDatabase(totalBooks);
            getActivity().runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() == 0) {
                    showNoMangaMessage();
                } else {
                    hideNoMangaMessage();
                }
                loadMoreData();
                swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
            });
        });
    }

    private void updatelocalData() {
        currentPage = 0;
        mAdapter.clearBooks();
        listAnimeAPI.fetchBooksFromDatabase(PAGE_SIZE, 0, mAdapter, language);
        getActivity().runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() == 0) {
                showNoMangaMessage();
            } else {
                hideNoMangaMessage();
            }
        });
    }

    private void loadMoreData() {
        int offset = currentPage * PAGE_SIZE;
        listAnimeAPI.fetchBooksFromDatabase(PAGE_SIZE, offset, mAdapter, language);
        getActivity().runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() == 0) {
                showNoMangaMessage();
            } else {
                hideNoMangaMessage();
            }
        });
        currentPage++;
    }

    private void switchToMainAdapter() {
        RecyclerView mRecyclerView = getView().findViewById(R.id.recycler_view_books);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void switchToSearchAdapter() {
        RecyclerView mRecyclerView = getView().findViewById(R.id.recycler_view_books);
        mRecyclerView.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();
    }

    private void updateLanguageList() {
        hideNoMangaMessage();
        language.clear();
        if (isFrenchSelected) {
            language.add("fr");
        }
        if (isEnglishSelected) {
            language.add("en");
        }
        String searchText = searchEditText.getText().toString().trim();
        if (!searchText.isEmpty()) {
            switchToSearchAdapter();
            listAnimeAPI.searchBooksFromDatabase(searchText, language);
        } else {
            switchToMainAdapter();
            loadInitialData();
        }
        updatelocalData();
        mAdapter.notifyDataSetChanged();
        searchAdapter.notifyDataSetChanged();
    }

    private void showNoMangaMessage() {
        noMangaMessage.setVisibility(View.VISIBLE);
        changeApiButton.setVisibility(View.VISIBLE);
    }

    private void hideNoMangaMessage() {
        noMangaMessage.setVisibility(View.GONE);
        changeApiButton.setVisibility(View.GONE);
    }

    public void onBookClick(BookClass book) {
        Intent intent = new Intent(getActivity(), ChapitreDownloaderActivity.class);
        intent.putExtra("cover", book.getImageUrl());
        intent.putExtra("MangaName", book.getTitle());
        intent.putExtra("id", book.getId());
        intent.putExtra("language", book.getLanguage());
        startActivity(intent);
    }
}