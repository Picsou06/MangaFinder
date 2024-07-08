package fr.picsou.animefinder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.picsou.animefinder.BookSearch.BookClass;
import fr.picsou.animefinder.BookSearch.BookLocalDatabase;
import fr.picsou.animefinder.BookSearch.BookSearchAdapter;
import fr.picsou.animefinder.BookSearch.ListAnimeAPI;

public class FinderFragment extends Fragment implements BookSearchAdapter.OnBookClickListener {

    private RecyclerView mRecyclerView;
    private BookSearchAdapter mAdapter;
    private List<BookClass> mBookList = new ArrayList<>();
    private EditText searchEditText;
    private ListAnimeAPI listAnimeAPI;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    public FinderFragment() {
        // Required empty public constructor
    }

    public static FinderFragment newInstance() {
        return new FinderFragment();
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

        // Initialisation du RecyclerView et de l'adaptateur comme avant
        mRecyclerView = view.findViewById(R.id.recycler_view_books);
        mAdapter = new BookSearchAdapter(GetCon, mBookList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        // Initialisation de ListAnimeAPI
        listAnimeAPI = new ListAnimeAPI(getContext(), mAdapter, mBookList);

        // Initialisation de l'EditText et du TextWatcher pour la recherche
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new SearchTextWatcher());

        executor.execute(() -> {
            long currentCount = BookLocalDatabase.getDatabase(requireContext()).bookDao().CountValue();
            handler.post(() -> listAnimeAPI.isupdated(currentCount));
        });
    }


    @Override
    public void onBookClick(BookClass book) {
        // Handle book click
        Toast.makeText(getContext(), "Clicked book: " + book.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void fetchAndStoreAllMangas() {
        executor.execute(() -> {
            List<BookClass> allBooks = listAnimeAPI.fetchAnimeListFromAPI();
            BookLocalDatabase.getDatabase(requireContext()).bookDao().insertBooks(allBooks);
            handler.post(() -> loadMangasFromDatabase(""));
        });
    }

    private void loadMangasFromDatabase(String searchText) {
        executor.execute(() -> {
            List<BookClass> books = BookLocalDatabase.getDatabase(requireContext()).bookDao().searchBooks("%" + searchText + "%");
            handler.post(() -> {
                mBookList.clear();
                mBookList.addAll(books);
                mAdapter.notifyDataSetChanged();
            });
        });
    }

    private class SearchTextWatcher implements TextWatcher {

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
            loadMangasFromDatabase(searchText);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
