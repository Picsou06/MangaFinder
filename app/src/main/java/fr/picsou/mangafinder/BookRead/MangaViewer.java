package fr.picsou.mangafinder.BookRead;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import fr.picsou.mangafinder.R;

public class MangaViewer extends AppCompatActivity {

    private static final String TAG = "MangaReaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        Intent intent = getIntent();
        String mangaName = intent.getStringExtra("MANGA_NAME");
        String cbzFilePath = intent.getStringExtra("CBZ_FILE_PATH");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mangaName);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        LinearLayout imageContainer = findViewById(R.id.image_container);

        // Ajout d'une vérification pour voir si le fichier existe
        File cbzFile = new File(cbzFilePath);
        if (!cbzFile.exists()) {
            Log.e(TAG, "Le fichier CBZ n'existe pas : " + cbzFilePath);
            return;
        } else {
            Log.d(TAG, "Le fichier CBZ existe : " + cbzFilePath);
        }

        try {
            extractAndDisplayImages(cbzFilePath, imageContainer);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'extraction des images du fichier CBZ", e);
        }
    }

    private void extractAndDisplayImages(String cbzFilePath, LinearLayout imageContainer) throws IOException {
        Log.d(TAG, "Ouverture du fichier CBZ : " + cbzFilePath);

        try (InputStream fis = new FileInputStream(cbzFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            Log.d(TAG, "Fichier CBZ ouvert avec succès.");

            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory()) {
                    Log.d(TAG, "Extraction de l'entrée : " + ze.getName());
                    Bitmap bm = BitmapFactory.decodeStream(zis);

                    if (bm != null) {
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bm);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setAdjustViewBounds(true);

                        // Paramètres de disposition pour éviter l'espace entre les images
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 0, 0, 0); // Marges à zéro
                        imageView.setLayoutParams(layoutParams);

                        imageContainer.addView(imageView);
                    } else {
                        Log.e(TAG, "Erreur lors du décodage de l'image : " + ze.getName());
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'ouverture du fichier CBZ.", e);
            throw e;
        }
    }
}
