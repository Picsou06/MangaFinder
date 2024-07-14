package fr.picsou.mangafinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.picsou.mangafinder.BookSearch.BookClass;
import fr.picsou.mangafinder.BookSearch.BookLocalDatabase;
import fr.picsou.mangafinder.BookSearch.BookDownloaderAdapter;
import fr.picsou.mangafinder.BookSearch.BookSearchAdapter;
import fr.picsou.mangafinder.BookSearch.ListAnimeAPI;

public class FinderFragment extends Fragment implements BookDownloaderAdapter.OnBookClickListener, BookSearchAdapter.OnBookClickListener {
    private ListAnimeAPI listAnimeAPI;
    private BookDownloaderAdapter mAdapter;
    private BookSearchAdapter searchAdapter;
    private EditText searchEditText;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 25;
    private List<String> language = new ArrayList<>(List.of("en", "fr"));
    private boolean isFrenchSelected = true;
    private boolean isEnglishSelected = true;
    private ImageButton frenchButton, englishButton;

    public FinderFragment() {
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
        searchAdapter = new BookSearchAdapter(getContext(), searchBookList);
        mAdapter.setOnBookClickListener(this);
        searchAdapter.setOnBookClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize ListAnimeAPI and fetch the anime list
        listAnimeAPI = new ListAnimeAPI(getContext(), mAdapter, searchAdapter);
        AsyncTask.execute(() -> {
            long count = BookLocalDatabase.getDatabase(getContext()).bookDao().CountValue();
            listAnimeAPI.updateDatabase(count);
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
            getActivity().runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            loadMoreData();
        });
    }

    private void updatelocalData(){
        currentPage=0;
        mAdapter.clearBooks();
        listAnimeAPI.fetchBooksFromDatabase(PAGE_SIZE, 0, mAdapter, language);
        mAdapter.notifyDataSetChanged();
    }

    private void loadMoreData() {
        int offset = currentPage * PAGE_SIZE;
        listAnimeAPI.fetchBooksFromDatabase(PAGE_SIZE, offset, mAdapter, language);
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

    public void onBookClick(BookClass book) {
        Intent intent = new Intent(getActivity(), ChapitreFinderSelectorActivity.class);
        intent.putExtra("cover", book.getImageUrl());
        intent.putExtra("animeName", book.getTitle());
        intent.putExtra("id", book.getId());
        intent.putExtra("language", book.getLanguage());
        startActivity(intent);
    }
}
