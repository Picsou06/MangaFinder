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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        // Initialiser l'exécuteur pour le chargement en arrière-plan
        executorService = Executors.newFixedThreadPool(2);

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

        loadImages(cbzFilePath, 0, 10);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((totalItemCount - visibleItemCount - firstVisibleItemPosition) <= 10) {
                        loadImages(cbzFilePath, images.size(), images.size() + IMAGES_PER_LOAD);
                    }
                }
            }
        });
    }

    private void loadImages(String cbzFilePath, int startIndex, int endIndex) {
        isLoading = true;

        executorService.execute(() -> {
            try {
                if (hasMoreImages(cbzFilePath, startIndex)) {
                    extractImages(cbzFilePath, startIndex, endIndex);
                } else {
                    runOnUiThread(() -> isLoading = false);
                }
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'extraction des images du fichier CBZ", e);
                runOnUiThread(() -> isLoading = false);
            }
        });
    }

    private boolean hasMoreImages(String cbzFilePath, int startIndex) {
        try (InputStream fis = new FileInputStream(cbzFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry ze;
            int index = 0;

            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && index >= startIndex) {
                    return true;
                }
                index++;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la vérification des pages restantes", e);
        }
        return false;
    }

    private Bitmap decodeSampledBitmapFromStream(InputStream inputStream, int reqWidth, int reqHeight) {
        try {
            byte[] imageData = toByteArray(inputStream);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du décodage de l'image", e);
            return null;
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void extractImages(String cbzFilePath, int startIndex, int endIndex) throws IOException {
        List<Bitmap> newImages = new ArrayList<>();
        try (InputStream fis = new FileInputStream(cbzFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry ze;
            int index = 0;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && index >= startIndex && index < endIndex) {
                    Bitmap bm = decodeSampledBitmapFromStream(zis, 800, 1200);
                    if (bm != null) {
                        newImages.add(bm);
                    }
                }
                index++;
                if (index >= endIndex) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'ouverture du fichier CBZ.", e);
            throw e;
        }

        // Mettre à jour les images dans le thread principal
        runOnUiThread(() -> {
            images.addAll(newImages);
            adapter.notifyDataSetChanged();
            isLoading = false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        for (Bitmap bitmap : images) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        images.clear();
    }
}
