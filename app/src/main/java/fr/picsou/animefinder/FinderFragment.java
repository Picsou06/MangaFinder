package fr.picsou.animefinder;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import fr.picsou.animefinder.BookSearch.BookClass;
import fr.picsou.animefinder.BookSearch.BookSearchAdapter;
import fr.picsou.animefinder.BookSearch.ListAnimeAPI;

public class FinderFragment extends Fragment implements BookSearchAdapter.OnBookClickListener {
    private RecyclerView mRecyclerView;
    private BookSearchAdapter mAdapter;
    private List<BookClass> mBookList;
    private BottomNavigationView bottomNavigationView;
    private ListAnimeAPI listAnimeAPI;
    private EditText searchEditText;
    private ImageButton searchButton;

    public FinderFragment() {
        // Required empty public constructor
    }

    public static FinderFragment newInstance(String currentSearch, Integer currentPage) {
        FinderFragment fragment = new FinderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mRecyclerView = view.findViewById(R.id.recycler_view_books);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBookList = new ArrayList<>();
        mAdapter = new BookSearchAdapter(getContext(), mBookList);
        mAdapter.setOnBookClickListener(this); // Set the click listener
        mRecyclerView.setAdapter(mAdapter);

        // Initialize ListAnimeAPI and fetch the anime list
        listAnimeAPI = new ListAnimeAPI(getContext(), mAdapter, mBookList);
        listAnimeAPI.fetchAnimeListFromAPI(500);

        // Initialize search components
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);

        // Set click listener on the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(searchText)) {
                    listAnimeAPI.fetchAnimeTitleFromAPI(searchText);
                }
            }
        });

        // Set focus change listener on the search EditText
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String searchText = searchEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(searchText)) {
                        listAnimeAPI.fetchAnimeListFromAPI(500);
                    }
                }
            }
        });
    }

    @Override
    public void onBookClick(BookClass book) {
        System.out.println(book.getId());
    }
}
