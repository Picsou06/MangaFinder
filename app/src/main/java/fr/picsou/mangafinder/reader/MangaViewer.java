package fr.picsou.mangafinder.reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import fr.picsou.mangafinder.R;

public class MangaViewer extends AppCompatActivity {
    private static final String TAG = "MangaReaderActivity";
    private static final int IMAGES_PER_LOAD = 20;

    private List<Bitmap> images;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        Intent intent = getIntent();
        String mangaName = intent.getStringExtra("MANGA_NAME");
        String cbzFilePath = intent.getStringExtra("CBZ_FILE_PATH");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mangaName);

        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        images = new ArrayList<>();
        adapter = new ImageAdapter(this, images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadImages(cbzFilePath, 0, IMAGES_PER_LOAD);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !recyclerView.canScrollVertically(1)) {
                    loadImages(cbzFilePath, images.size(), images.size() + IMAGES_PER_LOAD);
                }
            }
        });
    }

    private void loadImages(String cbzFilePath, int startIndex, int endIndex) {
        isLoading = true;
        try {
            extractImages(cbzFilePath, startIndex, endIndex);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'extraction des images du fichier CBZ", e);
        }
    }

    private void extractImages(String cbzFilePath, int startIndex, int endIndex) throws IOException {
        try (InputStream fis = new FileInputStream(cbzFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze;
            int index = 0;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && index >= startIndex && index < endIndex) {
                    Bitmap bm = BitmapFactory.decodeStream(zis);
                    if (bm != null) {
                        images.add(bm);
                    }
                }
                index++;
                if (index >= endIndex) {
                    break; // On a chargé suffisamment d'images
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'ouverture du fichier CBZ.", e);
            throw e;
        }

        adapter.notifyDataSetChanged(); // Notifier l'adapter que les données ont changé
        isLoading = false; // Fin de la charge
    }
}
