package fr.picsou.animefinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.lifecycleScope;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.picsou.animefinder.BookSearch.BookClass;
import fr.picsou.animefinder.BookSearch.BookLocalDatabase;
import fr.picsou.animefinder.BookSearch.BookSearchAdapter;
import fr.picsou.animefinder.BookSearch.ListAnimeAPI;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.launch;

public class FinderFragment extends Fragment implements BookSearchAdapter.OnBookClickListener {
    private RecyclerView mRecyclerView;
    private BookSearchAdapter mAdapter;
    private List<BookClass> mBookList;
    private ListAnimeAPI listAnimeAPI;
    private EditText searchEditText;

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
        BookSearchAdapter mAdapter = new BookSearchAdapter(getContext(), mBookList);
        mAdapter.setOnBookClickListener(this); // Set the click listener
        mRecyclerView.setAdapter(mAdapter);

        // Initialize ListAnimeAPI and fetch the anime list
        listAnimeAPI = new ListAnimeAPI(getContext(), mAdapter);

        // Launch coroutine using lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            listAnimeAPI.updateDatabase(BookLocalDatabase.getDatabase(getContext()).bookDao().CountValue()); // Fetch data from the database
        };

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
                    listAnimeAPI.searchBooksFromDatabase(searchText);
                }
            }
        });

        // Set focus change listener on the search EditText
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String searchText = searchEditText.getText().toString().trim();
                if (TextUtils.isEmpty(searchText)) {
                    // Launch coroutine to fetch all data from the database
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        listAnimeAPI.fetchBooksFromDatabase();
                    };
                }
            }
        });
    }

    @Override
    public void onBookClick(BookClass book) {
        System.out.println(book.getId());
    }
}
