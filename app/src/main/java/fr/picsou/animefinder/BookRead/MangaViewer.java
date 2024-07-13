package fr.picsou.animefinder.BookRead;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import com.bumptech.glide.Glide;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import fr.picsou.animefinder.R;

public class MangaViewer extends AppCompatActivity {

    private static final String TAG = "MangaReaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);
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

        try {
            unzip(new File(mangaName), new File(cbzFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void extractAndDisplayImages(String cbzFilePath, LinearLayout imageContainer) {
        try {
            File cbzFile = new File(cbzFilePath);
            Archive archive = new Archive(cbzFile);

            if (archive != null) {
                FileHeader fileHeader;
                while ((fileHeader = archive.nextFileHeader()) != null) {
                    if (!fileHeader.isDirectory()) {
                        String fileName = fileHeader.getFileNameString().trim();
                        File outputFile = new File(getCacheDir(), fileName);

                        try (FileOutputStream os = new FileOutputStream(outputFile)) {
                            archive.extractFile(fileHeader, os);
                        }

                        ImageView imageView = new ImageView(this);
                        Glide.with(this).load(outputFile).into(imageView);

                        imageContainer.addView(imageView);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'extraction des images du fichier CBZ", e);
        }
    }
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }
}
