package fr.picsou.animefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.picsou.animefinder.BookRead.BookReaderAdapter;
import fr.picsou.animefinder.BookRead.BookReaderClass;

public class DownloadedListFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookReaderAdapter bookAdapter;
    private TextView textViewEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloadedlist, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_books);
        textViewEmpty = view.findViewById(R.id.text_view_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<BookReaderClass> bookList = getListOfBooks();

        if (bookList == null || bookList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            bookAdapter = new BookReaderAdapter(getContext(), bookList);
            recyclerView.setAdapter(bookAdapter);

            bookAdapter.setOnBookClickListener(new BookReaderAdapter.OnBookClickListener() {
                @Override
                public void onBookClick(BookReaderClass book) {
                    openChapitreSelectorActivity(book);
                }
            });
        }

        return view;
    }

    private void openChapitreSelectorActivity(BookReaderClass book) {
        Intent intent = new Intent(getActivity(), ChapitreReaderSelectorActivity.class);
        intent.putExtra("cover", book.getImageCover());
        intent.putExtra("animeName", book.getTitle());
        startActivity(intent);
    }

    private List<BookReaderClass> getListOfBooks() {
        List<BookReaderClass> bookList = new ArrayList<>();

        File animeFinderDir = new File(getActivity().getFilesDir(), "AnimeFinder");
        if (animeFinderDir.exists() && animeFinderDir.isDirectory()) {
            File[] animeFolders = animeFinderDir.listFiles(File::isDirectory);

            if (animeFolders != null) {
                for (File folder : animeFolders) {
                    String title = folder.getName();
                    String imageCoverPath = folder.getAbsolutePath() + File.separator + "cover.jpg";
                    String numberOfPages = String.valueOf(folder.listFiles().length - 1);

                    BookReaderClass book = new BookReaderClass(title, imageCoverPath, numberOfPages);
                    bookList.add(book);
                }
            }
        }

        return bookList;
    }
}
