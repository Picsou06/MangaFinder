package fr.picsou.mangafinder.Read.Mangas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import fr.picsou.mangafinder.BookLocalDatabase;
import fr.picsou.mangafinder.Download.Mangas.BookClass;
import fr.picsou.mangafinder.Download.Mangas.BookDownloaderAdapter;
import fr.picsou.mangafinder.R;
import fr.picsou.mangafinder.Read.Chapters.ChapitreReaderListActivity;
import fr.picsou.mangafinder.Read.Chapters.ChapterClass;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MangaReaderListFragment extends Fragment {

    private BookReaderAdapter bookAdapter;
    private TextView textViewSelectedFile;
    private static MangaReaderListFragment instance;
    private File selectedFile;
    private RecyclerView recyclerView;
    private List<BookReaderClass> bookList;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        view = inflater.inflate(R.layout.fragment_downloadedlist, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_books);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        TextView textViewEmpty = view.findViewById(R.id.text_view_empty);
        textViewSelectedFile = view.findViewById(R.id.text_view_selected_file);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bookList = getListOfBooks();

        if (bookList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            bookAdapter = new BookReaderAdapter(getContext(), bookList);
            recyclerView.setAdapter(bookAdapter);

            bookAdapter.setOnBookClickListener(this::openChapitreSelectorActivity);
        }
        swipeRefreshLayout.setOnRefreshListener(this::refreshBookListInternal);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String fileName = getFileNameFromUri(uri);
                    selectedFile = createFileFromUri(uri, fileName);
                    if (selectedFile != null) {
                        textViewSelectedFile.setText(selectedFile.getName());
                        textViewSelectedFile.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(), "Impossible de récupérer le fichier à partir de l'URI.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    private File createFileFromUri(Uri uri, String name) {
        File file = null;
        try {
            file = new File(requireActivity().getCacheDir(), name);
            copyUriToFile(uri, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void copyUriToFile(Uri uri, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file);
             FileInputStream inputStream = (FileInputStream) requireActivity().getContentResolver().openInputStream(uri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = Objects.requireNonNull(inputStream).read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static void refreshBookList() {
        if (instance != null) {
            instance.refreshBookListInternal();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshBookListInternal() {
        swipeRefreshLayout.setRefreshing(true);
        bookList = getListOfBooks();

        if (bookList.isEmpty()) {
            view.findViewById(R.id.text_view_empty).setVisibility(View.VISIBLE);
            view.findViewById(R.id.recycler_view_books).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.text_view_empty).setVisibility(View.GONE);
            view.findViewById(R.id.recycler_view_books).setVisibility(View.VISIBLE);

            bookAdapter.clearBooks();
            bookAdapter.updateBooks(bookList);

            bookAdapter.setOnBookClickListener(this::openChapitreSelectorActivity);
            bookAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void openChapitreSelectorActivity(BookReaderClass book) {
        Intent intent = new Intent(getActivity(), ChapitreReaderListActivity.class);
        intent.putExtra("cover", book.getImageCover());
        intent.putExtra("MangaName", book.getLanguage()+"-"+book.getTitle());
        startActivity(intent);
    }

    private List<BookReaderClass> getListOfBooks() {
        List<BookReaderClass> bookList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            BookLocalDatabase db = BookLocalDatabase.getDatabase(getContext());

            List<ChapterClass> chapterList = db.chapterDao().getAllChapters();

            for (ChapterClass chapter : chapterList) {
                if (db.bookDao().getBookById(chapter.getBookId()) == null) {
                    db.chapterDao().deleteChapters(chapter.getBookId());
                    continue;
                }
                bookList.add(db.bookDao().getBookById(chapter.getBookId()));
            }
            for (BookReaderClass book : bookList) {
                File mangaDir = new File(getContext().getFilesDir(), "MangaFinder/" + book.getLanguage() + "-" + book.getTitle());
                System.out.println(mangaDir.getAbsolutePath());
                File cover = new File(mangaDir, "cover.jpg");
                if (cover.exists()) {
                    book.setImageCover(cover.getAbsolutePath());
                }
            }
            latch.countDown();
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bookList;
    }
}
