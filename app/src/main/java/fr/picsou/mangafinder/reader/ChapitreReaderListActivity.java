package fr.picsou.mangafinder.reader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.picsou.mangafinder.R;

public class ChapitreReaderListActivity extends AppCompatActivity implements ChapterReaderAdapter.OnChapterClickListener {
    private ChapterReaderAdapter adapter;
    private List<File> chapterFiles;
    String language;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapitre_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Create the delete button programmatically
        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setId(View.generateViewId());
        deleteButton.setImageResource(R.drawable.ic_delete_black_24dp);
        deleteButton.setBackgroundColor(getResources().getColor(R.color.recherche));
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.CENTER_VERTICAL
        );
        layoutParams.setMarginEnd(10);
        deleteButton.setPadding(8, 8, 8, 8);
        deleteButton.setOnClickListener(this::deleteAnime);
        toolbar.addView(deleteButton, layoutParams);

        ImageView imageViewCover = findViewById(R.id.image_cover);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String coverUrl = args.getString("cover", "");
            String MangaName = args.getString("MangaName", "");

            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverUrl)
                        .into(imageViewCover);
            }

            String[] titleParts = MangaName.split("-", 2);
            language = (titleParts.length > 1) ? titleParts[0].trim() : "Unknown";
            String actualTitle = (titleParts.length > 1) ? titleParts[1].trim() : MangaName;
            toolbar.setTitle(actualTitle);

            chapterFiles = getChapterFiles(MangaName);

            RecyclerView recyclerView = findViewById(R.id.list_chapters);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adapter = new ChapterReaderAdapter(this, chapterFiles, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private List<File> getChapterFiles(String MangaName) {
        List<File> chapters = new ArrayList<>();
        File animeDir = new File(getFilesDir(), "MangaFinder" + File.separator + MangaName);
        if (animeDir.exists() && animeDir.isDirectory()) {
            File[] files = animeDir.listFiles((dir, name) -> name.endsWith(".cbz"));
            if (files != null) {
                Collections.addAll(chapters, files);
            }
        }
        return chapters;
    }

    @Override
    public void onChapterClick(File chapter) {
        String chapterName = chapter.getName();
        String chapterPath = chapter.getAbsolutePath();
        Intent intent = new Intent(ChapitreReaderListActivity.this, MangaViewer.class);
        intent.putExtra("MANGA_NAME", chapterName);
        intent.putExtra("CBZ_FILE_PATH", chapterPath);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDeleteClick(File chapter) {
        if (chapter.delete()) {
            chapterFiles.remove(chapter);
            if (adapter.getItemCount() == 0) {
                boolean deleted = deleteAnimeFolder();
                if (deleted) {
                    MangaReaderListFragment.refreshBookList();
                    finish();
                }
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void deleteAnime(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Voulez-vous vraiment supprimer cet anime ?");
        builder.setPositiveButton("Oui", (dialog, which) -> {
            boolean deleted = deleteAnimeFolder();
            if (deleted) {
                MangaReaderListFragment.refreshBookList();
                finish();
            }
        });
        builder.setNegativeButton("Non", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private boolean deleteAnimeFolder() {
        String MangaName = Objects.requireNonNull(getSupportActionBar()).getTitle().toString();
        File animeDir = new File(getFilesDir(), "MangaFinder" + File.separator + language + "-" + MangaName);
        if (animeDir.exists() && animeDir.isDirectory()) {
            File[] files = animeDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
            return animeDir.delete();
        }
        return false;
    }
}
