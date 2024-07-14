package fr.picsou.animefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import fr.picsou.animefinder.BookRead.BookReaderAdapter;
import fr.picsou.animefinder.BookRead.BookReaderClass;
import fr.picsou.animefinder.BookSearch.BookClass;
import fr.picsou.animefinder.BookSearch.BookLocalDatabase;
import fr.picsou.animefinder.BookSearch.BookSearchAdapter;

public class DownloadedListFragment extends Fragment {

    private BookReaderAdapter bookAdapter;
    private LinearLayout importMenuLayout;
    private EditText editTextAnimeName;
    private TextView textViewSelectedFile;
    private static DownloadedListFragment instance;
    private File selectedFile;
    private RecyclerView recyclerView; // Declare the RecyclerView here
    private List<BookReaderClass> bookList;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        view = inflater.inflate(R.layout.fragment_downloadedlist, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_books); // Initialize the RecyclerView
        TextView textViewEmpty = view.findViewById(R.id.text_view_empty);
        ImageButton btnOpenAnimeFinder = view.findViewById(R.id.btn_open_anime_finder);
        importMenuLayout = view.findViewById(R.id.import_menu_layout);
        editTextAnimeName = view.findViewById(R.id.edit_text_anime_name);
        Button btnChooseFile = view.findViewById(R.id.btn_choose_file);
        textViewSelectedFile = view.findViewById(R.id.text_view_selected_file);
        Button btnImport = view.findViewById(R.id.btn_import);

        btnOpenAnimeFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImportMenu();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bookList = getListOfBooks();

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

        btnChooseFile.setOnClickListener(this::onChooseFileClick);
        btnImport.setOnClickListener(this::onImportClick);

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
            cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
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
            file = new File(getActivity().getCacheDir(), name);
            copyUriToFile(uri, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void copyUriToFile(Uri uri, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file);
             FileInputStream inputStream = (FileInputStream) getActivity().getContentResolver().openInputStream(uri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

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

        File animeFolder = new File(getActivity().getFilesDir(), "AnimeFinder/" + animeName);
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

        // Thread pour télécharger l'image de couverture
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
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Image de couverture téléchargée et sauvegardée.", Toast.LENGTH_SHORT).show();
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    // Retourner sur le thread principal pour afficher un Toast
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur lors du téléchargement de l'image de couverture.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            System.out.println("HELPER, image ajoutée!");
        });

        // Démarrer le thread de téléchargement
        downloadThread.start();

        try {
            // Attendre que le thread de téléchargement soit terminé
            downloadThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Suite du code après que le téléchargement soit terminé
        System.out.println("HELPER, fichier importé!");
        refreshBookListInternal();
    }


    private static class GetPictureAsyncTask extends AsyncTask<String, Void, String> {

        private WeakReference<Context> contextRef;

        GetPictureAsyncTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params) {
            Context context = contextRef.get();
            if (context == null) return null;

            String animeName = params[0];
            return BookLocalDatabase.getDatabase(context).bookDao().getPicture(animeName);
        }
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

    private void refreshBookListInternal() {
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

            bookAdapter.setOnBookClickListener(new BookReaderAdapter.OnBookClickListener() {
                @Override
                public void onBookClick(BookReaderClass book) {
                    openChapitreSelectorActivity(book);
                }
            });
        }
        System.out.println("HELPER, Folder Updated");

        List<BookReaderClass> bookList = getListOfBooks();
        System.out.println("HELPER, " + bookList.toString());

        if (bookAdapter != null) {
            bookAdapter.clearBooks();
            bookAdapter.updateBooks(bookList);
            bookAdapter.notifyDataSetChanged();
        } else {
            bookAdapter = new BookReaderAdapter(getContext(), bookList);
            recyclerView.setAdapter(bookAdapter); // Set the adapter here if it's not set
        }
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

                    // Check if cover.jpg exists
                    File coverFile = new File(imageCoverPath);
                    if (!coverFile.exists()) {
                        imageCoverPath = null;
                    }

                    int numberOfPages = getNumberOfPages(folder);

                    BookReaderClass book = new BookReaderClass(title, imageCoverPath, String.valueOf(numberOfPages));
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
