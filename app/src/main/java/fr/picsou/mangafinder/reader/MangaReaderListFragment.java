package fr.picsou.mangafinder.reader;

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

import fr.picsou.mangafinder.downloader.BookLocalDatabase;
import fr.picsou.mangafinder.R;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MangaReaderListFragment extends Fragment {

    private BookReaderAdapter bookAdapter;
    private LinearLayout importMenuLayout;
    private EditText editTextAnimeName;
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
        ImageButton btnOpenMangaFinder = view.findViewById(R.id.btn_open_anime_finder);
        importMenuLayout = view.findViewById(R.id.import_menu_layout);
        editTextAnimeName = view.findViewById(R.id.edit_text_anime_name);
        Button btnChooseFile = view.findViewById(R.id.btn_choose_file);
        textViewSelectedFile = view.findViewById(R.id.text_view_selected_file);
        Button btnImport = view.findViewById(R.id.btn_import);

        btnOpenMangaFinder.setOnClickListener(v -> toggleImportMenu());

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

        btnChooseFile.setOnClickListener(this::onChooseFileClick);
        btnImport.setOnClickListener(this::onImportClick);

        // Configure swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshBookListInternal);

        return view;
    }


    private void toggleImportMenu() {
        if (importMenuLayout.getVisibility() == View.VISIBLE) {
            importMenuLayout.setVisibility(View.GONE);
        } else {
            refreshBookList();
            importMenuLayout.setVisibility(View.VISIBLE);
            importMenuLayout.bringToFront();
        }
    }

    public void onChooseFileClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
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

    @SuppressLint("SetTextI18n")
    public void onImportClick(View view) {
        toggleImportMenu();
        String animeName = editTextAnimeName.getText().toString().trim();

        if (animeName.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez saisir le nom de l'anime.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFile == null || !selectedFile.exists()) {
            Toast.makeText(getContext(), "Veuillez sélectionner un fichier .cbz valide.", Toast.LENGTH_SHORT).show();
            return;
        }

        File animeFolder = new File(requireActivity().getFilesDir(), "MangaFinder/" + animeName);
        if (!animeFolder.exists()) {
            animeFolder.mkdirs();
        }

        File destinationFile = new File(animeFolder, selectedFile.getName());
        try {
            copyFile(selectedFile, destinationFile);
            Toast.makeText(getContext(), "Fichier importé avec succès.", Toast.LENGTH_SHORT).show();
            textViewSelectedFile.setText("Aucun fichier sélectionné");
            textViewSelectedFile.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur lors de l'importation du fichier.", Toast.LENGTH_SHORT).show();
        }

        Thread downloadThread = new Thread(() -> {
            String imageCoverLink = BookLocalDatabase.getDatabase(getContext()).bookDao().getPicture(animeName);

            if (imageCoverLink != null) {
                try {
                    URL url = new URL(imageCoverLink);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    File coverFile = new File(animeFolder, "cover.jpg");

                    FileOutputStream outputStream = new FileOutputStream(coverFile);
                    InputStream inputStream = connection.getInputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    // Retourner sur le thread principal pour afficher un Toast
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Image de couverture téléchargée et sauvegardée.", Toast.LENGTH_SHORT).show());

                } catch (IOException e) {
                    e.printStackTrace();
                    // Retourner sur le thread principal pour afficher un Toast
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Erreur lors du téléchargement de l'image de couverture.", Toast.LENGTH_SHORT).show());
                }
            }
        });

        downloadThread.start();

        try {
            downloadThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        refreshBookListInternal();
    }


    private void copyFile(File sourceFile, File destFile) throws IOException {
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
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
        TextView textViewEmpty = view.findViewById(R.id.text_view_empty);
        if (bookList == null || bookList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            bookAdapter = new BookReaderAdapter(getContext(), bookList);
            recyclerView.setAdapter(bookAdapter);

            bookAdapter.setOnBookClickListener(this::openChapitreSelectorActivity);
        }

        List<BookReaderClass> bookList = getListOfBooks();
        if (bookAdapter != null) {
            bookAdapter.clearBooks();
            bookAdapter.updateBooks(bookList);
            bookAdapter.notifyDataSetChanged();
        } else {
            bookAdapter = new BookReaderAdapter(getContext(), bookList);
            recyclerView.setAdapter(bookAdapter);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void openChapitreSelectorActivity(BookReaderClass book) {
        Intent intent = new Intent(getActivity(), ChapitreReaderListActivity.class);
        intent.putExtra("cover", book.getImageCover());
        intent.putExtra("animeName", book.getLanguage()+"-"+book.getTitle());
        startActivity(intent);
    }

    private List<BookReaderClass> getListOfBooks() {
        List<BookReaderClass> bookList = new ArrayList<>();

        File MangaFinderDir = new File(requireActivity().getFilesDir(), "MangaFinder");
        if (MangaFinderDir.exists() && MangaFinderDir.isDirectory()) {
            File[] animeFolders = MangaFinderDir.listFiles(File::isDirectory);

            if (animeFolders != null) {
                for (File folder : animeFolders) {
                    String title = folder.getName();
                    String imageCoverPath = folder.getAbsolutePath() + File.separator + "cover.jpg";

                    String[] titleParts = title.split("-", 2);
                    String language = (titleParts.length > 1) ? titleParts[0].trim() : "Unknown";
                    String actualTitle = (titleParts.length > 1) ? titleParts[1].trim() : title;

                    // Check if cover.jpg exists
                    File coverFile = new File(imageCoverPath);
                    if (!coverFile.exists()) {
                        imageCoverPath = null;
                    }

                    int numberOfPages = getNumberOfPages(folder);

                    BookReaderClass book = new BookReaderClass(actualTitle, imageCoverPath, String.valueOf(numberOfPages), language);
                    bookList.add(book);
                }
            }
        }

        return bookList;
    }


    private int getNumberOfPages(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            return files.length - 1; // Assuming cover.jpg is not counted as a page
        }
        return 0;
    }
}
