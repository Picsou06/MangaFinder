package fr.picsou.animefinder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
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
    private ImageButton btnOpenAnimeFinder; // Déclaration du ImageButton

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloadedlist, container, false);

        // Initialisation des vues
        recyclerView = view.findViewById(R.id.recycler_view_books);
        textViewEmpty = view.findViewById(R.id.text_view_empty);
        btnOpenAnimeFinder = view.findViewById(R.id.btn_open_anime_finder); // Initialisation du ImageButton

        // Configurer le RecyclerView et afficher la liste des livres
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

        // Gestion du clic sur le ImageButton pour ouvrir AnimeFinder
        btnOpenAnimeFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnimeFinderFolder();
            }
        });

        return view;
    }

    private void openChapitreSelectorActivity(BookReaderClass book) {
        Intent intent = new Intent(getActivity(), ChapitreReaderSelectorActivity.class);
        intent.putExtra("cover", book.getImageCover());
        intent.putExtra("animeName", book.getTitle());
        startActivity(intent);
    }

    private void openAnimeFinderFolder() {
        File animeFinderDir = new File(getActivity().getFilesDir(), "AnimeFinder");

        if (animeFinderDir.exists() && animeFinderDir.isDirectory()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            Uri uri = Uri.parse(animeFinderDir.getAbsolutePath());
            intent.setDataAndType(uri, "*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "Aucune application pour ouvrir le dossier AnimeFinder.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Le dossier AnimeFinder n'existe pas.", Toast.LENGTH_SHORT).show();
        }
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
