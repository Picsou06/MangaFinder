package fr.picsou.animefinder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private static final String SEARCH_KEY = "search_key";
    private static final String PAGE_KEY = "page_key";
    private ListAnimeAPI listAnimeAPI;

    public FinderFragment() {
        // Required empty public constructor
    }

    public static FinderFragment newInstance(String currentSearch, Integer currentPage) {
        FinderFragment fragment = new FinderFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_KEY, currentSearch);
        args.putInt(PAGE_KEY, currentPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String search = getArguments().getString(SEARCH_KEY);
            int page = getArguments().getInt(PAGE_KEY);
        }
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
        listAnimeAPI.fetchAnimeList();
    }

    @Override
    public void onBookClick(BookClass book) {
        System.out.println(book.getId());
    }
}
